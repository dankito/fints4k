package net.dankito.fints.model

import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache


open class BankData(
    val bankCode: String,
    val countryCode: Int,
    var finTs3ServerAddress: String,
    var bpdVersion: Int = 0,
    var supportedLanguages: List<Dialogsprache> = listOf()
) {


    override fun toString(): String {
        return bankCode
    }

}