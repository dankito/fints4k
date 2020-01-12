package net.dankito.banking.ui.model


open class Bank @JvmOverloads constructor(
    val bankCode: String,
    var finTsServerAddress: String,
    var bic: String,
    var name: String,
    var iconUrl: String? = null
) {


    internal constructor() : this("", "", "", "") // for object deserializers


    val displayName: String
        get() = name


    override fun toString(): String {
        return "$name ($bankCode)"
    }

}