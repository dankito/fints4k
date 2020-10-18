package net.dankito.banking.ui.android.security


enum class SecurityProviderServiceType(val type: String) {

    AlgorithmParameterGenerator("AlgorithmParameterGenerator"),

    AlgorithmParameters("AlgorithmParameters"),

    CertPathBuilder("CertPathBuilder"),

    CertPathValidator("CertPathValidator"),

    CertStore("CertStore"),

    CertificateFactory("CertificateFactory"),

    Cipher("Cipher"),

    KeyAgreement("KeyAgreement"),

    KeyFactory("KeyFactory"),

    KeyGenerator("KeyGenerator"),

    KeyManagerFactory("KeyManagerFactory"),

    KeyPairGenerator("KeyPairGenerator"),

    KeyStore("KeyStore"),

    Mac("Mac"),

    MessageDigest("MessageDigest"),

    SSLContext("SSLContext"),

    SecretKeyFactory("SecretKeyFactory"),

    SecureRandom("SecureRandom"),

    Signature("Signature"),

}