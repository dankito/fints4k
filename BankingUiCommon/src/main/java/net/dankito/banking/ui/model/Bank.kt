package net.dankito.banking.ui.model


open class Bank(
    val bankCode: String,
    var finTsServerAddress: String,
    var bic: String,
    var name: String
) {


    internal constructor() : this("", "", "", "") // for object deserializers


    val displayName: String
        get() = name

    var iconUrl: String? = null


    override fun toString(): String {
        return "$name ($bankCode)"
    }

}