package net.dankito.banking.ui.android.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


open class CryptographyManager {

    companion object {

        const val AndroidKeyStore = "AndroidKeyStore"

        val PasswordCharset = Charsets.UTF_8

        private const val KeySize: Int = 256
        private const val EncryptionBlockMode = KeyProperties.BLOCK_MODE_GCM
        private const val EncryptionPadding = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val EncryptionAlgorithm = KeyProperties.KEY_ALGORITHM_AES

        private const val CipherTransformation = "$EncryptionAlgorithm/$EncryptionBlockMode/$EncryptionPadding"

    }


    @RequiresApi(Build.VERSION_CODES.M)
    open fun getInitializedCipherForEncryption(keyName: String): Cipher {
        return getInitializedCipher(keyName, Cipher.ENCRYPT_MODE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    open fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher {
        return getInitializedCipher(keyName, Cipher.DECRYPT_MODE, initializationVector)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected open fun getInitializedCipher(keyName: String, cipherMode: Int, initializationVector: ByteArray? =  null): Cipher {
        val cipher = Cipher.getInstance(CipherTransformation)
        val secretKey = getOrCreateSecretKey(keyName)

        cipher.init(cipherMode, secretKey, initializationVector?.let { GCMParameterSpec(128, initializationVector) })

        return cipher
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected open fun getOrCreateSecretKey(keyName: String): SecretKey {
        val keyStore = KeyStore.getInstance(AndroidKeyStore)
        keyStore.load(null)
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        val paramsBuilder = KeyGenParameterSpec.Builder(keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        paramsBuilder.apply {
            setBlockModes(EncryptionBlockMode)
            setEncryptionPaddings(EncryptionPadding)
            setKeySize(KeySize)
            setUserAuthenticationRequired(true)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
            AndroidKeyStore)
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }


    open fun encryptData(plaintext: String, cipher: Cipher): ByteArray {
        return cipher.doFinal(plaintext.toByteArray(PasswordCharset))
    }

    open fun decryptData(cipherText: ByteArray, cipher: Cipher): String {
        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, PasswordCharset)
    }


    open fun encryptDataWithPbe(plaintext: String, password: String, salt: ByteArray): Pair<ByteArray, ByteArray> {
        val secret: SecretKey = generatePbeSecretKey(password, salt)

        val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secret)
        val initializationVector = cipher.iv

        return Pair(cipher.doFinal(plaintext.toByteArray(PasswordCharset)), initializationVector)
    }

    open fun decryptDataWithPbe(cipherText: ByteArray, password: String, initializationVector: ByteArray, salt: ByteArray): String {
        val secret: SecretKey = generatePbeSecretKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(initializationVector))

        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, PasswordCharset)
    }

    protected open fun generatePbeSecretKey(userPassword: String, salt: ByteArray): SecretKey {
        // Initialize PBE with password
        val factory = SecretKeyFactory.getInstance(findBestPbeAlgorithm()!!)
        val spec = PBEKeySpec(userPassword.toCharArray(), salt, 65536, 256)
        val key = factory.generateSecret(spec)

        return SecretKeySpec(key.encoded, "AES")
    }

    open fun generateRandomBytes(countBytes: Int): ByteArray {
        return ByteArray(countBytes).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong().nextBytes(this)
            } else {
                SecureRandom().nextBytes(this)
            }
        }
    }


    open fun findBestPbeAlgorithm(): String? {
        return findBestMatchingAlgorithm(SecurityProviderServiceType.SecretKeyFactory, "PBKDF2","PBKDF2WithHmacSHA256")
            ?: findBestMatchingAlgorithm(SecurityProviderServiceType.SecretKeyFactory, "PBE")
    }

    open fun findBestMatchingAlgorithm(type: SecurityProviderServiceType, nameStartsWith: String, vararg preferredAlgorithms: String): String? {
        val supportedAlgorithms = listServiceTypeAlgorithmsWithName(type, "PBKDF2")

        val bestMatchingAlgorithm = preferredAlgorithms.firstOrNull { supportedAlgorithms.contains(it) }
            ?: supportedAlgorithms.maxBy { it.length }

        return bestMatchingAlgorithm
    }

    open fun listServiceTypeAlgorithmsWithName(type: SecurityProviderServiceType, nameStartsWith: String): List<String> {
        return listServiceTypeAlgorithms(type)
            .filter { it.startsWith(nameStartsWith, true) }
    }

    open fun listServiceTypeAlgorithms(type: SecurityProviderServiceType): List<String> {
        val algorithms = mutableListOf<String>()

        Security.getProviders().forEach { provider ->
            algorithms.addAll(provider.services
                .filter { it.type == type.type }
                .map { it.algorithm }
            )
        }

        return algorithms
    }

}