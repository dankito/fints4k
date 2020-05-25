package net.dankito.banking.ui.javafx.controls

import javafx.beans.binding.ObjectBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.Callback
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.DateFormat


open class AccountTransactionsTable @JvmOverloads constructor(
    protected val presenter: BankingPresenter,
    transactions: ObservableList<AccountTransaction> = FXCollections.emptyObservableList<AccountTransaction>()
) : TableView<AccountTransaction>(transactions) {


    companion object {
        val ValueDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

        private val LabelMargin = Insets(4.0, 0.0, 4.0, 4.0)
    }


    init {
        initUi()
    }


    protected open fun initUi() {
        column(messages["account.transactions.table.column.header.value.date"], AccountTransaction::valueDate) {
            prefWidth = 115.0

            cellFormat {
                text = ValueDateFormat.format(it)
                alignment = Pos.CENTER_LEFT
                paddingLeft = 4.0
            }
        }

        columns.add(TableColumn<AccountTransaction, AccountTransaction>(messages["account.transactions.table.column.header.usage"]).apply {

            this.cellFormat {
                contentDisplay = ContentDisplay.GRAPHIC_ONLY

                graphic = vbox {
                    prefHeight = 94.0
                    alignment = Pos.CENTER_LEFT

                    label(it.bookingText ?: "") {
                        vboxConstraints {
                            margin = LabelMargin
                        }
                    }

                    label(it.otherPartyName ?: "") {
                        isVisible = it.showOtherPartyName
                        ensureOnlyUsesSpaceIfVisible()

                        vboxConstraints {
                            margin = LabelMargin
                        }
                    }

                    label(it.usage) {
                        vboxConstraints {
                            margin = LabelMargin
                        }
                    }
                }
            }

            cellValueFactory = Callback { object : ObjectBinding<AccountTransaction>() {
                override fun computeValue(): AccountTransaction {
                    return it.value
                }

            } }

            weightedWidth(4.0)
        })

        columns.add(TableColumn<AccountTransaction, String>(messages["account.transactions.table.column.header.amount"]).apply {
            prefWidth = 85.0

            this.cellFormat {
                text = it
                alignment = Pos.CENTER_RIGHT
                paddingRight = 4.0

                style {
                    textFill = if (rowItem.amount.toLong() < 0) Color.RED else Color.GREEN
                }
            }

            cellValueFactory = Callback { object : ObjectBinding<String>() {
                override fun computeValue(): String {
                    return presenter.formatAmount(it.value.amount) + " " + it.value.currency
                }

            } }
        })


        columnResizePolicy = SmartResize.POLICY

        vgrow = Priority.ALWAYS
    }

}