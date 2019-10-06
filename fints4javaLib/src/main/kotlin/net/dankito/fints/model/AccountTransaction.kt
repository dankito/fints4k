package net.dankito.fints.model


open class AccountTransaction(
    var referenceNumber: String = "",
    var bezugsReferenceNumber: String? = null,
    var accountIdentification: String = "",
    var statementNumber: String = "",
    var openingBalance: String = "",
    var statement: String = "",
    var accountOwner: String = "",
    var closingBalance: String = ""
) {
}