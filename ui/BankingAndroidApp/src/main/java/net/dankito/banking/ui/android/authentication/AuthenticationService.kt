package net.dankito.banking.ui.android.authentication

import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File
import org.slf4j.LoggerFactory


open class AuthenticationService(
    protected open val biometricAuthenticationService: IBiometricAuthenticationService,
    protected open val persistence: IBankingPersistence,
    protected open val dataFolder: File,
    protected open val serializer: ISerializer
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

        if (authenticationType == AuthenticationType.None) {
            val authenticationSettings = loadAuthenticationSettings()

            if (authenticationSettings == null) { // first app run -> create a default password
                removeAppProtection()
            }
            else {
                openDatabase(authenticationSettings)
            }
        }
    }


    open fun userLoggedInWithBiometricAuthentication() {
        loadAuthenticationSettings()?.let {
            openDatabase(it)
        }
    }

    open fun userLoggedInWithPassword(enteredPassword: String) {
        openDatabase(enteredPassword)
    }

    protected open fun openDatabase(authenticationSettings: AuthenticationSettings) {
        openDatabase(authenticationSettings.userPassword)
    }

    protected open fun openDatabase(password: String?) {
        persistence.decryptData(password)
    }

    open fun setAuthenticationMethodToBiometric() {
        if (saveNewUserPassword(generateRandomPassword())) {
            if (saveAuthenticationType(AuthenticationType.Biometric)) {
                authenticationType = AuthenticationType.Biometric
            }
        }
    }

    open fun setAuthenticationMethodToPassword(newPassword: String) {
        if (saveNewUserPassword(newPassword)) {
            if (saveAuthenticationType(AuthenticationType.Password)) {
                authenticationType = AuthenticationType.Password
            }
        }
    }

    open fun removeAppProtection() {
        if (saveNewUserPassword(generateRandomPassword())) {
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


    protected open fun saveNewUserPassword(newPassword: String?): Boolean {
        val settings = loadOrCreateDefaultAuthenticationSettings()
        val currentPassword = settings.userPassword

        if (currentPassword != newPassword) {
            settings.userPassword = newPassword

            if (saveAuthenticationSettings(settings)) {
                persistence.changePassword(currentPassword, newPassword) // TODO: actually this is bad. If changing password fails then password is saved in AuthenticationSettings but DB has a different password
                return true
            }

            return false
        }

        return true
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


    open fun generateRandomPassword(): String {
        return generateRandomPassword(30)
    }

    open fun generateRandomPassword(passwordLength: Int): String {
        val dictionary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789§±!@#$%^&*-_=+;:|/?.>,<"

        val passwordBuilder = StringBuilder()
        IntRange(0, passwordLength).forEach {
            passwordBuilder.append(dictionary.random())
        }

        return passwordBuilder.toString()
    }

}