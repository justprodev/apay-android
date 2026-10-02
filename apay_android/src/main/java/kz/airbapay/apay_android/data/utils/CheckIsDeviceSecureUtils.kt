package kz.airbapay.apay_android.data.utils

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK

// FingerprintManager is gone from the latest SDK stubs, BiometricManager covers enrolled biometrics
internal fun checkIsDeviceSecure(
    context: Context
): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            BiometricManager.from(context)
                .canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
}
