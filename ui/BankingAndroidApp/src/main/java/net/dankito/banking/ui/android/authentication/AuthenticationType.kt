package net.dankito.banking.ui.android.authentication


enum class AuthenticationType(internal val rawValue: Int) {

    None(3),

    Password(7),

    Biometric(9)

}