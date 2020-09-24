package net.dankito.banking.ui.javafx.dialogs.cashtransfer

import net.dankito.banking.search.TransactionParty
import tornadofx.*


open class RecipientListCellFragment : ListCellFragment<TransactionParty>() {

    companion object {
        const val ItemHeight = 60.0
    }


    open val recipient = RecipientViewModel().bindTo(this)


    override val root = vbox {
        label(recipient.name)

        label(recipient.bankName) {
            vboxConstraints {
                marginTopBottom(6.0)
            }
        }

        label(recipient.iban)
    }

}