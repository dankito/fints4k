package net.dankito.banking.ui.android.authentication


interface IBiometricAuthenticationService {

    val supportsBiometricAuthentication: Boolean


    fun authenticate(authenticationResult: (AuthenticationResult) -> Unit)

}