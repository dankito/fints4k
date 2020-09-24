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
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.InputValidator
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.search.TransactionParty
import net.dankito.utils.multiplatform.toBigDecimal
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
    preselectedValues: TransferMoneyData? = null
) : Window() {

    companion object {
        private val FieldHeight = 40.0
        private val TextFieldHeight = 32.0

        private const val BankIconSize = 24.0

        private val ButtonHeight = 40.0
        private val ButtonWidth = 150.0
    }


    protected val bankAccountsSupportingTransferringMoney = FXCollections.observableArrayList(presenter.allAccounts.filter { it.supportsTransferringMoney })

    protected val selectedBankAccount = SimpleObjectProperty<TypedBankAccount>(preselectedValues?.account ?: bankAccountsSupportingTransferringMoney.firstOrNull())

    protected val showBankAccounts = SimpleBooleanProperty(bankAccountsSupportingTransferringMoney.size > 1)

    protected val recipientName = SimpleStringProperty(preselectedValues?.recipientName ?: "")

    protected val recipientIban = SimpleStringProperty(preselectedValues?.recipientAccountId ?: "")

    protected val recipientBank = SimpleObjectProperty<BankInfo>()

    protected val recipientBankName = SimpleStringProperty()

    protected val recipientBic = SimpleStringProperty(preselectedValues?.recipientBankCode ?: "")

    protected val amount = SimpleDoubleProperty(preselectedValues?.amount?.toDouble() ?: 0.0)

    protected val reference = SimpleStringProperty(preselectedValues?.reference ?: "")

    protected val realTimeTransfer = SimpleBooleanProperty(false)

    protected val supportsRealTimeTransfer
            = SimpleBooleanProperty(selectedBankAccount.value?.supportsRealTimeTransfer ?: false)

    protected val requiredDataEntered = SimpleBooleanProperty(false)


    protected var txtfldRecipientName: AutoCompletionSearchTextField<TransactionParty> by singleAssign()

    protected var lastSearchRecipientJob: Job? = null


    protected val inputValidator = InputValidator()

    protected val dialogService = JavaFxDialogService()


    init {
        selectedBankAccount.addListener { _, _, newValue -> selectedBankAccountChanged(newValue) }

        recipientName.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
        recipientIban.addListener { _, _, newValue -> tryToGetBicFromIban(newValue) }
        recipientBic.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
        amount.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
        reference.addListener { _, _, _ -> checkIfRequiredDataEnteredOnUiThread() }
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
                            text = it.displayName

                            it.bank.iconUrl?.let { iconUrl ->
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

                field(messages["transfer.money.dialog.recipient.name.label"]) {
                    fixedHeight = FieldHeight

                    txtfldRecipientName = autocompletionsearchtextfield(this@TransferMoneyDialog.recipientName) {
                        fixedHeight = TextFieldHeight

                        textProperty().addListener { _, _, newValue -> searchRecipients(newValue) }

                        onAutoCompletion = { recipientSelected(it) }
                        listCellFragment = RecipientListCellFragment::class

                        setPrefItemHeight(RecipientListCellFragment.ItemHeight)
                    }
                }

                field(messages["transfer.money.dialog.recipient.iban.label"]) {
                    fixedHeight = FieldHeight

                    textfield(recipientIban) {
                        fixedHeight = TextFieldHeight

                        paddingLeft = 8.0

                        if (this@TransferMoneyDialog.recipientName.value.isNotBlank()) {
                            runLater {
                                requestFocus()
                            }
                        }
                    }
                }

                field(messages["transfer.money.dialog.recipient.bank.label"]) {
                    fixedHeight = FieldHeight

                    textfield(recipientBankName) {
                        fixedHeight = TextFieldHeight

                        isDisable = true

                        paddingLeft = 8.0
                    }
                }

                field(messages["transfer.money.dialog.recipient.bic.label"]) {
                    fixedHeight = FieldHeight

                    textfield(recipientBic) {
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

                            if (this@TransferMoneyDialog.recipientName.value.isNotBlank() && recipientIban.value.isNotBlank()) {
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

                field(messages["transfer.money.dialog.reference.label"]) {
                    fixedHeight = FieldHeight

                    textfield(reference) {
                        fixedHeight = TextFieldHeight
                    }
                }

                field {
                    fixedHeight = FieldHeight

                    checkbox(messages["transfer.money.dialog.real.time.transfer.label"], realTimeTransfer) {
                        fixedHeight = TextFieldHeight

                        enableWhen(supportsRealTimeTransfer)
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

        tryToGetBicFromIban(recipientIban.value)
    }


    private fun selectedBankAccountChanged(newValue: TypedBankAccount?) {
        supportsRealTimeTransfer.value = newValue?.supportsRealTimeTransfer ?: false

        if (supportsRealTimeTransfer.value == false) {
            realTimeTransfer.value = false
        }
    }


    protected open fun searchRecipients(query: String?) {
        lastSearchRecipientJob?.cancel()

        lastSearchRecipientJob = GlobalScope.launch(Dispatchers.IO) {
            val potentialRecipients = presenter.findRecipientsForName(query?.toString() ?: "")

            withContext(Dispatchers.Main) {
                txtfldRecipientName.setAutoCompleteList(potentialRecipients)
            }
        }
    }

    protected open fun recipientSelected(transactionParty: TransactionParty) {
        txtfldRecipientName.focusNextControl()

        recipientName.value = transactionParty.name
        recipientBic.value = transactionParty.bic
        recipientIban.value = transactionParty.iban
    }


    protected open fun tryToGetBicFromIban(enteredIban: String) {
        presenter.findUniqueBankForIbanAsync(enteredIban) { foundBank ->
            runLater {
                showValuesForFoundBankOnUiThread(foundBank, enteredIban)
            }
        }
    }

    protected open fun showValuesForFoundBankOnUiThread(firstFoundBank: BankInfo?, enteredIban: String) {
        recipientBank.value = firstFoundBank

        recipientBankName.value = determineFoundBankLabel(enteredIban, firstFoundBank)

        recipientBic.value = firstFoundBank?.bic ?: messages["transfer.money.dialog.bank.name.will.be.entered.automatically"]

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
            recipientName.value.isNotBlank()
                    && inputValidator.isRecipientNameValid(recipientName.value) // TODO: show error message for illegal characters
                    && inputValidator.isValidIban(recipientIban.value)
                    && inputValidator.isValidBic(recipientBic.value)
                    && amount.value > 0
                    && inputValidator.isReferenceValid(reference.value) // TODO: show error message for illegal characters
    }


    protected open fun cancelCashTransfer() {
        close()
    }

    protected open fun transferMoney() {
        recipientBank.value?.let {
            val account = selectedBankAccount.value

            val data = TransferMoneyData(
                account,
                inputValidator.convertToAllowedSepaCharacters(recipientName.value),
                recipientIban.value.replace(" ", ""),
                recipientBic.value.replace(" ", ""),
                amount.value.toBigDecimal().toBigDecimal(),
                inputValidator.convertToAllowedSepaCharacters(reference.value),
                realTimeTransfer.value
            )

            presenter.transferMoneyAsync(data) {
                runLater {
                    handleTransferMoneyResultOnUiThread(account, data, it)
                }
            }
        }
    }

    protected open fun handleTransferMoneyResultOnUiThread(account: TypedBankAccount, transferData: TransferMoneyData, response: BankingClientResponse) {
        val currency = account.currency

        if (response.successful) {
            dialogService.showInfoMessage(String.format(messages["transfer.money.dialog.message.transfer.cash.success"],
                transferData.amount, currency, transferData.recipientName), null, currentStage)
        }
        else if (response.userCancelledAction == false) {
            dialogService.showErrorMessage(String.format(messages["transfer.money.dialog.message.transfer.cash.error"],
                transferData.amount, currency, transferData.recipientName, response.errorToShowToUser), null, null, currentStage)
        }

        if (response.successful || response.userCancelledAction) { // do not close dialog if an error occurred
            close()
        }
    }

}