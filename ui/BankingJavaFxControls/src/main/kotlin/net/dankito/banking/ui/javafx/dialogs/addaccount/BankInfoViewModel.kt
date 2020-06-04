package net.dankito.banking.ui.javafx.dialogs.addaccount

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.banking.fints.model.BankInfo
import tornadofx.ItemViewModel


open class BankInfoViewModel : ItemViewModel<BankInfo>() {

    val supportsFinTs3_0 = bind { SimpleBooleanProperty(item?.supportsFinTs3_0 ?: false) }

    val bankName = bind { SimpleStringProperty(item?.name) }

    val bankCode = bind { SimpleStringProperty(item?.bankCode) }

    val bankAddress = bind { SimpleStringProperty(item?.let { item.postalCode + " " + item.city }) }

}