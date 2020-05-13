package net.dankito.banking.fints4java.android.ui.dialogs

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
import com.otaliastudios.autocomplete.Autocomplete
import kotlinx.android.synthetic.main.dialog_transfer_money.*
import kotlinx.android.synthetic.main.dialog_transfer_money.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.di.BankingComponent
import net.dankito.banking.fints4java.android.ui.adapter.BankAccountsAdapter
import net.dankito.banking.fints4java.android.ui.adapter.presenter.RemitteePresenter
import net.dankito.banking.fints4java.android.ui.extensions.addEnterPressedListener
import net.dankito.banking.fints4java.android.ui.listener.ListItemSelectedListener
import net.dankito.banking.fints4java.android.util.StandardAutocompleteCallback
import net.dankito.banking.fints4java.android.util.StandardTextWatcher
import net.dankito.banking.search.IRemitteeSearcher
import net.dankito.banking.search.Remittee
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.InputValidator
import net.dankito.fints.model.BankInfo
import net.dankito.utils.android.extensions.asActivity
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import javax.inject.Inject


open class TransferMoneyDialog : DialogFragment() {

    companion object {
        const val DialogTag = "TransferMoneyDialog"
    }


    protected var preselectedBankAccount: BankAccount? = null

    protected lateinit var bankAccount: BankAccount

    protected var preselectedValues: TransferMoneyData? = null

    protected val inputValidator = InputValidator() // TODO: move to presenter


    protected var foundBankForEnteredIban = false


    @Inject
    protected lateinit var presenter: BankingPresenter

    @Inject
    protected lateinit var remitteeSearcher: IRemitteeSearcher


    init {
        BankingComponent.component.inject(this)
    }


    open fun show(activity: AppCompatActivity, preselectedBankAccount: BankAccount?, fullscreen: Boolean = false) {
        show(activity, preselectedBankAccount, null, fullscreen)
    }

    open fun show(activity: AppCompatActivity, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?, fullscreen: Boolean = false) {
        this.preselectedBankAccount = preselectedBankAccount
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
        setPreselectedValues(rootView)

        val allBankAccountsSupportingTransferringMoney = presenter.bankAccountsSupportingTransferringMoney
        bankAccount = preselectedBankAccount ?: allBankAccountsSupportingTransferringMoney.first()

        if (allBankAccountsSupportingTransferringMoney.size > 1) {
            rootView.lytSelectBankAccount.visibility = View.VISIBLE

            val adapter = BankAccountsAdapter(allBankAccountsSupportingTransferringMoney)
            rootView.spnBankAccounts.adapter = adapter
            rootView.spnBankAccounts.onItemSelectedListener = ListItemSelectedListener(adapter) { selectedBankAccount ->
                this.bankAccount = selectedBankAccount
                setInstantPaymentControlsVisibility(rootView)
            }
            preselectedBankAccount?.let { rootView.spnBankAccounts.setSelection(adapter.getItems().indexOf(it)) }
        }

        initRemitteeAutocompletion(rootView.edtxtRemitteeName)

        rootView.edtxtRemitteeName.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredRemitteeNameIsValid()
        })

        rootView.edtxtRemitteeIban.addTextChangedListener(StandardTextWatcher { tryToGetBicFromIban(it) })

        rootView.edtxtRemitteeBic.addTextChangedListener(checkRequiredDataWatcher())
        rootView.edtxtAmount.addTextChangedListener(checkRequiredDataWatcher())
        rootView.edtxtUsage.addTextChangedListener(checkRequiredDataWatcher {
            checkIfEnteredUsageTextIsValid()
        })

        rootView.edtxtRemitteeName.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRemitteeNameIsValid() }
        rootView.edtxtRemitteeIban.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRemitteeIbanIsValid() }
        rootView.edtxtRemitteeBic.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRemitteeBicIsValid() }
        rootView.edtxtAmount.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredAmountIsValid() }
        rootView.edtxtUsage.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredUsageTextIsValid() }

        transferMoneyIfEnterPressed(rootView.edtxtRemitteeName)
        transferMoneyIfEnterPressed(rootView.edtxtRemitteeIban)
        transferMoneyIfEnterPressed(rootView.edtxtRemitteeBic)
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
            .with(RemitteePresenter(remitteeSearcher, edtxtRemitteeName.context))
            .build()
    }


    override fun onStart() {
        super.onStart()

        tryToGetBicFromIban(edtxtRemitteeIban.text.toString())
    }


    protected open fun setPreselectedValues(rootView: View) {
        preselectedValues?.let { data ->
            rootView.edtxtRemitteeName.setText(data.creditorName)

            rootView.edtxtRemitteeIban.setText(data.creditorIban)

            rootView.edtxtRemitteeBic.setText(data.creditorBic)

            focusEditTextAccordingToPreselectedValues(rootView, data)
        }
    }

    protected open fun focusEditTextAccordingToPreselectedValues(rootView: View, data: TransferMoneyData) {
        if (data.creditorName.trim().isNotEmpty()) {
            if (data.creditorIban.trim().isNotEmpty()) {
                if (data.creditorBic.trim().isNotEmpty()) {
                    rootView.edtxtAmount.requestFocus()
                }
                else {
                    rootView.edtxtRemitteeBic.requestFocus()
                }
            }
            else {
                rootView.edtxtRemitteeIban.requestFocus()
            }
        }
    }


    protected open fun remitteeSelected(item: Remittee) {
        edtxtRemitteeName.setText(item.name)
        edtxtRemitteeBic.setText(item.bic)
        edtxtRemitteeIban.setText(item.iban)
    }

    protected open fun transferMoney() {
        getEnteredAmount()?.let { amount -> // should only come at this stage when a valid amount has been entered
            val data = TransferMoneyData(
                edtxtRemitteeName.text.toString(),
                edtxtRemitteeIban.text.toString().replace(" ", ""),
                edtxtRemitteeBic.text.toString().replace(" ", ""),
                amount,
                edtxtUsage.text.toString(),
                chkbxInstantPayment.isChecked
            )

            presenter.transferMoneyAsync(bankAccount, data) {
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

    protected open fun tryToGetBicFromIban(enteredText: CharSequence) {
        presenter.findUniqueBankForIbanAsync(enteredText.toString()) { foundBank ->
            context?.asActivity()?.runOnUiThread {
                showValuesForFoundBankOnUiThread(foundBank)
            }
        }
    }

    private fun showValuesForFoundBankOnUiThread(foundBank: BankInfo?) {
        foundBankForEnteredIban = foundBank != null

        edtxtRemitteeBank.setText(if (foundBank != null) (foundBank.name + " " + foundBank.city) else "")

        edtxtRemitteeBic.setText(foundBank?.bic ?: "") // TODO: check if user entered BIC to not overwrite self entered BIC
        lytRemitteeBic.error = null

        if (foundBankForEnteredIban) {
            lytRemitteeIban.error = null
        }

        checkIfRequiredDataEnteredOnUiThread()
    }

    protected open fun checkIfRequiredDataEnteredOnUiThread() {
        btnTransferMoney.isEnabled = isRemitteeNameValid() && isRemitteeIbanValid()
                && isRemitteeBicValid()
                && isAmountGreaterZero() && isUsageTextValid()
    }

    protected open fun checkIfEnteredRemitteeNameIsValid() {
        if (isRemitteeNameValid()) {
            lytRemitteeName.error = null
        }
        else {
            val enteredName = edtxtRemitteeName.text.toString()

            if (enteredName.isEmpty()) {
                lytRemitteeName.error = context?.getString(R.string.error_no_name_entered)
            }
            else {
                lytRemitteeName.error = context?.getString(
                    R.string.error_invalid_sepa_characters_entered, inputValidator.getInvalidSepaCharacters(enteredName))
            }
        }
    }

    protected open fun isRemitteeNameValid(): Boolean {
        val enteredRemitteeName = edtxtRemitteeName.text.toString()

        return enteredRemitteeName.isNotEmpty()
                && inputValidator.containsOnlyValidSepaCharacters(enteredRemitteeName)
    }

    protected open fun checkIfEnteredRemitteeIbanIsValid() {
        val enteredIban = edtxtRemitteeIban.text.toString()

        if (isRemitteeIbanValid()) {
            lytRemitteeIban.error = null
        }
        else if (enteredIban.isBlank()) {
            lytRemitteeIban.error = context?.getString(R.string.error_no_iban_entered)
        }
        else {
            val invalidIbanCharacters = inputValidator.getInvalidIbanCharacters(enteredIban)
            if (invalidIbanCharacters.isNotEmpty()) {
                lytRemitteeIban.error = context?.getString(R.string.error_invalid_iban_characters_entered, invalidIbanCharacters)
            }
            else {
                lytRemitteeIban.error = context?.getString(R.string.error_invalid_iban_pattern_entered)
            }
        }

        if (foundBankForEnteredIban || enteredIban.isBlank()) {
            lytRemitteeBic.error = null
        }
        else {
            lytRemitteeBic.error = context?.getString(R.string.error_no_bank_found_for_entered_iban)
        }
    }

    protected open fun isRemitteeIbanValid(): Boolean {
        return inputValidator.isValidIban(edtxtRemitteeIban.text.toString())
    }

    protected open fun checkIfEnteredRemitteeBicIsValid() {
        if (isRemitteeBicValid()) {
            lytRemitteeBic.error = null
        }
        else {
            val enteredBic = edtxtRemitteeBic.text.toString()

            if (enteredBic.isBlank()) {
                lytRemitteeBic.error = context?.getString(R.string.error_no_bic_entered)
            }
            else {
                val invalidBicCharacters = inputValidator.getInvalidBicCharacters(enteredBic)
                if (invalidBicCharacters.isNotEmpty()) {
                    lytRemitteeBic.error = context?.getString(R.string.error_invalid_bic_characters_entered, invalidBicCharacters)
                }
                else {
                    lytRemitteeBic.error = context?.getString(R.string.error_invalid_bic_pattern_entered)
                }
            }
        }
    }

    protected open fun isRemitteeBicValid(): Boolean {
        return inputValidator.isValidBic(edtxtRemitteeBic.text.toString())
    }

    protected open fun checkIfEnteredAmountIsValid() {
        if (isAmountGreaterZero()) {
            lytAmount.error = null
        }
        else if (edtxtAmount.text.toString().isBlank()) {
            lytAmount.error = context?.getString(R.string.error_no_amount_entered)
        }
        else {
            lytAmount.error = context?.getString(R.string.error_invalid_amount_entered)
        }
    }

    protected open fun isAmountGreaterZero(): Boolean {
        try {
            getEnteredAmount()?.let { amount ->
                return amount > BigDecimal.ZERO
            }
        } catch (ignored: Exception) { }

        return false
    }

    protected open fun getEnteredAmount(): BigDecimal? {
        try {
            val amountString = edtxtAmount.text.toString().replace(',', '.')

            return amountString.toBigDecimal()
        } catch (ignored: Exception) { }

        return null
    }

    protected open fun checkIfEnteredUsageTextIsValid() {
        if (isUsageTextValid()) {
            lytUsage.error = null
        }
        else {
            lytUsage.error = context?.getString(R.string.error_invalid_sepa_characters_entered,
                inputValidator.getInvalidSepaCharacters(edtxtUsage.text.toString()))
        }
    }

    protected open fun isUsageTextValid(): Boolean {
        return inputValidator.containsOnlyValidSepaCharacters(edtxtUsage.text.toString())
    }

}