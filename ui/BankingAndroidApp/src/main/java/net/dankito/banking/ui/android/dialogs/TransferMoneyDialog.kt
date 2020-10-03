package net.dankito.banking.ui.android.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.otaliastudios.autocomplete.Autocomplete
import kotlinx.android.synthetic.main.dialog_transfer_money.*
import kotlinx.android.synthetic.main.dialog_transfer_money.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.adapter.IconedBankAccountsAdapter
import net.dankito.banking.ui.android.adapter.presenter.RecipientPresenter
import net.dankito.banking.ui.android.extensions.addEnterPressedListener
import net.dankito.banking.ui.android.extensions.closePopupOnBackButtonPress
import net.dankito.banking.ui.android.listener.ListItemSelectedListener
import net.dankito.banking.ui.android.util.StandardAutocompleteCallback
import net.dankito.banking.ui.android.util.StandardTextWatcher
import net.dankito.banking.search.TransactionParty
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.InputValidator
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.ui.android.extensions.isEllipsized
import net.dankito.banking.ui.android.views.InfoPopupWindow
import net.dankito.banking.util.ValidationResult
import net.dankito.utils.multiplatform.toBigDecimal
import net.dankito.utils.android.extensions.asActivity
import net.dankito.utils.android.extensions.getDimension
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


open class TransferMoneyDialog : DialogFragment() {

    companion object {
        const val DialogTag = "TransferMoneyDialog"
    }


    protected lateinit var account: TypedBankAccount

    protected var preselectedValues: TransferMoneyData? = null

    protected val inputValidator = InputValidator() // TODO: move to presenter


    protected var recipientBic: String? = null


    protected var validRecipientNameEntered = false

    protected var validRecipientIbanEntered = false

    protected var validRecipientBicEntered = false

    protected var validReferenceEntered = true

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
        val accountsSupportingTransferringMoney = presenter.accountsSupportingTransferringMoneySortedByDisplayIndex
        account = preselectedValues?.account ?: accountsSupportingTransferringMoney.first()

        if (accountsSupportingTransferringMoney.size > 1) {
            rootView.lytSelectBankAccount.visibility = View.VISIBLE

            val adapter = IconedBankAccountsAdapter(accountsSupportingTransferringMoney)
            rootView.spnBankAccounts.adapter = adapter
            rootView.spnBankAccounts.onItemSelectedListener = ListItemSelectedListener(adapter) { selectedBankAccount ->
                this.account = selectedBankAccount
                setRealTimeTransferControlsVisibility(rootView)
            }
            preselectedValues?.account?.let { rootView.spnBankAccounts.setSelection(adapter.getItems().indexOf(it)) }
        }

        initRecipientAutocompletion(rootView.edtxtRecipientName)

        rootView.edtxtRecipientName.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredRecipientNameIsValidWhileUserIsTyping()
        })

        rootView.edtxtRecipientIban.addTextChangedListener(StandardTextWatcher {
            checkIfEnteredRecipientIbanIsValidWhileUserIsTyping()
            tryToGetBicFromIban(it)
        })

        rootView.edtxtAmount.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredAmountIsValid()
        })
        rootView.edtxtReference.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredReferenceTextIsValid()
        })

        rootView.edtxtRecipientName.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRecipientNameIsValidAfterFocusLost() }
        rootView.edtxtRecipientIban.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRecipientIbanIsValidAfterFocusLost() }
        rootView.edtxtAmount.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredAmountIsValid() }
        rootView.edtxtReference.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredReferenceTextIsValid() }

        transferMoneyIfEnterPressed(rootView.edtxtRecipientName)
        transferMoneyIfEnterPressed(rootView.edtxtRecipientIban)
        transferMoneyIfEnterPressed(rootView.edtxtAmount)
        transferMoneyIfEnterPressed(rootView.edtxtReference)

        // fix that even in Locales using ',' as decimal separator entering ',' is not allowed (thanks dstibbe! https://stackoverflow.com/a/34256139)
        val decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator()
        rootView.edtxtAmount.keyListener = DigitsKeyListener.getInstance("0123456789$decimalSeparator")

        rootView.btnShowRealTimeTransferInfo.setOnClickListener { showRealTimeTransferInfo(rootView.btnShowRealTimeTransferInfo) }

        setRealTimeTransferControlsVisibility(rootView)

        rootView.btnCancel.setOnClickListener { dismiss() }

        rootView.btnTransferMoney.setOnClickListener { transferMoney() }

        adjustCheckBoxRealTimeTransferWidth()
    }


    protected open fun adjustCheckBoxRealTimeTransferWidth() {
        // wait some time till CheckBox is layout and lineCount is set
        val timer = Timer()
        timer.schedule(10) { requireActivity().runOnUiThread { adjustCheckBoxRealTimeTransferWidthOnUiThread() }}
        timer.schedule(2500) { requireActivity().runOnUiThread { adjustCheckBoxRealTimeTransferWidthOnUiThread() }}
    }

    protected open fun adjustCheckBoxRealTimeTransferWidthOnUiThread() {
        if (chkbxRealTimeTransfer.isEllipsized == false) {
            // by default chkbxRealTimeTransfer uses full width, even though if its text doesn't need this space -> there
            chkbxRealTimeTransfer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f)
            chkbxRealTimeTransfer.requestLayout()
        }
    }

    protected open fun setRealTimeTransferControlsVisibility(rootView: View) {
        rootView.lytRealTimeTransfer.visibility =
            if (account.supportsRealTimeTransfer) {
                View.VISIBLE
            }
            else {
                View.GONE
            }
    }

    protected open fun showRealTimeTransferInfo(btnShowRealTimeTransferInfo: ImageButton) {
        InfoPopupWindow(requireActivity(), R.string.dialog_transfer_money_real_time_transfer_info).show(btnShowRealTimeTransferInfo)
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

    private fun initRecipientAutocompletion(edtxtRecipientName: EditText) {
        val autocompleteCallback = StandardAutocompleteCallback<TransactionParty> { _, item ->
            recipientSelected(item)
            true
        }

        Autocomplete.on<TransactionParty>(edtxtRecipientName)
            .with(6f)
            .with(ColorDrawable(Color.WHITE))
            .with(autocompleteCallback)
            .with(RecipientPresenter(presenter, edtxtRecipientName.context))
            .build()
            .closePopupOnBackButtonPress(dialog)
    }


    override fun onStart() {
        super.onStart()

        setPreselectedValues()

        if (recipientBic != null) {
            tryToGetBicFromIban(edtxtRecipientIban.text.toString())
        }
    }


    protected open fun setPreselectedValues() {
        preselectedValues?.let { data ->
            preselectedValues = null

            edtxtRecipientName.setText(data.recipientName)

            if (data.recipientAccountId.isNotBlank()) { // set only if recipientAccountId has a value as otherwise recipientBankCode would be overridden by empty search result
                edtxtRecipientIban.setText(data.recipientAccountId)
            }

            // a little bit inconsistent as if IBAN is not set bank's name won't be displayed even though it can be retrieved by BIC
            recipientBic = data.recipientBankCode

            if (data.amount > BigDecimal.ZERO) {
                edtxtAmount.setText(data.amount.toString())
            }

            edtxtReference.setText(data.reference)

            focusEditTextAccordingToPreselectedValues()
        }
    }

    protected open fun focusEditTextAccordingToPreselectedValues() {
        when {
            edtxtRecipientName.text.toString().isBlank() -> edtxtRecipientName.requestFocus()
            edtxtRecipientIban.text.toString().isBlank() -> edtxtRecipientIban.requestFocus()
            edtxtAmount.text.toString().isBlank() -> edtxtAmount.requestFocus()
            edtxtReference.text.toString().isBlank() -> edtxtReference.requestFocus()
            else -> edtxtReference.requestFocus()
        }
    }


    protected open fun recipientSelected(item: TransactionParty) {
        edtxtRecipientName.setText(item.name)
        edtxtRecipientIban.setText(item.iban)
        recipientBic = item.bic

        focusEditTextAccordingToPreselectedValues()
    }

    protected open fun transferMoney() {
        getEnteredAmount()?.let { amount -> // should only come at this stage when a valid amount has been entered
            val data = TransferMoneyData(
                account,
                inputValidator.convertToAllowedSepaCharacters(edtxtRecipientName.text.toString()),
                edtxtRecipientIban.text.toString().replace(" ", ""),
                recipientBic?.replace(" ", "") ?: "", // should always be != null at this point
                amount.toBigDecimal(),
                inputValidator.convertToAllowedSepaCharacters(edtxtReference.text.toString()),
                chkbxRealTimeTransfer.isChecked
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
                val message = if (response.successful) {
                    context.getString(R.string.dialog_transfer_money_message_transfer_successful,
                        String.format("%.02f", transferData.amount), "€", transferData.recipientName) // TODO: where to get currency from?
                }
                else {
                    context.getString(R.string.dialog_transfer_money_message_transfer_failed,
                        String.format("%.02f", transferData.amount), "€", transferData.recipientName, // TODO: where to get currency from?
                        response.errorToShowToUser
                    )
                }

                AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
            }

            if (response.successful || response.userCancelledAction) { // do not close dialog if an error occurred
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
        validRecipientBicEntered = foundBank != null
        recipientBic = foundBank?.bic

        if (foundBank != null) {
            txtRecipientBankInfo.text = getString(R.string.dialog_transfer_money_bic_detected_from_iban, foundBank.bic, foundBank.name)
            txtRecipientBankInfo.visibility = View.VISIBLE
        }
        else if (enteredIban.length >= InputValidator.MinimumLengthToDetermineBicFromIban) {
            txtRecipientBankInfo.text = getString(R.string.dialog_transfer_money_could_not_determine_bic_from_iban, enteredIban.substring(4, InputValidator.MinimumLengthToDetermineBicFromIban))
            txtRecipientBankInfo.visibility = View.VISIBLE
        }
        else {
            txtRecipientBankInfo.visibility = View.GONE
        }

        checkIfRequiredDataEnteredOnUiThread()
    }

    protected open fun checkIfRequiredDataEnteredOnUiThread() {
        btnTransferMoney.isEnabled = validRecipientNameEntered && validRecipientIbanEntered
                && validRecipientBicEntered
                && validAmountEntered && validReferenceEntered
    }

    protected open fun checkIfEnteredRecipientNameIsValidWhileUserIsTyping() {
        val enteredRecipientName = edtxtRecipientName.text.toString()
        val validationResult = inputValidator.validateRecipientNameWhileTyping(enteredRecipientName)

        this.validRecipientNameEntered = validationResult.validationSuccessfulOrCouldCorrectString

        showValidationResult(lytRecipientName, validationResult)
    }

    protected open fun checkIfEnteredRecipientNameIsValidAfterFocusLost() {
        val enteredRecipientName = edtxtRecipientName.text.toString()
        val validationResult = inputValidator.validateRecipientName(enteredRecipientName)

        this.validRecipientNameEntered = validationResult.validationSuccessfulOrCouldCorrectString

        if (validationResult.validationSuccessful == false) { // only update hint / error if validation fails, don't hide previous hint / error otherwise
            showValidationResult(lytRecipientName, validationResult)
        }
    }

    protected open fun checkIfEnteredRecipientIbanIsValidWhileUserIsTyping() {
        val enteredIban = edtxtRecipientIban.text.toString()
        val validationResult = inputValidator.validateIbanWhileTyping(enteredIban)

        this.validRecipientIbanEntered = validationResult.validationSuccessfulOrCouldCorrectString

        showValidationResult(lytRecipientIban, validationResult)
    }

    protected open fun checkIfEnteredRecipientIbanIsValidAfterFocusLost() {
        val validationResult = inputValidator.validateIban(edtxtRecipientIban.text.toString())

        this.validRecipientIbanEntered = validationResult.validationSuccessfulOrCouldCorrectString

        if (validationResult.validationSuccessful == false) { // only update hint / error if validation fails, don't hide previous hint / error otherwise
            showValidationResult(lytRecipientIban, validationResult)
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

    protected open fun checkIfEnteredReferenceTextIsValid() {
        val validationResult = inputValidator.validateReference(edtxtReference.text.toString())

        this.validReferenceEntered = validationResult.validationSuccessfulOrCouldCorrectString

        showValidationResult(lytReference, validationResult)
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

        (textInputLayout.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            val isShowingHintOrError = validationResult.validationError != null || validationResult.validationHint != null
            params.bottomMargin = if (isShowingHintOrError == false || textInputLayout == lytReference) 0
                                    else context!!.getDimension(R.dimen.dialog_transfer_money_input_fields_bottom_margin_when_displaying_validation_label)
        }
    }

}