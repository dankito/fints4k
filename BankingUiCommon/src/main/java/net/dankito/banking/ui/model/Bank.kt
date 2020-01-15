package net.dankito.banking.ui.model


open class Bank @JvmOverloads constructor(
    var name: String,
    val bankCode: String,
    var bic: String,
    var finTsServerAddress: String,
    var iconUrl: String? = null
) {


    internal constructor() : this("", "", "", "") // for object deserializers


    val displayName: String
        get() = name


    override fun toString(): String {
        return "$name ($bankCode)"
    }

}