package net.dankito.banking.bankfinder


class InMemoryBankFinderTest : BankFinderTestBase() {

    override fun createBankFinder(): IBankFinder {
        return InMemoryBankFinder()
    }

}