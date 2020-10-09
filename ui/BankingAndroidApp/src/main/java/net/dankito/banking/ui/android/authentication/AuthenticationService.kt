package net.dankito.banking.ui.android.authentication

import android.util.Base64
import at.favre.lib.crypto.bcrypt.BCrypt
import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.banking.ui.android.security.CryptographyManager
import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File
import org.slf4j.LoggerFactory
import javax.crypto.Cipher


open class AuthenticationService(
    protected open val biometricAuthenticationService: IBiometricAuthenticationService,
    protected open val persistence: IBankingPersistence,
    protected open val dataFolder: File,
    protected open val serializer: ISerializer,
    protected open val cryptographyManager: CryptographyManager = CryptographyManager()
) {

    companion object {
        private const val AuthenticationSettingsFilename = "a"

        private const val EncryptionKeyName = "BankingAndroidKey"

        private const val DefaultPasswordEncryptionKey = "AnyData" // TODO: store in a secure place

        private val log = LoggerFactory.getLogger(AuthenticationService::class.java)
    }


    open val isBiometricAuthenticationSupported: Boolean
        get() = biometricAuthenticationService.supportsBiometricAuthentication

    open var authenticationType: AuthenticationType = AuthenticationType.None
        protected set

    protected open var encryptionCipherForBiometric: Cipher? = null


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

    open fun authenticateUserWithBiometricToSetAsNewAuthenticationMethod(authenticationResult: (AuthenticationResult) -> Unit) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            authenticationResult(AuthenticationResult(false, "Biometric authentication is only supported on Android 6 and above"))
            return
        }

        val cipher = cryptographyManager.getInitializedCipherForEncryption(EncryptionKeyName)

        biometricAuthenticationService.authenticate(cipher) { result ->
            if (result.successful) {
                this.encryptionCipherForBiometric = cipher
            }

            authenticationResult(result)
        }
    }

    open fun authenticateUserWithBiometric(result: (Boolean) -> Unit) {
        // Biometric authentication is only supported on Android 6 and above
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            result(false)
            return
        }

        loadAuthenticationSettings()?.let { settings ->
            val iv = decodeFromBase64(settings.initializationVector ?: "")

            val cipher = cryptographyManager.getInitializedCipherForDecryption(EncryptionKeyName, iv)
            biometricAuthenticationService.authenticate(cipher) { authenticationResult ->
                if (authenticationResult.successful) {
                    settings.encryptedDefaultPassword?.let {
                        val encryptedUserPassword = decodeFromBase64(it)
                        val decrypted = cryptographyManager.decryptData(encryptedUserPassword, cipher)

                        result(openDatabase(decrypted))
                    }
                }
                else {
                    result(false)
                }
            }
        }
        ?: run { result(false) }
    }

    protected open fun openDatabase(settings: AuthenticationSettings) {
        if (settings.type == AuthenticationType.None) {
            settings.encryptedDefaultPassword?.let { encryptedPassword ->
                settings.initializationVector?.let { iv ->
                    settings.salt?.let { salt ->
                        val decrypted = cryptographyManager.decryptDataWithPbe(decodeFromBase64(encryptedPassword), DefaultPasswordEncryptionKey,
                            decodeFromBase64(iv), decodeFromBase64(salt))

                        openDatabase(decrypted)
                    }
                }
            }
        }
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

        settings.type = type
        settings.hashedUserPassword = if (type == AuthenticationType.Password) BCrypt.withDefaults().hashToString(12, newPassword.toCharArray()) else null
        settings.initializationVector = null
        settings.salt = null

        if (type == AuthenticationType.Biometric) {
            encryptionCipherForBiometric?.let { encryptionCipher ->
                val encryptedPassword = cryptographyManager.encryptData(newPassword, encryptionCipher)
                settings.encryptedDefaultPassword = encodeToBase64(encryptedPassword)
                settings.initializationVector = encodeToBase64(encryptionCipher.iv)
            }
        }
        else if (type == AuthenticationType.None) {
            val salt = cryptographyManager.generateRandomBytes(8)
            val (encryptedPassword, iv) = cryptographyManager.encryptDataWithPbe(newPassword, DefaultPasswordEncryptionKey, salt)
            settings.encryptedDefaultPassword = encodeToBase64(encryptedPassword)
            settings.initializationVector = encodeToBase64(iv)
            settings.salt = encodeToBase64(salt)
        }

        if (saveAuthenticationSettings(settings)) {
            this.authenticationType = type
            this.encryptionCipherForBiometric = null

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


    open fun encodeToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    open fun decodeFromBase64(data: String): ByteArray {
        return Base64.decode(data, Base64.DEFAULT)
    }

}