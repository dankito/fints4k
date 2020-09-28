package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_bank_account_settings.*
import kotlinx.android.synthetic.main.dialog_bank_account_settings.view.*
import kotlinx.android.synthetic.main.dialog_bank_settings.view.toolbar
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.model.TypedBankAccount


open class BankAccountSettingsDialog : SettingsDialogBase() {

    companion object {
        const val DialogTag = "BankAccountSettingsDialog"
    }


    protected lateinit var account: TypedBankAccount



    fun show(account: TypedBankAccount, activity: AppCompatActivity) {
        this.account = account

        show(activity, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_bank_account_settings, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        rootView.apply {
            toolbar.apply {
                setupToolbar(this, account.displayName)
            }

            edtxtBankAccountName.text = account.displayName

            lvlAccountHolderName.value = account.accountHolderName
            lvlAccountIdentifier.value = account.identifier
            lvlSubAccountNumber.setValueAndVisibilityIfValueIsSet(account.subAccountNumber)
            lvlIban.setValueAndVisibilityIfValueIsSet(account.iban)
            lvlAccountType.value = account.type.toString() // TODO: translate
        }
    }


    override val hasUnsavedChanges: Boolean
        get() = didChange(edtxtBankAccountName, account.displayName)

    override fun saveChanges() {
        account.userSetDisplayName = edtxtBankAccountName.text

        presenter.accountUpdated(account)
    }

}