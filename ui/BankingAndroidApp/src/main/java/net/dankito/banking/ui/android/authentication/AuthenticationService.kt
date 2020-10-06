package net.dankito.banking.ui.android.authentication

import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File


open class AuthenticationService(
    protected val biometricAuthenticationService: IBiometricAuthenticationService,
    protected val dataFolder: File,
    protected val serializer: ISerializer
) {

    open val isBiometricAuthenticationSupported: Boolean
        get() = biometricAuthenticationService.supportsBiometricAuthentication

    open var authenticationType: AuthenticationType = AuthenticationType.None
        protected set


    fun setAuthenticationMethodToBiometric() {

    open fun getAuthenticationType(): AuthenticationType {
        return AuthenticationType.None
    }


    fun setAuthenticationMethodToBiometric() {

    }

    fun setAuthenticationMethodToPassword(newPassword: String) {

    }

    fun removeAppProtection() {

    }

}