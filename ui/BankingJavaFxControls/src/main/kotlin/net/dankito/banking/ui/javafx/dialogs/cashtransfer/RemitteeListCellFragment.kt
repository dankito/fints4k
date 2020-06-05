package net.dankito.banking.ui.javafx.dialogs.cashtransfer

import net.dankito.banking.search.Remittee
import tornadofx.*


open class RemitteeListCellFragment : ListCellFragment<Remittee>() {

    open val remittee = RemitteeViewModel().bindTo(this)


    override val root = vbox {
        label(remittee.name)

        label(remittee.bankName) {
            vboxConstraints {
                marginTopBottom(6.0)
            }
        }

        label(remittee.iban)
    }

}