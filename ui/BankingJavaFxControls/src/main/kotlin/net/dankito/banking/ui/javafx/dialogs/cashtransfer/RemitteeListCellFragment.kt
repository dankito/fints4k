package net.dankito.banking.ui.javafx.dialogs.cashtransfer

import net.dankito.banking.search.Remittee
import tornadofx.ListCellFragment
import tornadofx.bindTo
import tornadofx.label
import tornadofx.vbox


open class RemitteeListCellFragment : ListCellFragment<Remittee>() {

    open val remittee = RemitteeViewModel().bindTo(this)


    override val root = vbox {
        label(remittee.name)

        label(remittee.iban)
    }

}