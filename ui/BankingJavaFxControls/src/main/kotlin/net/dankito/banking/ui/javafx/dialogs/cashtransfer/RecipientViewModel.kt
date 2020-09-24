package net.dankito.banking.ui.javafx.dialogs.cashtransfer

import javafx.beans.property.SimpleStringProperty
import net.dankito.banking.search.TransactionParty
import tornadofx.ItemViewModel


open class RecipientViewModel : ItemViewModel<TransactionParty>() {

    val name = bind { SimpleStringProperty(item?.name) }

    val bankName = bind { SimpleStringProperty(item?.bankName) }

    val iban = bind { SimpleStringProperty(item?.iban) }

}