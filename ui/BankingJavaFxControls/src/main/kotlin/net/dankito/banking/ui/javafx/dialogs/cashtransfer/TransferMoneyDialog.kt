package net.dankito.banking.ui.javafx.dialogs.cashtransfer

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ContentDisplay
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import kotlinx.coroutines.*
import net.dankito.banking.ui.javafx.dialogs.JavaFxDialogService
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.InputValidator
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.search.Remittee
import net.dankito.banking.ui.javafx.extensions.focusNextControl
import net.dankito.utils.javafx.ui.controls.AutoCompletionSearchTextField
import net.dankito.utils.javafx.ui.controls.autocompletionsearchtextfield
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import net.dankito.utils.javafx.ui.extensions.fixedWidth
import tornadofx.*


open class TransferMoneyDialog @JvmOverloads constructor(
    protected val presenter: BankingPresenter,
    preselectedBankAccount: BankAccount? = null,
    preselectedValues: TransferMoneyData? = null
) : Window() {

    companion object {
        private val FieldHeight = 40.0
        private val TextFieldHeight = 32.0

        private const val BankIconSize = 24.0

        private val ButtonHeight = 40.0
        private val ButtonWidth = 150.0
    }


    protected val bankAccountsSupportingTransferringMoney = FXCollections.observableArrayList(presenter.bankAccounts.filter { it.supportsTransferringMoney })

    protected val selectedBankAccount = SimpleObjectProperty<BankAccount>(preselectedBankAccount ?: bankAccountsSupportingTransferringMoney.firstOrNull())

    protected val showBankAccounts = SimpleBooleanProperty(bankAccountsSupportingTransferringMoney.size > 1)

    protected val remitteeName = SimpleStringProperty(preselectedValues?.creditorName ?: "")

    protected val remitteeIban = SimpleStringProperty(preselectedValues?.creditorIban ?: "")

    protected val remitteeBank = SimpleObjectProperty<BankInfo>()

    protected val remitteeBankName = SimpleStringProperty()

    protected val remitteeBic = SimpleStringProperty(preselectedValues?.creditorBic ?: "")

    protected val amount = SimpleDoubleProperty(preselectedValues?.amount?.toDouble() ?: 0.0)

    protected val usage = SimpleStringProperty(preselectedValues?.usage ?: "")

    protected val instantPayment = SimpleBooleanProperty(false)

    protected val supportsInstantPayment = SimpleBooleanProperty(selectedBankAccount.value?.supportsInstantPaymentMoneyTransfer ?: false)

    protected val requiredDataEntered = SimpleBooleanProperty(false)


    protected var txtfldRemitteeName: AutoCompletionSearchTextField<Remittee> by singleAssign()

    protected var lastSearchRemitteeJob: Job? = null


    protected val inputValidator = InputValidator()

    protected val dialogService = JavaFxDialogService()


    init {
        selectedBankAccount.addListener { _, _, newValue -> selectedBankAccountChanged(newValue) }

        remitteeName.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
        remitteeIban.addListener { _, _, newValue -> tryToGetBicFromIban(newValue) }
        remitteeBic.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
        amount.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
        usage.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
    }


    override val root = vbox {
        prefWidth = 650.0

        form {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }

            fieldset {
                field(messages["transfer.money.dialog.account.label"]) {
                    visibleWhen(showBankAccounts)
                    ensureOnlyUsesSpaceIfVisible()

                    combobox(selectedBankAccount, bankAccountsSupportingTransferringMoney) {
                        fixedHeight = TextFieldHeight

                        cellFormat {
                            text = it.displayNameIncludingBankName

                            it.customer.iconUrl?.let { iconUrl ->
                                graphic = ImageView(iconUrl)?.apply {
                                    this.fitHeight = BankIconSize
                                    this.fitWidth = BankIconSize
                                    this.isPreserveRatio = true
                                }
                                contentDisplay = ContentDisplay.LEFT
                            }
                            ?: run { contentDisplay = ContentDisplay.TEXT_ONLY }
                        }
                    }

                    vboxConstraints {
                        marginBottom = 12.0
                    }
                }

                field(messages["transfer.money.dialog.remittee.name.label"]) {
                    fixedHeight = FieldHeight

                    txtfldRemitteeName = autocompletionsearchtextfield(this@TransferMoneyDialog.remitteeName) {
                        fixedHeight = TextFieldHeight

                        textProperty().addListener { _, _, newValue -> searchRemittees(newValue) }

                        onAutoCompletion = { remitteeSelected(it) }
                        listCellFragment = RemitteeListCellFragment::class
                    }
                }

                field(messages["transfer.money.dialog.remittee.iban.label"]) {
                    fixedHeight = FieldHeight

                    textfield(remitteeIban) {
                        fixedHeight = TextFieldHeight

                        paddingLeft = 8.0

                        if (this@TransferMoneyDialog.remitteeName.value.isNotBlank()) {
                            runLater {
                                requestFocus()
                            }
                        }
                    }
                }

                field(messages["transfer.money.dialog.remittee.bank.label"]) {
                    fixedHeight = FieldHeight

                    textfield(remitteeBankName) {
                        fixedHeight = TextFieldHeight

                        isDisable = true

                        paddingLeft = 8.0
                    }
                }

                field(messages["transfer.money.dialog.remittee.bic.label"]) {
                    fixedHeight = FieldHeight

                    textfield(remitteeBic) {
                        fixedHeight = TextFieldHeight

                        isDisable = true

                        paddingLeft = 8.0
                    }
                }

                field(messages["transfer.money.dialog.amount.label"]) {
                    fixedHeight = FieldHeight

                    hbox {
                        alignment = Pos.CENTER_RIGHT

                        doubleTextfield(amount, false) {
                            fixedHeight = TextFieldHeight
                            fixedWidth = 100.0
                            alignment = Pos.CENTER_RIGHT

                            if (this@TransferMoneyDialog.remitteeName.value.isNotBlank() && remitteeIban.value.isNotBlank()) {
                                runLater {
                                    requestFocus()
                                }
                            }

                            hboxConstraints {
                                marginRight = 8.0
                            }
                        }

                        label(selectedBankAccount.value?.currency ?: "€")
                    }
                }

                field(messages["transfer.money.dialog.usage.label"]) {
                    fixedHeight = FieldHeight

                    textfield(usage) {
                        fixedHeight = TextFieldHeight
                    }
                }

                field {
                    fixedHeight = FieldHeight

                    checkbox(messages["transfer.money.dialog.instant.payment.label"], instantPayment) {
                        fixedHeight = TextFieldHeight

                        enableWhen(supportsInstantPayment)
                    }
                }
            }
        }

        hbox {
            alignment = Pos.CENTER_RIGHT

            button(messages["cancel"]) {
                prefHeight = ButtonHeight
                prefWidth = ButtonWidth

                isCancelButton = true

                action { cancelCashTransfer() }

                hboxConstraints {
                    margin = Insets(6.0, 0.0, 4.0, 0.0)
                }
            }

            button(messages["transfer.money.dialog.transfer.money.label"]) {
                prefHeight = ButtonHeight
                prefWidth = ButtonWidth

                isDefaultButton = true

                enableWhen(requiredDataEntered)

                action { transferMoney() }

                hboxConstraints {
                    margin = Insets(6.0, 4.0, 4.0, 12.0)
                }
            }
        }

        tryToGetBicFromIban(remitteeIban.value)
    }


    private fun selectedBankAccountChanged(newValue: BankAccount?) {
        supportsInstantPayment.value = newValue?.supportsInstantPaymentMoneyTransfer ?: false

        if (supportsInstantPayment.value == false) {
            instantPayment.value = false
        }
    }


    protected open fun searchRemittees(query: String?) {
        lastSearchRemitteeJob?.cancel()

        lastSearchRemitteeJob = GlobalScope.launch(Dispatchers.IO) {
            val potentialRemittees = presenter.findRemitteesForName(query?.toString() ?: "")

            withContext(Dispatchers.Main) {
                txtfldRemitteeName.setAutoCompleteList(potentialRemittees)
            }
        }
    }

    protected open fun remitteeSelected(remittee: Remittee) {
        txtfldRemitteeName.focusNextControl()

        remitteeName.value = remittee.name
        remitteeBic.value = remittee.bic
        remitteeIban.value = remittee.iban
    }


    protected open fun tryToGetBicFromIban(enteredIban: String) {
        presenter.findUniqueBankForIbanAsync(enteredIban) { foundBank ->
            runLater {
                showValuesForFoundBankOnUiThread(foundBank, enteredIban)
            }
        }
    }

    protected open fun showValuesForFoundBankOnUiThread(firstFoundBank: BankInfo?, enteredIban: String) {
        remitteeBank.value = firstFoundBank

        remitteeBankName.value = determineFoundBankLabel(enteredIban, firstFoundBank)

        remitteeBic.value = firstFoundBank?.bic ?: messages["transfer.money.dialog.bank.name.will.be.entered.automatically"]

        checkIfRequiredDataEnteredOnUiThread()
    }

    protected open fun determineFoundBankLabel(enteredIban: String?, bankInfo: BankInfo?): String? {
        return if (bankInfo != null) {
            return bankInfo.name + " " + bankInfo.city
        }
        else if (enteredIban.isNullOrBlank()) {
            messages["transfer.money.dialog.bank.name.will.be.entered.automatically"]
        }
        else {
            messages["transfer.money.dialog.bank.not.found.for.iban"]
        }
    }


    protected open fun checkIfRequiredDataEnteredOnUiThread() {
        requiredDataEntered.value =
            remitteeName.value.isNotBlank()
                    && inputValidator.isRemitteeNameValid(remitteeName.value) // TODO: show error message for illegal characters
                    && inputValidator.isValidIban(remitteeIban.value)
                    && inputValidator.isValidBic(remitteeBic.value)
                    && amount.value > 0
                    && inputValidator.isUsageValid(usage.value) // TODO: show error message for illegal characters
    }


    protected open fun cancelCashTransfer() {
        close()
    }

    protected open fun transferMoney() {
        remitteeBank.value?.let { remitteeBank ->
            val bankAccount = selectedBankAccount.value

            val data = TransferMoneyData(
                inputValidator.convertToAllowedSepaCharacters(remitteeName.value),
                remitteeIban.value.replace(" ", ""),
                remitteeBic.value.replace(" ", ""),
                amount.value.toBigDecimal(),
                inputValidator.convertToAllowedSepaCharacters(usage.value),
                instantPayment.value
            )

            presenter.transferMoneyAsync(bankAccount, data) {
                runLater {
                    handleTransferMoneyResultOnUiThread(bankAccount, data, it)
                }
            }
        }
    }

    protected open fun handleTransferMoneyResultOnUiThread(bankAccount: BankAccount, transferData: TransferMoneyData, response: BankingClientResponse) {
        val currency = bankAccount.currency

        if (response.isSuccessful) {
            dialogService.showInfoMessage(String.format(messages["transfer.money.dialog.message.transfer.cash.success"],
                transferData.amount, currency, transferData.creditorName), null, currentStage)
        }
        else if (response.userCancelledAction == false) {
            dialogService.showErrorMessage(String.format(messages["transfer.money.dialog.message.transfer.cash.error"],
                transferData.amount, currency, transferData.creditorName, response.errorToShowToUser), null, response.error, currentStage)
        }

        if (response.isSuccessful || response.userCancelledAction) { // do not close dialog if an error occurred
            close()
        }
    }

}