package net.dankito.fints.banks


class InMemoryBankFinderTest : BankFinderTestBase() {

    override fun createBankFinder(): IBankFinder {
        return InMemoryBankFinder()
    }

}