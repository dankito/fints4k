package net.dankito.banking.ui.javafx.dialogs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.fints.model.BankInfo
import net.dankito.utils.javafx.ui.controls.ProcessingIndicatorButton
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import net.dankito.utils.javafx.ui.extensions.setBackgroundToColor
import tornadofx.*


open class AddAccountDialog(protected val presenter: BankingPresenter) : Window() {

    companion object {
        private val LabelMargins = Insets(6.0, 4.0, 6.0, 4.0)

        private val TextFieldHeight = 36.0
        private val TextFieldMargins = Insets(0.0, 4.0, 12.0, 4.0)

        private val ButtonHeight = 40.0
        private val ButtonWidth = 150.0
    }


    protected val dialogService = JavaFxDialogService()


    protected val bankCode = SimpleStringProperty("")

    protected var selectedBank: BankInfo? = null

    protected val customerId = SimpleStringProperty("")

    protected val password = SimpleStringProperty("")

    protected val requiredDataHasBeenEntered = SimpleBooleanProperty(false)


    protected val checkEnteredCredentialsResult = SimpleStringProperty("")

    protected val isEnteredCredentialsResultVisible = SimpleBooleanProperty(false)


    protected val checkCredentialsButton = ProcessingIndicatorButton(messages["check"], ButtonHeight)


    init {
        bankCode.addListener { _, _, newValue -> checkIsEnteredBankCodeValid(newValue) }

        customerId.addListener { _, _, _ -> checkIfRequiredDataHasBeenEntered() }

        password.addListener { _, _, _ -> checkIfRequiredDataHasBeenEntered() }
    }


    override val root = vbox {
        prefWidth = 350.0

        label(messages["add.account.dialog.bank.code.label"]) {
            vboxConstraints {
                margin = LabelMargins
            }
        }

       textfield(bankCode) {
            prefHeight = TextFieldHeight

            vboxConstraints {
                margin = TextFieldMargins
            }
        }

        label(messages["add.account.dialog.customer.id.and.password.hint"]) {
            font = Font.font(this.font.name, FontWeight.BOLD, this.font.size + 1)

            isWrapText = true

            vboxConstraints {
                marginTop = 12.0
                marginBottom = 6.0
            }
        }

        label(messages["add.account.dialog.customer.id"]) {
            vboxConstraints {
                margin = LabelMargins
            }
        }

        textfield(customerId) {
            promptText = messages["add.account.dialog.customer.id.hint"]
            prefHeight = TextFieldHeight

            vboxConstraints {
                margin = TextFieldMargins
            }
        }

        label(messages["add.account.dialog.password"]) {
            vboxConstraints {
                margin = LabelMargins
            }
        }

        passwordfield(password) {
            promptText = messages["add.account.dialog.password.hint"]
            prefHeight = TextFieldHeight

            vboxConstraints {
                margin = TextFieldMargins
            }
        }

        label(checkEnteredCredentialsResult) {
            useMaxHeight = true

            visibleWhen(isEnteredCredentialsResultVisible)
            ensureOnlyUsesSpaceIfVisible()

            isWrapText = true
            font = Font(font.size + 1)

            setBackgroundToColor(Color.RED)

            paddingAll = 8.0

            checkEnteredCredentialsResult.addListener { _, _, _ ->
                tooltip = Tooltip(checkEnteredCredentialsResult.value)
            }

            vboxConstraints {
                marginTop = 12.0
                marginBottom = 6.0

                vGrow = Priority.ALWAYS
            }
        }

        hbox {
            alignment = Pos.CENTER_RIGHT

            button(messages["cancel"]) {
                fixedHeight = ButtonHeight
                prefWidth = ButtonWidth

                isCancelButton = true

                action { close() }

                hboxConstraints {
                    margin = Insets(6.0, 0.0, 4.0, 0.0)
                }
            }

            add(checkCredentialsButton.apply {
                prefWidth = ButtonWidth

                isDefaultButton = true

                enableWhen(requiredDataHasBeenEntered)

                action { checkEnteredCredentials() }

                hboxConstraints {
                    margin = Insets(6.0, 4.0, 4.0, 12.0)
                }
            })
        }
    }


    protected open fun checkIsEnteredBankCodeValid(enteredBankCode: String?) {
        enteredBankCode?.let {
            val banksSearchResult = presenter.searchBanksByNameBankCodeOrCity(enteredBankCode)

            // TODO: show banksSearchResult in AutoCompleteListView

            val uniqueBankCodes = banksSearchResult.map { it.bankCode }.toSet()
            selectedBank = if (uniqueBankCodes.size == 1) banksSearchResult.first() else null

            checkIfRequiredDataHasBeenEntered()
        }
    }

    protected open fun checkIfRequiredDataHasBeenEntered() {
        requiredDataHasBeenEntered.value = selectedBank != null
                && selectedBank?.supportsFinTs3_0 == true
                && customerId.value.isNotEmpty() // TODO: check if it is of length 10?
                && password.value.isNotEmpty() // TODO: check if it is of length 5?
    }


    protected open fun checkEnteredCredentials() {
        isEnteredCredentialsResultVisible.value = false

        selectedBank?.let {
            presenter.addAccountAsync(it, customerId.value, password.value) { response ->
                runLater { handleAddAccountResultOnUiThread(response) }
            }
        }
    }

    protected open fun handleAddAccountResultOnUiThread(response: AddAccountResponse) {
        checkCredentialsButton.resetIsProcessing()

        if (response.isSuccessful) {
            handleSuccessfullyAddedAccountResultOnUiThread(response)
        }
        else {
            val account = response.account

            checkEnteredCredentialsResult.value = String.format(messages["add.account.dialog.error.could.not.add.account"],
                account.bank.bankCode, account.customerId, response.errorToShowToUser)

            isEnteredCredentialsResultVisible.value = true
        }
    }

    private fun handleSuccessfullyAddedAccountResultOnUiThread(response: AddAccountResponse) {
        val message = if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan) messages["add.account.dialog.successfully.added.account.bank.supports.retrieving.transactions.of.last.90.days.without.tan"]
                      else messages["add.account.dialog.successfully.added.account"]

        val userSelection = dialogService.showDialog(Alert.AlertType.CONFIRMATION, message, null, currentStage, ButtonType.YES, ButtonType.NO)

        when (userSelection) {
            ButtonType.YES -> presenter.getAccountTransactionsAsync(response.account) { }
            else -> { } // nothing to do then, simply close dialog
        }

        close()
    }

}