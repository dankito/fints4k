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
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.DateFormat


open class AccountTransactionsTable @JvmOverloads constructor(
    protected val presenter: BankingPresenter,
    transactions: ObservableList<IAccountTransaction> = FXCollections.emptyObservableList()
) : TableView<IAccountTransaction>(transactions) {


    companion object {
        val ValueDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

        private val LabelMargin = Insets(4.0, 0.0, 4.0, 4.0)
    }


    init {
        initUi()
    }


    protected open fun initUi() {
        column(messages["account.transactions.table.column.header.value.date"], IAccountTransaction::valueDate) {
            prefWidth = 115.0

            cellFormat {
                text = ValueDateFormat.format(it)
                alignment = Pos.CENTER_LEFT
                paddingLeft = 4.0
            }
        }

        columns.add(TableColumn<IAccountTransaction, IAccountTransaction>(messages["account.transactions.table.column.header.reference"]).apply {

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

                    label(it.reference) {
                        vboxConstraints {
                            margin = LabelMargin
                        }
                    }
                }
            }

            cellValueFactory = Callback { object : ObjectBinding<IAccountTransaction>() {
                override fun computeValue(): IAccountTransaction {
                    return it.value
                }

            } }

            weightedWidth(4.0)
        })

        columns.add(TableColumn<IAccountTransaction, String>(messages["account.transactions.table.column.header.amount"]).apply {
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