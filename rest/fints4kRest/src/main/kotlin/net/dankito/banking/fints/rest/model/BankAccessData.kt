package net.dankito.banking.fints.rest.model


open class BankAccessData(
    open val bankCode: String,
    open val loginName: String,
    open val password: String,
    open val finTsServerAddress: String? = null
) {

    internal constructor() : this("", "", "") // for object deserializers

}