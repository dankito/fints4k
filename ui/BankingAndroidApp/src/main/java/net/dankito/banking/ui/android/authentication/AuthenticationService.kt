package net.dankito.banking.ui.android.authentication

import at.favre.lib.crypto.bcrypt.BCrypt
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
        private const val AuthenticationSettingsFilename = "s"

        private val log = LoggerFactory.getLogger(AuthenticationService::class.java)
    }


    open val isBiometricAuthenticationSupported: Boolean
        get() = biometricAuthenticationService.supportsBiometricAuthentication

    open var authenticationType: AuthenticationType = AuthenticationType.None
        protected set


    init {
        val settings = loadAuthenticationSettings()

        if (settings == null) { // first app run -> create a default password
            removeAppProtection()
        }
        else {
            authenticationType = settings.type

            if (settings.type == AuthenticationType.None) {
                openDatabase(settings)
            }
        }
    }


    open fun authenticateUserWithPassword(enteredPassword: String): Boolean {
        if (isCorrectUserPassword(enteredPassword)) {
            return openDatabase(enteredPassword)
        }

        return false
    }

    open fun isCorrectUserPassword(enteredPassword: String): Boolean {
        loadAuthenticationSettings()?.let { settings ->
            val result = BCrypt.verifyer().verify(enteredPassword.toCharArray(), settings.hashedUserPassword)

            return result.verified
        }

        return false
    }

    protected open fun openDatabase(authenticationSettings: AuthenticationSettings) {
        openDatabase(authenticationSettings.userPassword)
    }

    protected open fun openDatabase(password: String?): Boolean {
        return persistence.decryptData(password)
    }


    open fun setAuthenticationMethodToBiometric() {
        saveNewAuthenticationMethod(AuthenticationType.Biometric, generateRandomPassword())
    }

    open fun setAuthenticationMethodToPassword(newPassword: String) {
        saveNewAuthenticationMethod(AuthenticationType.Password, newPassword)
    }

    open fun removeAppProtection() {
        saveNewAuthenticationMethod(AuthenticationType.None, generateRandomPassword())
    }


    protected open fun saveNewAuthenticationMethod(type: AuthenticationType, newPassword: String): Boolean {
        val settings = loadOrCreateDefaultAuthenticationSettings()

        if (type == settings.type &&
            ((type != AuthenticationType.Password && settings.userPassword == newPassword)
                    || (type == AuthenticationType.Password && isCorrectUserPassword(newPassword)))) { // nothing changed
            return true
        }

        settings.type = type
        settings.hashedUserPassword = if (type == AuthenticationType.Password) BCrypt.withDefaults().hashToString(12, newPassword.toCharArray()) else null
        settings.userPassword = if (type == AuthenticationType.Password) null else newPassword

        if (saveAuthenticationSettings(settings)) {
            this.authenticationType = type
            persistence.changePassword(newPassword) // TODO: actually this is bad. If changing password fails then password is saved in AuthenticationSettings but DB has a different password
            return true
        }

        return false
    }

    protected open fun loadOrCreateDefaultAuthenticationSettings(): AuthenticationSettings {
        return loadAuthenticationSettings() ?: AuthenticationSettings(AuthenticationType.None)
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