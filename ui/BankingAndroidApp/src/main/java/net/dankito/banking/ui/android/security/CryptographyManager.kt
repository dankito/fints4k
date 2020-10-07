package net.dankito.banking.ui.android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


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


    open fun getInitializedCipherForEncryption(keyName: String): Cipher {
        return getInitializedCipher(keyName, Cipher.ENCRYPT_MODE)
    }

    open fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher {
        return getInitializedCipher(keyName, Cipher.DECRYPT_MODE, initializationVector)
    }

    protected open fun getInitializedCipher(keyName: String, cipherMode: Int, initializationVector: ByteArray? =  null): Cipher {
        val cipher = Cipher.getInstance(CipherTransformation)
        val secretKey = getOrCreateSecretKey(keyName)

        cipher.init(cipherMode, secretKey, initializationVector?.let { GCMParameterSpec(128, initializationVector) })

        return cipher
    }


    open fun encryptData(plaintext: String, cipher: Cipher): ByteArray {
        return cipher.doFinal(plaintext.toByteArray(PasswordCharset))
    }

    open fun decryptData(cipherText: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(cipherText)
        return String(plaintext, PasswordCharset)
    }

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

}