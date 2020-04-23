package net.dankito.banking.fints4java.android.ui.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_transfer_money.*
import kotlinx.android.synthetic.main.dialog_transfer_money.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.adapter.BankAccountsAdapter
import net.dankito.banking.fints4java.android.ui.listener.ListItemSelectedListener
import net.dankito.banking.ui.model.BankAccount
import net.dankito.banking.ui.model.parameters.TransferMoneyData
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.InputValidator
import net.dankito.fints.model.BankInfo
import net.dankito.utils.android.extensions.asActivity
import java.math.BigDecimal


open class TransferMoneyDialog : DialogFragment() {

    companion object {
        const val DialogTag = "TransferMoneyDialog"
    }


    protected lateinit var presenter: BankingPresenter

    protected var preselectedBankAccount: BankAccount? = null

    protected lateinit var bankAccount: BankAccount

    protected var preselectedValues: TransferMoneyData? = null

    protected val inputValidator = InputValidator() // TODO: move to presenter


    protected var foundBankForEnteredIban = false


    open fun show(activity: AppCompatActivity, presenter: BankingPresenter, preselectedBankAccount: BankAccount?, fullscreen: Boolean = false) {
        show(activity, presenter, preselectedBankAccount, null, fullscreen)
    }

    open fun show(activity: AppCompatActivity, presenter: BankingPresenter, preselectedBankAccount: BankAccount?, preselectedValues: TransferMoneyData?, fullscreen: Boolean = false) {
        this.presenter = presenter
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
            }
            preselectedBankAccount?.let { rootView.spnBankAccounts.setSelection(adapter.getItems().indexOf(it)) }
        }

        // TODO: add autocompletion by searching for name in account entries
        rootView.edtxtRemitteeName.addTextChangedListener(otherEditTextChangedWatcher)

        rootView.edtxtRemitteeIban.addTextChangedListener(ibanChangedWatcher)

        rootView.edtxtRemitteeBic.addTextChangedListener(otherEditTextChangedWatcher)
        rootView.edtxtAmount.addTextChangedListener(otherEditTextChangedWatcher)
        rootView.edtxtUsage.addTextChangedListener(otherEditTextChangedWatcher)

        rootView.edtxtRemitteeName.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRemitteeNameIsValid() }
        rootView.edtxtRemitteeIban.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredRemitteeIbanIsValid() }
        rootView.edtxtAmount.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredAmountIsValid() }
        rootView.edtxtUsage.setOnFocusChangeListener { _, hasFocus -> if (hasFocus == false) checkIfEnteredUsageTextIsValid() }

        rootView.btnCancel.setOnClickListener { dismiss() }

        rootView.btnTransferMoney.setOnClickListener { transferMoney() }
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

    protected open fun transferMoney() {
        getEnteredAmount()?.let { amount -> // should only come at this stage when a valid amount has been entered
            val data = TransferMoneyData(
                edtxtRemitteeName.text.toString(),
                edtxtRemitteeIban.text.toString(),
                edtxtRemitteeBic.text.toString(),
                amount,
                edtxtUsage.text.toString()
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

            this.dismiss()
        }
    }


    protected val otherEditTextChangedWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(enteredText: CharSequence?, start: Int, before: Int, count: Int) {
            checkIfRequiredDataEnteredOnUiThread()
        }

        override fun afterTextChanged(s: Editable?) { }

    }

    protected val ibanChangedWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(enteredText: CharSequence?, start: Int, before: Int, count: Int) {
            enteredText?.let {
                tryToGetBicFromIban(enteredText)
            }
        }

        override fun afterTextChanged(s: Editable?) { }

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

        edtxtRemitteeBic.setText(foundBank?.bic ?: "")

        if (foundBankForEnteredIban) {
            lytRemitteeBic.error = null
        }

        checkIfRequiredDataEnteredOnUiThread()
    }

    protected open fun checkIfRequiredDataEnteredOnUiThread() {
        val isRemitteeNameValid = isRemitteeNameValid()
        val isValidIban = isRemitteeIbanValid()
        val isAmountValid = isAmountGreaterZero()
        val isUsageTextValid = isUsageTextValid()

        btnTransferMoney.isEnabled = isRemitteeNameValid && isValidIban
                && edtxtRemitteeBic?.text.toString().isNotEmpty() // TODO: check if it is of length is 8 or 11?
                && isAmountValid && isUsageTextValid
    }

    protected open fun checkIfEnteredRemitteeNameIsValid() {
        if (isRemitteeNameValid()) {
            lytRemitteeName.error = null
        }
        else {
            lytRemitteeName.error = context?.getString(R.string.error_invalid_sepa_characters_entered,
                inputValidator.getInvalidSepaCharacters(edtxtRemitteeName.text.toString()))
        }
    }

    protected open fun isRemitteeNameValid(): Boolean {
        val enteredRemitteeName = edtxtRemitteeName.text.toString()

        return enteredRemitteeName.isNotEmpty()
                && inputValidator.containsOnlyValidSepaCharacters(enteredRemitteeName)
    }

    protected open fun checkIfEnteredRemitteeIbanIsValid() {
        if (isRemitteeIbanValid()) {
            lytRemitteeIban.error = null
        }
        else {
            val invalidIbanCharacters = inputValidator.getInvalidIbanCharacters(edtxtRemitteeIban.text.toString())
            if (invalidIbanCharacters.isNotEmpty()) {
                lytRemitteeIban.error = context?.getString(R.string.error_invalid_iban_characters_entered, invalidIbanCharacters)
            }
            else {
                lytRemitteeIban.error = context?.getString(R.string.error_invalid_iban_pattern_entered)
            }
        }

        if (foundBankForEnteredIban) {
            lytRemitteeBic.error = null
        }
        else {
            lytRemitteeBic.error = context?.getString(R.string.error_no_bank_found_for_entered_iban)
        }
    }

    protected open fun isRemitteeIbanValid(): Boolean {
        return inputValidator.isValidIban(edtxtRemitteeIban.text.toString())
    }

    protected open fun checkIfEnteredAmountIsValid() {
        if (isAmountGreaterZero()) {
            lytAmount.error = null
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
            val amountString = edtxtAmount.text.toString()

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