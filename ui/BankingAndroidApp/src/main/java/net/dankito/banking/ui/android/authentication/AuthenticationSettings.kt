package net.dankito.banking.ui.android.authentication


open class AuthenticationSettings(
    open var type: AuthenticationType,
    open var hashedUserPassword: String? = null,
    open var encryptedDefaultPassword: String? = null,
    open var initializationVector: String? = null,
    open var salt: String? = null
) {

    internal constructor() : this(AuthenticationType.None) // for object deserializers

}