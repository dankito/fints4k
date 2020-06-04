package net.dankito.banking.ui.javafx.dialogs.cashtransfer

import javafx.beans.property.SimpleStringProperty
import net.dankito.banking.search.Remittee
import tornadofx.ItemViewModel


open class RemitteeViewModel : ItemViewModel<Remittee>() {

    val name = bind { SimpleStringProperty(item?.name) }

    val iban = bind { SimpleStringProperty(item?.iban) }

}