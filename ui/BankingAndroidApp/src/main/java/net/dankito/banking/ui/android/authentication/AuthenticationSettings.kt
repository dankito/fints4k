package net.dankito.banking.ui.android.authentication


open class AuthenticationSettings(
    open var type: AuthenticationType,
    open var hashedUserPassword: String? = null,
    open var userPassword: String? = null,
    open var encryptedUserPassword: String? = null,
    open var initializationVector: String? = null
) {

    internal constructor() : this(AuthenticationType.None) // for object deserializers

}