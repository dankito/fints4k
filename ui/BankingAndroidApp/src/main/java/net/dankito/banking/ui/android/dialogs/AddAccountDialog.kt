package net.dankito.banking.ui.android.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.otaliastudios.autocomplete.Autocomplete
import kotlinx.android.synthetic.main.dialog_add_account.*
import kotlinx.android.synthetic.main.dialog_add_account.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.adapter.presenter.BankInfoPresenter
import net.dankito.banking.ui.android.extensions.addEnterPressedListener
import net.dankito.banking.ui.android.extensions.closePopupOnBackButtonPress
import net.dankito.banking.ui.android.util.StandardAutocompleteCallback
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.utils.android.extensions.asActivity
import javax.inject.Inject


open class AddAccountDialog : DialogFragment() {

    companion object {
        const val DialogTag = "AddAccountDialog"
    }


    protected var selectedBank: BankInfo? = null


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)
    }


    fun show(activity: AppCompatActivity, fullscreen: Boolean = true) {
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
        rootView.apply {
            initBankListAutocompletion(edtxtBank.actualEditText)
            edtxtBank.actualEditText.requestFocus()

            edtxtUserName.actualEditText.addTextChangedListener(otherEditTextChangedWatcher)
            bankCredentialsPassword.passwordBox.addTextChangedListener(otherEditTextChangedWatcher)

            addAccountIfEnterPressed(edtxtBank.actualEditText)
            addAccountIfEnterPressed(edtxtUserName.actualEditText)
            addAccountIfEnterPressed(bankCredentialsPassword.passwordBox)

            btnAddAccount.setOnClickListener { addAccount() }
            btnCancel.setOnClickListener { dismiss() }
        }
    }

    private fun initBankListAutocompletion(edtxtBank: EditText) {
        val autocompleteCallback = StandardAutocompleteCallback<BankInfo> { _, item ->
            bankSelected(item)
            true
        }

        Autocomplete.on<BankInfo>(edtxtBank)
            .with(6f)
            .with(ColorDrawable(Color.WHITE))
            .with(autocompleteCallback)
            .with(BankInfoPresenter(presenter, edtxtBank.context))
            .build()
            .closePopupOnBackButtonPress(dialog)
    }

    private fun addAccountIfEnterPressed(editText: EditText) {
        editText.addEnterPressedListener {
            if (btnAddAccount.isEnabled) { // required data has been entered
                addAccount()

                return@addEnterPressedListener true
            }

            false
        }
    }


    protected open fun addAccount() {
        selectedBank?.let { selectedBank -> // should always be non-null at this stage
            val userName = edtxtUserName.text
            val password = bankCredentialsPassword.password

            btnAddAccount.isEnabled = false
            pgrbrAddAccount.visibility = View.VISIBLE

            presenter.addAccountAsync(selectedBank, userName, password, bankCredentialsPassword.savePassword) { response ->
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
            if (response.successful) {
                this.dismiss()
            }
            else {
                AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.dialog_add_account_message_could_not_add_account, response.errorToShowToUser))
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
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

    protected open fun bankSelected(bank: BankInfo) {
        selectedBank = bank

        edtxtBank.text = bank.name

        edtxtUserName.requestFocus()

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
                && edtxtUserName.text.isNotEmpty()
                && bankCredentialsPassword.password.isNotEmpty()

        btnAddAccount.isEnabled = requiredDataEntered
    }

}