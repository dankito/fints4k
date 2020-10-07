package net.dankito.banking.ui.android.authentication

import javax.crypto.Cipher


interface IBiometricAuthenticationService {

    val supportsBiometricAuthentication: Boolean


    fun authenticate(cipher: Cipher? = null, authenticationResult: (AuthenticationResult) -> Unit)

}