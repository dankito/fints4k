package net.dankito.banking.fints4java.android.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_add_account.*
import kotlinx.android.synthetic.main.dialog_add_account.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.MainWindowPresenter
import net.dankito.fints.model.BankInfo
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.utils.android.extensions.asActivity


class AddAccountDialog : DialogFragment() {

    companion object {
        const val DialogTag = "AddAccountDialog"
    }


    private lateinit var presenter: MainWindowPresenter

    private var selectedBank: BankInfo? = null


    fun show(activity: AppCompatActivity, presenter: MainWindowPresenter, fullscreen: Boolean = false) {
        this.presenter = presenter

        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.Dialog
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

    private fun setupUI(rootView: View) {
        rootView.edtxtBankCode.addTextChangedListener(bankCodeChangedWatcher)

        rootView.edtxtCustomerId.addTextChangedListener(otherEditTextChangedWatcher)
        rootView.edtxtPin.addTextChangedListener(otherEditTextChangedWatcher)

        rootView.btnSelect.setOnClickListener { addAccount() }
        rootView.btnCancel.setOnClickListener { dismiss() }
    }

    private fun addAccount() {
        selectedBank?.let { selectedBank -> // should always be non-null at this stage
            val customerId = edtxtCustomerId.text.toString()
            val pin = edtxtPin.text.toString()

            presenter.checkIfAccountExists(selectedBank, customerId, pin) { response ->
                context?.asActivity()?.runOnUiThread {
                    handleAccountCheckResponseOnUiThread(response)
                }
            }
        }
    }

    private fun handleAccountCheckResponseOnUiThread(response: FinTsClientResponse) {
        context?.let { context ->
            if (response.isSuccessful) {
                AlertDialog.Builder(context)
                    .setMessage("Successfully added account")
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
                this.dismiss()
            }
            else {
                AlertDialog.Builder(context).setMessage("Could not add account: ${response.exception ?: response.errorsToShowToUser.joinToString("\n")}").show()
            }
        }
    }


    val bankCodeChangedWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(enteredText: CharSequence?, start: Int, before: Int, count: Int) {
            enteredText?.let { searchForBankAsync(enteredText) }
        }

        override fun afterTextChanged(s: Editable?) { }

    }

    val otherEditTextChangedWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(enteredText: CharSequence?, start: Int, before: Int, count: Int) {
            checkIfRequiredDataEnteredOnUiThread()
        }

        override fun afterTextChanged(s: Editable?) { }

    }

    private fun searchForBankAsync(enteredBankCode: CharSequence) {
        presenter.searchForBankAsync(enteredBankCode.toString()) { foundBanks ->
            context?.asActivity()?.runOnUiThread {
                showFoundBanksOnUiThread(foundBanks)
            }
        }
    }

    private fun showFoundBanksOnUiThread(foundBanks: List<BankInfo>) {
        if (foundBanks.isNotEmpty()) {
            selectedBank = foundBanks.first()
        }
        else {
            selectedBank = null
        }

        checkIfRequiredDataEnteredOnUiThread()
    }

    private fun checkIfRequiredDataEnteredOnUiThread() {
        val requiredDataEntered = selectedBank != null
                && edtxtCustomerId.text.toString().isNotEmpty() // TODO: check if it is of length 10?
                && edtxtPin.text.toString().isNotEmpty() // TODO: check if it is of length 5?

        btnSelect.isEnabled = requiredDataEntered
    }

}