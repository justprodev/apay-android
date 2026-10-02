# card.io native libraries (card number recognition), taken from https://github.com/card-io/card.io-Android-source (MIT).
# OpenCV (core + imgproc) is linked statically from opencv/lib, see /opencv/build_opencv.ps1.
cmake_minimum_required(VERSION 3.18.1)
project(cardio CXX C)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_EXTENSIONS ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

add_compile_definitions(ANDROID_DMZ=1)
# card.io sources predate modern clang: silence warnings and the narrowing of IPL_DEPTH_* constants
add_compile_options(-O3 -ffunction-sections -fdata-sections -w -Wno-c++11-narrowing)
add_link_options(-Wl,-z,max-page-size=16384 -Wl,--gc-sections)

set(DMZ ${CMAKE_CURRENT_SOURCE_DIR}/card.io-dmz)
set(OPENCV ${CMAKE_CURRENT_SOURCE_DIR}/opencv)

add_library(cpufeatures STATIC ${ANDROID_NDK}/sources/android/cpufeatures/cpu-features.c)
target_include_directories(cpufeatures PUBLIC ${ANDROID_NDK}/sources/android/cpufeatures)
target_link_libraries(cpufeatures PUBLIC dl)

foreach (lib opencv_core opencv_imgproc zlib)
    add_library(${lib} STATIC IMPORTED)
    set_target_properties(${lib} PROPERTIES IMPORTED_LOCATION ${OPENCV}/lib/${ANDROID_ABI}/lib${lib}.a)
endforeach ()

# --- libcardioDecider.so: chooses the recognizer library at runtime ---
add_library(cardioDecider SHARED ${DMZ}/processor_support.cpp nativeDecider.cpp)
target_include_directories(cardioDecider PRIVATE ${DMZ})
target_compile_definitions(cardioDecider PRIVATE ANDROID_HAS_NEON=1)
target_link_libraries(cardioDecider cpufeatures log)

function (cardio_recognizer name)
    add_library(${name} SHARED ${DMZ}/dmz_all.cpp nativeRecognizer.cpp)
    target_include_directories(${name} PRIVATE ${OPENCV}/include ${DMZ} ${DMZ}/cv)
    target_link_libraries(${name} opencv_imgproc opencv_core zlib cpufeatures log jnigraphics)
endfunction ()

# --- libcardioRecognizer.so (NEON on armeabi-v7a, the one used on x86/x86_64) ---
cardio_recognizer(cardioRecognizer)
target_compile_definitions(cardioRecognizer PRIVATE SCAN_EXPIRY=1)
if (ANDROID_ABI STREQUAL "armeabi-v7a")
    target_compile_definitions(cardioRecognizer PRIVATE ANDROID_HAS_NEON=1)
    target_compile_options(cardioRecognizer PRIVATE -mfpu=neon)
elseif (ANDROID_ABI STREQUAL "arm64-v8a" OR ANDROID_ABI STREQUAL "x86_64")
    # 64-bit changed register names, the NEON asm of card.io does not support it
    target_compile_definitions(cardioRecognizer PRIVATE ANDROID_HAS_NEON=0)
endif ()

# --- libcardioRecognizer_tegra2.so (no NEON, used on arm64 and VFPv3-D16 devices) ---
if (NOT ANDROID_ABI STREQUAL "x86")
    cardio_recognizer(cardioRecognizer_tegra2)
    target_compile_definitions(cardioRecognizer_tegra2 PRIVATE ANDROID_HAS_NEON=0)
    if (ANDROID_ABI STREQUAL "armeabi-v7a")
        target_compile_options(cardioRecognizer_tegra2 PRIVATE -mfpu=vfpv3-d16)
    endif ()
endif ()