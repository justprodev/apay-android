# Builds the static OpenCV 2.4.13.7 libraries (core + imgproc) used by card.io for all Android ABIs.
# The output is committed to apay_android/src/main/cpp/opencv, rerun only when OpenCV/NDK must be updated.
# Requirements: Android NDK (r28+ aligns native libs to 16KB), cmake + ninja (Android SDK cmake 3.22.1 works).
param(
    [string]$Ndk = "$env:ANDROID_HOME\ndk\28.2.13676358",
    [string]$Cmake = "$env:ANDROID_HOME\cmake\3.22.1\bin",
    [string]$Work = "$env:TEMP\ocv-build",
    [string[]]$Abis = @("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
)
# native tools write warnings to stderr, failures are detected through $LASTEXITCODE
$ErrorActionPreference = "Continue"
$version = "2.4.13.7"
$out = Join-Path $PSScriptRoot "..\apay_android\src\main\cpp\opencv"
$env:Path = "$Cmake;$env:Path"
New-Item -ItemType Directory -Force $Work | Out-Null

$zip = Join-Path $Work "opencv-$version.zip"
$src = Join-Path $Work "opencv-$version"
if (-not (Test-Path $src)) {
    if (-not (Test-Path $zip)) { curl.exe -sL -o $zip "https://github.com/opencv/opencv/archive/refs/tags/$version.zip" }
    Expand-Archive $zip $Work
}

$disabled = (Get-ChildItem "$src\modules" -Directory -Name) | Where-Object { $_ -notin "core", "imgproc" } | ForEach-Object { "-DBUILD_opencv_$_=OFF" }

foreach ($abi in $Abis) {
    $bd = Join-Path $Work $abi
    New-Item -ItemType Directory -Force $bd | Out-Null
    Push-Location $bd
    # -std=gnu++11: OpenCV 2.4 uses std::mem_fun_ref which is removed in C++17
    cmake -G Ninja "-DCMAKE_TOOLCHAIN_FILE=$Ndk\build\cmake\android.toolchain.cmake" "-DANDROID_ABI=$abi" `
        -DANDROID_PLATFORM=android-23 -DANDROID_STL=c++_static -DCMAKE_BUILD_TYPE=Release -DBUILD_SHARED_LIBS=OFF `
        -DBUILD_ANDROID_EXAMPLES=OFF -DBUILD_DOCS=OFF -DBUILD_EXAMPLES=OFF -DBUILD_PERF_TESTS=OFF -DBUILD_TESTS=OFF `
        -DBUILD_JAVA=OFF -DBUILD_ANDROID_SERVICE=OFF -DBUILD_opencv_apps=OFF -DBUILD_opencv_java=OFF -DBUILD_opencv_python=OFF `
        -DWITH_CUDA=OFF -DWITH_IPP=OFF -DWITH_TBB=OFF -DWITH_EIGEN=OFF -DWITH_PNG=OFF -DWITH_JPEG=OFF -DWITH_TIFF=OFF `
        -DWITH_JASPER=OFF -DWITH_OPENEXR=OFF -DWITH_FFMPEG=OFF -DWITH_OPENCL=OFF -DBUILD_ZLIB=ON `
        "-DCMAKE_CXX_FLAGS=-std=gnu++11 -g0 -Os -fvisibility=hidden -ffunction-sections -fdata-sections" `
        "-DCMAKE_C_FLAGS=-g0 -Os -ffunction-sections -fdata-sections" @disabled $src
    if ($LASTEXITCODE) { throw "configure failed for $abi" }
    cmake --build . --target opencv_core opencv_imgproc
    if ($LASTEXITCODE) { throw "build failed for $abi" }
    $libDir = Join-Path $out "lib\$abi"
    New-Item -ItemType Directory -Force $libDir | Out-Null
    Get-ChildItem -Recurse -Include libopencv_core.a, libopencv_imgproc.a, libzlib.a $bd | Copy-Item -Destination $libDir -Force
    Pop-Location
}

$inc = Join-Path $out "include"
New-Item -ItemType Directory -Force "$inc\opencv2" | Out-Null
Copy-Item -Recurse -Force "$src\modules\core\include\opencv2\*" "$inc\opencv2"
Copy-Item -Recurse -Force "$src\modules\imgproc\include\opencv2\*" "$inc\opencv2"
Copy-Item -Force "$Work\$($Abis[0])\opencv2\opencv_modules.hpp" "$inc\opencv2"
Copy-Item -Force "$Work\$($Abis[0])\cvconfig.h" $inc
Copy-Item -Force "$src\LICENSE" $out