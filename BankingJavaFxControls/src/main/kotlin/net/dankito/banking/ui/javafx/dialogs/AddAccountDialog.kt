package net.dankito.banking.ui.javafx.dialogs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.presenter.MainWindowPresenter
import net.dankito.fints.model.BankInfo
import net.dankito.utils.javafx.ui.controls.UpdateButton
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.ui.extensions.setBackgroundToColor
import tornadofx.*


open class AddAccountDialog(protected val presenter: MainWindowPresenter) : Window() {

    companion object {
        private val LabelMargins = Insets(6.0, 4.0, 6.0, 4.0)

        private val TextFieldHeight = 36.0
        private val TextFieldMargins = Insets(0.0, 4.0, 12.0, 4.0)

        private val ButtonHeight = 40.0
        private val ButtonWidth = 150.0
    }


    private val bankCode = SimpleStringProperty("")

    protected var selectedBank: BankInfo? = null

    private val customerId = SimpleStringProperty("")

    private val password = SimpleStringProperty("")

    private val requiredDataHasBeenEntered = SimpleBooleanProperty(false)


    private val checkEnteredCredentialsResult = SimpleStringProperty("")

    private val isEnteredCredentialsResultVisible = SimpleBooleanProperty(false)

    private val didEnteredCredentialsMatch = SimpleBooleanProperty(false)


    private val checkCredentialsButton = UpdateButton(messages["check"])


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
            visibleWhen(isEnteredCredentialsResultVisible)
            ensureOnlyUsesSpaceIfVisible()

            isWrapText = true
            font = Font(font.size + 1)

            paddingAll = 8.0

            checkEnteredCredentialsResult.addListener { _, _, _ ->
                if (didEnteredCredentialsMatch.value) {
                    setBackgroundToColor(Color.TRANSPARENT)
                }
                else {
                    setBackgroundToColor(Color.RED)
                }

                tooltip = Tooltip(checkEnteredCredentialsResult.value)

                currentWindow?.sizeToScene()
            }

            vboxConstraints {
                marginTop = 12.0
                marginBottom = 6.0
            }
        }

        hbox {
            alignment = Pos.CENTER_RIGHT

            button(messages["cancel"]) {
                prefHeight = ButtonHeight
                prefWidth = ButtonWidth

                isCancelButton = true

                action { close() }

                hboxConstraints {
                    margin = Insets(6.0, 0.0, 4.0, 0.0)
                }
            }

            add(checkCredentialsButton.apply {
                prefHeight = ButtonHeight
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
        selectedBank?.let {
            presenter.addAccountAsync(it, customerId.value, password.value) { response ->
                runLater { handleAddAccountResultOnUiThread(response) }
            }
        }
    }

    protected open fun handleAddAccountResultOnUiThread(response: AddAccountResponse) {
        isEnteredCredentialsResultVisible.value = true
        didEnteredCredentialsMatch.value = response.isSuccessful
        val account = response.account

        // TODO: in case of success show alert to ask if account transactions should get retrieved?

        val message = if (response.isSuccessful) messages["add.account.dialog.add.account.success"]
                    else String.format(messages["add.account.dialog.could.not.add.account"],
                            account.bank.bankCode, account.customerId, response.errorToShowToUser)

        checkEnteredCredentialsResult.value = message

        if (response.isSuccessful) {
            close()
        }
    }

}