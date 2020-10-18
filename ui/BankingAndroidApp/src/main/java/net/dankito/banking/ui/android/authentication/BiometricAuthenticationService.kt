package net.dankito.banking.ui.android.authentication

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.util.CurrentActivityTracker
import javax.crypto.Cipher


open class BiometricAuthenticationService(
    protected open val context: Context,
    protected open val activityTracker: CurrentActivityTracker,
    protected open val biometricManager: BiometricManager = BiometricManager.from(context)
) : IBiometricAuthenticationService {

    override val supportsBiometricAuthentication: Boolean
        get() = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS


    override fun authenticate(cipher: Cipher?, authenticationResult: (AuthenticationResult) -> Unit) {
        activityTracker.currentOrNextActivity { activity ->
            val executor = ContextCompat.getMainExecutor(context)

            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationError(errorCode: Int, errorString: CharSequence) {
                        super.onAuthenticationError(errorCode, errorString)
                        authenticationResult(AuthenticationResult(false, errorString.toString()))
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)

                        authenticationResult(AuthenticationResult(true))
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        authenticationResult(AuthenticationResult(false))
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.activity_login_authenticate_with_biometrics_prompt))
                .setNegativeButtonText(context.getString(android.R.string.cancel))
                .build()

            if (cipher == null) {
                biometricPrompt.authenticate(promptInfo)
            }
            else {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }

    }
}