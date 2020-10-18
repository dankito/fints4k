package net.dankito.banking.ui.comparator

import net.dankito.banking.ui.model.TypedBankAccount


open class BankAccountComparator : Comparator<TypedBankAccount> {

    override fun compare(a: TypedBankAccount, b: TypedBankAccount): Int {
        if (a.bank.displayIndex == b.bank.displayIndex) {
            return a.displayIndex.compareTo(b.displayIndex)
        }

        return a.bank.displayIndex.compareTo(b.bank.displayIndex)
    }

}