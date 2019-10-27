package net.dankito.banking.fints4java.android.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_bank_transfer.*
import kotlinx.android.synthetic.main.dialog_bank_transfer.view.*
import net.dankito.banking.fints4java.android.R
import net.dankito.banking.fints4java.android.ui.MainWindowPresenter
import net.dankito.fints.model.BankTransferData
import net.dankito.utils.android.extensions.asActivity
import org.slf4j.LoggerFactory
import java.math.BigDecimal


open class BankTransferDialog : DialogFragment() {

    companion object {
        const val DialogTag = "BankTransferDialog"
    }


    protected lateinit var presenter: MainWindowPresenter

    protected var preselectedValues: BankTransferData? = null


    open fun show(activity: AppCompatActivity, presenter: MainWindowPresenter, fullscreen: Boolean = false) {
        show(activity, presenter, null, fullscreen)
    }

    open fun show(activity: AppCompatActivity, presenter: MainWindowPresenter, preselectedValues: BankTransferData?, fullscreen: Boolean = false) {
        this.presenter = presenter
        this.preselectedValues = preselectedValues

        val style = if(fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.Dialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_bank_transfer, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        setPreselectedValues(rootView)

        // TODO: add autocompletion by searching for name in account entries
        rootView.edtxtRemitteeName.addTextChangedListener(otherEditTextChangedWatcher)

        rootView.edtxtRemitteeIban.addTextChangedListener(ibanChangedWatcher)

        rootView.edtxtRemitteeBic.addTextChangedListener(otherEditTextChangedWatcher)
        rootView.edtxtAmount.addTextChangedListener(otherEditTextChangedWatcher)

        rootView.btnCancel.setOnClickListener { dismiss() }

        rootView.btnDoBankTransfer.setOnClickListener { dismiss() }
    }

    protected open fun setPreselectedValues(rootView: View) {
        preselectedValues?.let { data ->
            rootView.edtxtRemitteeName.setText(data.creditorName)

            rootView.edtxtRemitteeIban.setText(data.creditorIban)

            rootView.edtxtRemitteeBic.setText(data.creditorBic)

            focusEditTextAccordingToPreselectedValues(rootView, data)
        }
    }

    protected open fun focusEditTextAccordingToPreselectedValues(rootView: View, data: BankTransferData) {
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
        if (enteredText.length >= 12) { // first two characters are country code, 3rd and 4th character are checksum
            if (enteredText.startsWith("DE", true)) {
                presenter.searchForBankAsync(enteredText.substring(4)) { foundBanks ->
                    if (foundBanks.isNotEmpty()) {
                        context?.asActivity()?.runOnUiThread {
                            edtxtRemitteeBic.setText(foundBanks.first().bic)

                            checkIfRequiredDataEnteredOnUiThread()
                        }
                    }
                }
            }
        }
    }

    protected open fun checkIfRequiredDataEnteredOnUiThread() {
        val requiredDataEntered =
                edtxtRemitteeName.text.toString().isNotEmpty()
                && edtxtRemitteeIban.text.toString().isNotEmpty() // TODO: check if it is of length > 12, in Germany > 22?
                && edtxtRemitteeBic?.text.toString().isNotEmpty() // TODO: check if it is of length is 8 or 11?
                && isAmountGreaterZero(edtxtAmount.text.toString())

        btnDoBankTransfer.isEnabled = requiredDataEntered
    }

    protected open fun isAmountGreaterZero(amountString: String): Boolean {
        try {
            val amount = amountString.toBigDecimal()

            return amount > BigDecimal.ZERO
        } catch (ignored: Exception) { }

        return false
    }

}