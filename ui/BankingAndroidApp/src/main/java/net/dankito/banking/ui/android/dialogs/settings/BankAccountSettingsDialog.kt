package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_bank_account_settings.*
import kotlinx.android.synthetic.main.dialog_bank_account_settings.view.*
import kotlinx.android.synthetic.main.dialog_bank_settings.view.toolbar
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.CheckableValueAdapterItem
import net.dankito.banking.ui.android.adapter.FastAdapterRecyclerView
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

            swtchHideAccount.setOnCheckedChangeListener { _, hideAccount -> swtchUpdateAccountAutomatically.isEnabled = hideAccount == false }

            swtchHideAccount.isChecked = account.hideAccount
            swtchUpdateAccountAutomatically.isChecked = account.updateAccountAutomatically

            lvlAccountHolderName.value = account.accountHolderName
            lvlAccountIdentifier.value = account.identifier
            lvlSubAccountNumber.setValueAndVisibilityIfValueIsSet(account.subAccountNumber)
            lvlIban.setValueAndVisibilityIfValueIsSet(account.iban)
            lvlAccountType.value = account.type.toString() // TODO: translate

            val context = rootView.context
            val accountFeaturesItems = listOf(
                CheckableValueAdapterItem(account.supportsRetrievingBalance, context, R.string.dialog_bank_account_settings_account_features_supports_retrieving_balance),
                CheckableValueAdapterItem(account.supportsRetrievingAccountTransactions, context, R.string.dialog_bank_account_settings_account_features_supports_retrieving_account_transactions),
                CheckableValueAdapterItem(account.supportsTransferringMoney, context, R.string.dialog_bank_account_settings_account_features_supports_money_transfer),
                CheckableValueAdapterItem(account.supportsRealTimeTransfer, context, R.string.dialog_bank_account_settings_account_features_supports_real_time_transfer)
            )
            FastAdapterRecyclerView(rootView.rcyAccountFeatures, accountFeaturesItems)
        }
    }


    override val hasUnsavedChanges: Boolean
        get() = didChange(edtxtBankAccountName, account.displayName)
                || swtchHideAccount.isChecked != account.hideAccount
                || swtchUpdateAccountAutomatically.isChecked != account.updateAccountAutomatically

    override fun saveChanges() {
        account.userSetDisplayName = edtxtBankAccountName.text

        account.hideAccount = swtchHideAccount.isChecked
        account.updateAccountAutomatically = swtchUpdateAccountAutomatically.isChecked

        presenter.accountUpdated(account)
    }

}