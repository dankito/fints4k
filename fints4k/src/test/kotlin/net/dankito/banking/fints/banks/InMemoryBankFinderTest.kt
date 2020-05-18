package net.dankito.banking.fints.banks


class InMemoryBankFinderTest : BankFinderTestBase() {

    override fun createBankFinder(): IBankFinder {
        return InMemoryBankFinder()
    }

}