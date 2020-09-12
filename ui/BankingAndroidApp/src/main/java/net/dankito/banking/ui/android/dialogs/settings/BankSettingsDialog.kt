package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_bank_settings.edtxtBankName
import kotlinx.android.synthetic.main.dialog_bank_settings.edtxtCustomerId
import kotlinx.android.synthetic.main.dialog_bank_settings.edtxtPassword
import kotlinx.android.synthetic.main.dialog_bank_settings.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.alerts.AskDeleteAccountAlert
import net.dankito.banking.ui.android.alerts.AskDismissChangesAlert
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.views.FormEditText
import net.dankito.banking.ui.model.TypedCustomer
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.hideKeyboardDelayed
import javax.inject.Inject


open class BankSettingsDialog : DialogFragment() {

    companion object {
        const val DialogTag = "BankSettingsDialog"
    }


    protected lateinit var bank: TypedCustomer


    @Inject
    protected lateinit var presenter: BankingPresenter


    init {
        BankingComponent.component.inject(this)
    }



    fun show(bank: TypedCustomer, activity: AppCompatActivity, fullscreen: Boolean = false) {
        this.bank = bank

        val style = if (fullscreen) R.style.FullscreenDialogWithStatusBar else R.style.FloatingDialog
        setStyle(STYLE_NORMAL, style)

        show(activity.supportFragmentManager, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_bank_settings, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        rootView.apply {
            toolbar.apply {
                title = bank.bankName

                inflateMenu(R.menu.menu_bank_settings_dialog)
                setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }

                setNavigationOnClickListener { askToDismissChanges() }
            }

            edtxtBankName.text = bank.displayName
            edtxtCustomerId.text = bank.customerId
            edtxtPassword.text = bank.password

            btnDeleteAccount.setOnClickListener { askUserToDeleteAccount() }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnitmSaveChanges -> saveChangesAndCloseDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }


    protected val hasUnsavedChanges: Boolean
        get() = didChange(edtxtBankName, bank.displayName)
                || didChange(edtxtCustomerId, bank.customerId)
                || didChange(edtxtPassword, bank.password)

    protected open fun didChange(editedValue: FormEditText, originalValue: String): Boolean {
        return editedValue.text != originalValue
    }

    protected open fun saveChangesAndCloseDialog(): Boolean {
        if (hasUnsavedChanges) {
            saveChanges()
        }

        closeDialog()

        return true
    }

    protected open fun saveChanges() {
        bank.userSetDisplayName = edtxtBankName.text
        bank.customerId = edtxtCustomerId.text
        bank.password = edtxtPassword.text

        presenter.accountUpdated(bank)
    }

    protected open fun askToDismissChanges() {
        if (hasUnsavedChanges) {
            AskDismissChangesAlert().show(this)
        }
        else {
            closeDialog()
        }
    }

    protected open fun askUserToDeleteAccount() {
        AskDeleteAccountAlert().show(bank, presenter, requireContext()) {
            closeDialog()
        }
    }

    protected open fun closeDialog() {
        dismiss()
    }
}