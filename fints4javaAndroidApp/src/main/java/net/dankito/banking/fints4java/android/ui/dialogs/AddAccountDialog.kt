package net.dankito.banking.fints4java.android.ui.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_add_account.*
import kotlinx.android.synthetic.main.dialog_add_account.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.adapter.BankListAdapter
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.presenter.MainWindowPresenter
import net.dankito.fints.model.BankInfo
import net.dankito.utils.android.extensions.asActivity


open class AddAccountDialog : DialogFragment() {

    companion object {
        const val DialogTag = "AddAccountDialog"
    }


    protected lateinit var presenter: MainWindowPresenter

    protected lateinit var adapter: BankListAdapter

    protected var selectedBank: BankInfo? = null


    fun show(activity: AppCompatActivity, presenter: MainWindowPresenter, fullscreen: Boolean = false) {
        this.presenter = presenter

        presenter.preloadBanksAsync()
        this.adapter = BankListAdapter(presenter)

        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.FloatingDialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_add_account, container, false)

        rootView?.let {
            setupUI(rootView)
        }

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        rootView.edtxtBankCode.threshold = 1 // will start working from first character
        rootView.edtxtBankCode.setAdapter(adapter)

        rootView.edtxtBankCode.setOnItemClickListener { _, _, position, _ -> bankSelected(adapter.getItem(position)) }

        rootView.edtxtCustomerId.addTextChangedListener(otherEditTextChangedWatcher)
        rootView.edtxtPin.addTextChangedListener(otherEditTextChangedWatcher)

        rootView.btnAddAccount.setOnClickListener { addAccount() }
        rootView.btnCancel.setOnClickListener { dismiss() }
    }

    protected open fun addAccount() {
        selectedBank?.let { selectedBank -> // should always be non-null at this stage
            val customerId = edtxtCustomerId.text.toString()
            val pin = edtxtPin.text.toString()

            btnAddAccount.isEnabled = false
            pgrbrAddAccount.visibility = View.VISIBLE

            presenter.addAccountAsync(selectedBank, customerId, pin) { response ->
                context?.asActivity()?.runOnUiThread {
                    btnAddAccount.isEnabled = true
                    pgrbrAddAccount.visibility = View.GONE

                    handleAccountCheckResponseOnUiThread(response)
                }
            }
        }
    }

    protected open fun handleAccountCheckResponseOnUiThread(response: AddAccountResponse) {
        context?.let { context ->
            if (response.isSuccessful) {
                this.dismiss()

                showMessageForSuccessfullyAddedAccount(context, response)
            }
            else {
                AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.dialog_add_account_message_could_not_add_account, response.errorToShowToUser))
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    protected open fun showMessageForSuccessfullyAddedAccount(context: Context, response: AddAccountResponse) {
        val view = createSuccessfullyAddedAccountView(context, response)

        AlertDialog.Builder(context)
            .setView(view)
            .setPositiveButton(R.string.fetch) { dialog, _ -> retrieveAccountTransactionsAndDismiss(response, dialog) }
            .setNeutralButton(R.string.no) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    protected open fun createSuccessfullyAddedAccountView(context: Context, response: AddAccountResponse): View? {

        val messageId = if (response.supportsRetrievingTransactionsOfLast90DaysWithoutTan)
            R.string.dialog_add_account_message_successfully_added_account_support_retrieving_transactions_of_last_90_days_without_tan
        else R.string.dialog_add_account_message_successfully_added_account

        val view = context.asActivity()?.layoutInflater?.inflate(R.layout.view_successfully_added_account, null)

        view?.findViewById<TextView>(R.id.txtSuccessfullyAddedAccountMessage)?.setText(messageId)

        return view
    }

    protected open fun retrieveAccountTransactionsAndDismiss(response: AddAccountResponse, messageDialog: DialogInterface) {
        presenter.getAccountTransactionsAsync(response.account) { }

        messageDialog.dismiss()
    }


    protected val otherEditTextChangedWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(enteredText: CharSequence?, start: Int, before: Int, count: Int) {
            checkIfRequiredDataEnteredOnUiThread()
        }

        override fun afterTextChanged(s: Editable?) { }

    }

    protected open fun bankSelected(bank: BankInfo) {
        selectedBank = bank

        edtxtBankCode.setText(bank.bankCode)

        edtxtFinTsServerAddress.setText(bank.pinTanAddress)

        edtxtBankCode.clearListSelection()

        checkIfRequiredDataEnteredOnUiThread()

        if (bank.supportsFinTs3_0 == false) {
            showBankDoesNotSupportFinTs30ErrorMessage(bank)
        }
    }

    private fun showBankDoesNotSupportFinTs30ErrorMessage(bank: BankInfo) {
        activity?.let { context ->
            val errorMessage = context.getString(R.string.dialog_add_account_bank_does_not_support_fints_3_error_message, bank.name)

            AlertDialog.Builder(context)
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    protected open fun checkIfRequiredDataEnteredOnUiThread() {
        val requiredDataEntered = selectedBank != null
                && selectedBank?.supportsFinTs3_0 == true
                && edtxtCustomerId.text.toString().isNotEmpty() // TODO: check if it is of length 10?
                && edtxtPin.text.toString().isNotEmpty() // TODO: check if it is of length 5?

        btnAddAccount.isEnabled = requiredDataEntered
    }

}