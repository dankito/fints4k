package net.dankito.banking.ui.android.authentication

import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File
import org.slf4j.LoggerFactory


open class AuthenticationService(
    protected val biometricAuthenticationService: IBiometricAuthenticationService,
    protected val dataFolder: File,
    protected val serializer: ISerializer
) {

    companion object {
        private const val AuthenticationTypeFilename = "a"

        private const val AuthenticationSettingsFilename = "s"

        private val log = LoggerFactory.getLogger(AuthenticationService::class.java)
    }


    open val isBiometricAuthenticationSupported: Boolean
        get() = biometricAuthenticationService.supportsBiometricAuthentication

    open var authenticationType: AuthenticationType = AuthenticationType.None
        protected set


    init {
        authenticationType = loadAuthenticationType()
    }


    open fun setAuthenticationMethodToBiometric() {
        if (saveUserPasswordIfChanged(null)) {
            if (saveAuthenticationType(AuthenticationType.Biometric)) {
                authenticationType = AuthenticationType.Biometric
            }
        }
    }

    open fun setAuthenticationMethodToPassword(newPassword: String) {
        if (saveUserPasswordIfChanged(newPassword)) {
            if (saveAuthenticationType(AuthenticationType.Password)) {
                authenticationType = AuthenticationType.Password
            }
        }
    }

    open fun removeAppProtection() {
        if (saveUserPasswordIfChanged(null)) {
            if (saveAuthenticationType(AuthenticationType.None)) {
                authenticationType = AuthenticationType.None
            }
        }
    }


    open fun isCorrectUserPassword(password: String): Boolean {
        loadAuthenticationSettings()?.let { settings ->
            return settings.userPassword == password
        }

        return false
    }


    protected open fun loadAuthenticationType(): AuthenticationType {
        try {
            val file = File(dataFolder, AuthenticationTypeFilename)

            if (file.exists()) {

                val fileContent = file.readText()

                return when (fileContent.toInt()) {
                    AuthenticationType.Password.rawValue -> AuthenticationType.Password
                    AuthenticationType.Biometric.rawValue -> AuthenticationType.Biometric
                    AuthenticationType.None.rawValue -> AuthenticationType.None
                    else -> AuthenticationType.None
                }
            }
        } catch (e: Exception) {
            log.error("Could not load AuthenticationType", e)
        }

        return AuthenticationType.None
    }

    protected open fun saveAuthenticationType(type: AuthenticationType): Boolean {
        try {
            val file = File(dataFolder, AuthenticationTypeFilename)

            file.writeText(type.rawValue.toString())

            return true
        } catch (e: Exception) {
            log.error("Could not save AuthenticationType", e)
        }

        return false
    }


    protected open fun saveUserPasswordIfChanged(userPassword: String?): Boolean {
        val settings = loadOrCreateDefaultAuthenticationSettings()

        if (settings.userPassword != userPassword) {
            settings.userPassword = userPassword

            return saveAuthenticationSettings(settings)
        }

        return false
    }

    protected open fun loadOrCreateDefaultAuthenticationSettings(): AuthenticationSettings {
        return loadAuthenticationSettings() ?: AuthenticationSettings(null)
    }

    protected open fun loadAuthenticationSettings(): AuthenticationSettings? {
        try {
            val file = File(dataFolder, AuthenticationSettingsFilename)

            if (file.exists()) {
                val json = file.readText()

                return serializer.deserializeObject(json, AuthenticationSettings::class)
            }
        } catch (e: Exception) {
            log.error("Could not load AuthenticationSettings", e)
        }

        return null
    }

    protected open fun saveAuthenticationSettings(settings: AuthenticationSettings): Boolean {
        try {
            serializer.serializeObjectToString(settings)?.let { json ->
                val file = File(dataFolder, AuthenticationSettingsFilename)

                file.writeText(json)

                return true
            }
        } catch (e: Exception) {
            log.error("Could not save AuthenticationSettings", e)
        }

        return false
    }

}