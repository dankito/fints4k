package net.dankito.banking.bankfinder


expect class BankListDeserializer() {

    fun loadBankList(): List<BankInfo>

}