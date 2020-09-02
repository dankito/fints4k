package net.dankito.banking.ui.android.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.otaliastudios.autocomplete.Autocomplete
import kotlinx.android.synthetic.main.dialog_transfer_money.*
import kotlinx.android.synthetic.main.dialog_transfer_money.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.adapter.BankAccountsAdapter
import net.dankito.banking.ui.android.adapter.presenter.RemitteePresenter
import net.dankito.banking.ui.android.extensions.addEnterPressedListener
import net.dankito.banking.ui.android.extensions.closePopupOnBackButtonPress
import net.dankito.banking.ui.android.listener.ListItemSelectedListener
import net.dankito.banking.ui.android.util.StandardAutocompleteCallback
import net.dankito.banking.ui.android.util.StandardTextWatcher
import net.dankito.banking.search.Remittee
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.InputValidator
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.util.ValidationResult
import net.dankito.utils.multiplatform.toBigDecimal
import net.dankito.utils.android.extensions.asActivity
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import javax.inject.Inject


open class TransferMoneyDialog : DialogFragment() {

    companion object {
        val AmountFormat = NumberFormat.getCurrencyInstance()

        const val DialogTag = "TransferMoneyDialog"
    }


    protected lateinit var bankAccount: BankAccount

    protected var preselectedValues: TransferMoneyData? = null

    protected val inputValidator = InputValidator() // TODO: move to presenter


    protected var remitteeBic: String? = null


    protected var validRemitteeNameEntered = false

    protected var validRemitteeIbanEntered = false

    protected var validRemitteeBicEntered = false

    protected var validUsageEntered = true

    protected var validAmountEntered = false

    protected var didJustCorrectInput = mutableMapOf<TextInputLayout, Boolean>()


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)
    }


    open fun show(activity: AppCompatActivity, fullscreen: Boolean = false) {
        show(activity, null, fullscreen)
    }

    open fun show(activity: AppCompatActivity, preselectedValues: TransferMoneyData?, fullscreen: Boolean = false) {
        this.preselectedValues = preselectedValues

        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.FloatingDialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_transfer_money, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        val allBankAccountsSupportingTransferringMoney = presenter.bankAccountsSupportingTransferringMoney
        bankAccount = preselectedValues?.account ?: allBankAccountsSupportingTransferringMoney.first()

        if (allBankAccountsSupportingTransferringMoney.size > 1) {
            rootView.lytSelectBankAccount.visibility = View.VISIBLE

            val adapter = BankAccountsAdapter(allBankAccountsSupportingTransferringMoney)
            rootView.spnBankAccounts.adapter = adapter
            rootView.spnBankAccounts.onItemSelectedListener = ListItemSelectedListener(adapter) { selectedBankAccount ->
                this.bankAccount = selectedBankAccount
                setInstantPaymentControlsVisibility(rootView)
            }
            preselectedValues?.account?.let { rootView.spnBankAccounts.setSelection(adapter.getItems().indexOf(it)) }
        }

        initRemitteeAutocompletion(rootView.edtxtRemitteeName)

        rootView.edtxtRemitteeName.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredRemitteeNameIsValidWhileUserIsTyping()
        })

        rootView.edtxtRemitteeIban.addTextChangedListener(StandardTextWatcher {
            checkIfEnteredRemitteeIbanIsValidWhileUserIsTyping()
            tryToGetBicFromIban(it)
        })

        rootView.edtxtAmount.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredAmountIsValid()
        })
        rootView.edtxtUsage.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredUsageTextIsValid()
        })

        rootView.edtxtRemitteeName.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRemitteeNameIsValidAfterFocusLost() }
        rootView.edtxtRemitteeIban.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRemitteeIbanIsValidAfterFocusLost() }
        rootView.edtxtAmount.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredAmountIsValid() }
        rootView.edtxtUsage.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredUsageTextIsValid() }

        transferMoneyIfEnterPressed(rootView.edtxtRemitteeName)
        transferMoneyIfEnterPressed(rootView.edtxtRemitteeIban)
        transferMoneyIfEnterPressed(rootView.edtxtAmount)
        transferMoneyIfEnterPressed(rootView.edtxtUsage)

        // fix that even in Locales using ',' as decimal separator entering ',' is not allowed (thanks dstibbe! https://stackoverflow.com/a/34256139)
        val decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator()
        rootView.edtxtAmount.keyListener = DigitsKeyListener.getInstance("0123456789$decimalSeparator")

        setInstantPaymentControlsVisibility(rootView)

        rootView.btnCancel.setOnClickListener { dismiss() }

        rootView.btnTransferMoney.setOnClickListener { transferMoney() }
    }

    private fun setInstantPaymentControlsVisibility(rootView: View) {
        rootView.chkbxInstantPayment.visibility =
            if (bankAccount.supportsInstantPaymentMoneyTransfer) {
                View.VISIBLE
            }
            else {
                View.GONE
            }
    }

    private fun transferMoneyIfEnterPressed(editText: EditText) {
        editText.addEnterPressedListener {
            if (isRequiredDataEntered()) {
                transferMoney()

                return@addEnterPressedListener true
            }

            false
        }
    }

    private fun isRequiredDataEntered() = btnTransferMoney.isEnabled

    private fun initRemitteeAutocompletion(edtxtRemitteeName: EditText) {
        val autocompleteCallback = StandardAutocompleteCallback<Remittee> { _, item ->
            remitteeSelected(item)
            true
        }

        Autocomplete.on<Remittee>(edtxtRemitteeName)
            .with(6f)
            .with(ColorDrawable(Color.WHITE))
            .with(autocompleteCallback)
            .with(RemitteePresenter(presenter, edtxtRemitteeName.context))
            .build()
            .closePopupOnBackButtonPress(dialog)
    }


    override fun onStart() {
        super.onStart()

        setPreselectedValues()

        if (remitteeBic != null) {
            tryToGetBicFromIban(edtxtRemitteeIban.text.toString())
        }
    }


    protected open fun setPreselectedValues() {
        preselectedValues?.let { data ->
            preselectedValues = null

            edtxtRemitteeName.setText(data.creditorName)

            if (data.creditorIban.isNotBlank()) { // set only if creditorIban has a value as otherwise creditorBic would be overridden by empty search result
                edtxtRemitteeIban.setText(data.creditorIban)
            }

            // a little bit inconsistent as if IBAN is not set bank's name won't be displayed even though it can be retrieved by BIC
            remitteeBic = data.creditorBic

            if (data.amount > BigDecimal.ZERO) {
                edtxtAmount.setText(AmountFormat.format(data.amount))
            }

            focusEditTextAccordingToPreselectedValues(data)
        }
    }

    protected open fun focusEditTextAccordingToPreselectedValues(data: TransferMoneyData) {
        if (data.creditorName.trim().isNotEmpty()) {
            if (data.creditorIban.trim().isNotEmpty()) {
                edtxtAmount.requestFocus()
            }
            else {
                edtxtRemitteeIban.requestFocus()
            }
        }
    }


    protected open fun remitteeSelected(item: Remittee) {
        edtxtRemitteeName.setText(item.name)
        edtxtRemitteeIban.setText(item.iban)
        remitteeBic = item.bic
    }

    protected open fun transferMoney() {
        getEnteredAmount()?.let { amount -> // should only come at this stage when a valid amount has been entered
            val data = TransferMoneyData(
                bankAccount,
                inputValidator.convertToAllowedSepaCharacters(edtxtRemitteeName.text.toString()),
                edtxtRemitteeIban.text.toString().replace(" ", ""),
                remitteeBic?.replace(" ", "") ?: "", // should always be != null at this point
                amount.toBigDecimal(),
                inputValidator.convertToAllowedSepaCharacters(edtxtUsage.text.toString()),
                chkbxInstantPayment.isChecked
            )

            presenter.transferMoneyAsync(data) {
                context?.asActivity()?.runOnUiThread {
                    handleTransferMoneyResultOnUiThread(data, it)
                }
            }
        }
    }

    protected open fun handleTransferMoneyResultOnUiThread(transferData: TransferMoneyData, response: BankingClientResponse) {
        context?.let { context ->
            if (response.userCancelledAction == false) {
                val message = if (response.isSuccessful) {
                    context.getString(R.string.dialog_transfer_money_message_transfer_successful,
                        String.format("%.02f", transferData.amount), "€", transferData.creditorName) // TODO: where to get currency from?
                }
                else {
                    context.getString(R.string.dialog_transfer_money_message_transfer_failed,
                        String.format("%.02f", transferData.amount), "€", transferData.creditorName, // TODO: where to get currency from?
                        response.errorToShowToUser
                    )
                }

                AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
            }

            if (response.isSuccessful || response.userCancelledAction) { // do not close dialog if an error occurred
                this.dismiss()
            }
        }
    }


    protected fun checkRequiredDataWatcher(additionalCheck: (() -> Unit)? = null): TextWatcher {
        return StandardTextWatcher {
            additionalCheck?.invoke()

            checkIfRequiredDataEnteredOnUiThread()
        }
    }

    protected open fun tryToGetBicFromIban(enteredIban: CharSequence) {
        presenter.findUniqueBankForIbanAsync(enteredIban.toString()) { foundBank ->
            context?.asActivity()?.runOnUiThread {
                showValuesForFoundBankOnUiThread(enteredIban, foundBank)
            }
        }
    }

    private fun showValuesForFoundBankOnUiThread(enteredIban: CharSequence, foundBank: BankInfo?) {
        validRemitteeBicEntered = foundBank != null
        remitteeBic = foundBank?.bic

        if (foundBank != null) {
            txtRemitteeBankInfo.text = getString(R.string.dialog_transfer_money_bic_detected_from_iban, foundBank.bic, foundBank.name)
            txtRemitteeBankInfo.visibility = View.VISIBLE
        }
        else if (enteredIban.length >= InputValidator.MinimumLengthToDetermineBicFromIban) {
            txtRemitteeBankInfo.text = getString(R.string.dialog_transfer_money_could_not_determine_bic_from_iban, enteredIban.substring(4, InputValidator.MinimumLengthToDetermineBicFromIban))
            txtRemitteeBankInfo.visibility = View.VISIBLE
        }
        else {
            txtRemitteeBankInfo.visibility = View.GONE
        }

        checkIfRequiredDataEnteredOnUiThread()
    }

    protected open fun checkIfRequiredDataEnteredOnUiThread() {
        btnTransferMoney.isEnabled = validRemitteeNameEntered && validRemitteeIbanEntered
                && validRemitteeBicEntered
                && validAmountEntered && validUsageEntered
    }

    protected open fun checkIfEnteredRemitteeNameIsValidWhileUserIsTyping() {
        val enteredRemitteeName = edtxtRemitteeName.text.toString()
        val validationResult = inputValidator.validateRemitteeNameWhileTyping(enteredRemitteeName)

        this.validRemitteeNameEntered = validationResult.validationSuccessfulOrCouldCorrectString

        showValidationResult(lytRemitteeName, validationResult)
    }

    protected open fun checkIfEnteredRemitteeNameIsValidAfterFocusLost() {
        val enteredRemitteeName = edtxtRemitteeName.text.toString()
        val validationResult = inputValidator.validateRemitteeName(enteredRemitteeName)

        this.validRemitteeNameEntered = validationResult.validationSuccessfulOrCouldCorrectString

        if (validationResult.validationSuccessful == false) { // only update hint / error if validation fails, don't hide previous hint / error otherwise
            showValidationResult(lytRemitteeName, validationResult)
        }
    }

    protected open fun checkIfEnteredRemitteeIbanIsValidWhileUserIsTyping() {
        val enteredIban = edtxtRemitteeIban.text.toString()
        val validationResult = inputValidator.validateIbanWhileTyping(enteredIban)

        this.validRemitteeIbanEntered = validationResult.validationSuccessfulOrCouldCorrectString

        showValidationResult(lytRemitteeIban, validationResult)
    }

    protected open fun checkIfEnteredRemitteeIbanIsValidAfterFocusLost() {
        val validationResult = inputValidator.validateIban(edtxtRemitteeIban.text.toString())

        this.validRemitteeIbanEntered = validationResult.validationSuccessfulOrCouldCorrectString

        if (validationResult.validationSuccessful == false) { // only update hint / error if validation fails, don't hide previous hint / error otherwise
            showValidationResult(lytRemitteeIban, validationResult)
        }
    }

    protected open fun checkIfEnteredAmountIsValid() {
        val validationResult = inputValidator.validateAmount(edtxtAmount.text.toString())

        this.validAmountEntered = validationResult.validationSuccessfulOrCouldCorrectString

        showValidationResult(lytAmount, validationResult)
    }

    protected open fun getEnteredAmount(): BigDecimal? {
        try {
            val amountString = edtxtAmount.text.toString().replace(',', '.')

            return amountString.toBigDecimal()
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun checkIfEnteredUsageTextIsValid() {
        val validationResult = inputValidator.validateUsage(edtxtUsage.text.toString())

        this.validUsageEntered = validationResult.validationSuccessfulOrCouldCorrectString

        showValidationResult(lytUsage, validationResult)
    }

    protected open fun showValidationResult(textInputLayout: TextInputLayout, validationResult: ValidationResult) {
        if (didJustCorrectInput.containsKey(textInputLayout)) { // we have just auto corrected TextInputLayout's EditText's text below, don't overwrite its displayed hints and error
            return
        }

        if (validationResult.didCorrectString) {
            textInputLayout.editText?.let { editText ->
                val selectionStart = editText.selectionStart
                val selectionEnd = editText.selectionEnd
                val lengthDiff = validationResult.correctedInputString.length - validationResult.inputString.length

                didJustCorrectInput.put(textInputLayout, true)

                editText.setText(validationResult.correctedInputString)

                if (validationResult.correctedInputString.isNotEmpty()) {
                    editText.setSelection(selectionStart + lengthDiff, selectionEnd + lengthDiff)
                }

                didJustCorrectInput.remove(textInputLayout)
            }
        }

        textInputLayout.error = validationResult.validationError
        if (validationResult.validationError == null) { // don't overwrite error text
            textInputLayout.helperText = validationResult.validationHint
        }
    }

}