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
import kotlinx.coroutines.*
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.ui.javafx.dialogs.addaccount.BankInfoListCellFragment
import net.dankito.banking.ui.javafx.extensions.focusNextControl
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.javafx.ui.controls.AutoCompletionSearchTextField
import net.dankito.utils.javafx.ui.controls.ProcessingIndicatorButton
import net.dankito.utils.javafx.ui.controls.autocompletionsearchtextfield
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


    protected val bankName = SimpleStringProperty("")

    protected var txtfldBank: AutoCompletionSearchTextField<BankInfo> by singleAssign()

    protected var selectedBank: BankInfo? = null

    protected var lastSearchBanksJob: Job? = null

    protected val customerId = SimpleStringProperty("")

    protected val password = SimpleStringProperty("")

    protected val requiredDataHasBeenEntered = SimpleBooleanProperty(false)


    protected val checkEnteredCredentialsResult = SimpleStringProperty("")

    protected val isEnteredCredentialsResultVisible = SimpleBooleanProperty(false)


    protected val checkCredentialsButton = ProcessingIndicatorButton(messages["check"], ButtonHeight)


    init {
        bankName.addListener { _, _, newValue -> searchBanks(newValue) }

        customerId.addListener { _, _, _ -> checkIfRequiredDataHasBeenEntered() }

        password.addListener { _, _, _ -> checkIfRequiredDataHasBeenEntered() }
    }


    override val root = vbox {
        prefWidth = 350.0

        label(messages["add.account.dialog.bank.label"]) {
            vboxConstraints {
                margin = LabelMargins
            }
        }

        txtfldBank = autocompletionsearchtextfield(bankName) {
            prefHeight = TextFieldHeight

            onAutoCompletion = { bankSelected(it) }
            listCellFragment = BankInfoListCellFragment::class

            setPrefItemHeight(BankInfoListCellFragment.ItemHeight)

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


    protected open fun searchBanks(query: String?) {
        lastSearchBanksJob?.cancel()

        lastSearchBanksJob = GlobalScope.launch(Dispatchers.IO) {
            val filteredBanks = presenter.searchBanksByNameBankCodeOrCity(query)

            withContext(Dispatchers.Main) {
                txtfldBank.setAutoCompleteList(filteredBanks)

                checkIfRequiredDataHasBeenEntered()
            }
        }
    }

    protected open fun bankSelected(bank: BankInfo) {
        txtfldBank.focusNextControl()

        selectedBank = bank

        bankName.value = bank.name

        checkIfRequiredDataHasBeenEntered()

        if (bank.supportsFinTs3_0 == false) {
            showBankDoesNotSupportFinTs30ErrorMessage(bank)
        }
    }

    protected open fun showBankDoesNotSupportFinTs30ErrorMessage(bank: BankInfo) {
        val errorMessage = String.format(messages["add.account.dialog.error.bank.does.not.support.fints.3.error.message"], bank.name)

        JavaFxDialogService().showErrorMessageOnUiThread(errorMessage)
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

        if (response.successful) {
            handleSuccessfullyAddedAccountResultOnUiThread(response)
        }
        else {
            val account = response.customer

            checkEnteredCredentialsResult.value = String.format(messages["add.account.dialog.error.could.not.add.account"],
                account.bankCode, account.customerId, response.errorToShowToUser)

            isEnteredCredentialsResultVisible.value = true
        }
    }

    private fun handleSuccessfullyAddedAccountResultOnUiThread(response: AddAccountResponse) {
        // TODO: remove this message and display a button to load all transactions
        val message = if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan) messages["add.account.dialog.successfully.added.account.bank.supports.retrieving.transactions.of.last.90.days.without.tan"]
                      else messages["add.account.dialog.successfully.added.account"]

        val userSelection = dialogService.showDialog(Alert.AlertType.CONFIRMATION, message, null, currentStage, ButtonType.YES, ButtonType.NO)

        when (userSelection) {
            ButtonType.YES -> presenter.fetchAllAccountTransactionsAsync(response.customer) { }
            else -> { } // nothing to do then, simply close dialog
        }

        close()
    }

}