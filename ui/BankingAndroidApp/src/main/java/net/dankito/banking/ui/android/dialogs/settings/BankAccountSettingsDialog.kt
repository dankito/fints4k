package net.dankito.banking.ui.android.dialogs.settings

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_bank_account_settings.*
import kotlinx.android.synthetic.main.dialog_bank_account_settings.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.CheckableValueAdapterItem
import net.dankito.banking.ui.android.adapter.FastAdapterRecyclerView
import net.dankito.banking.ui.model.BankAccountType
import net.dankito.banking.ui.model.TypedBankAccount


open class BankAccountSettingsDialog : SettingsDialogBase() {

    companion object {
        const val DialogTag = "BankAccountSettingsDialog"
    }


    protected lateinit var account: TypedBankAccount



    fun show(account: TypedBankAccount, activity: FragmentActivity) {
        this.account = account

        show(activity, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bank_account_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.apply {
            setupToolbar(this, account.displayName)
        }

        edtxtBankAccountName.text = account.displayName

        swtchHideAccount.setOnCheckedChangeListener { _, hideAccount -> swtchIncludeInAutomaticAccountsUpdate.isEnabled = hideAccount == false }

        swtchHideAccount.isChecked = account.hideAccount
        swtchIncludeInAutomaticAccountsUpdate.isChecked = account.includeInAutomaticAccountsUpdate

        btnShareAccountData.setOnClickListener { shareAccountData() }

        lvlAccountHolderName.value = account.accountHolderName
        lvlAccountIdentifier.value = account.identifier
        lvlSubAccountNumber.setValueAndVisibilityIfValueIsSet(account.subAccountNumber)
        lvlIban.setValueAndVisibilityIfValueIsSet(account.iban)
        lvlAccountType.value = getString(getBankAccountTypeResId(account.type))

        val context = view.context
        val accountFeaturesItems = listOf(
            CheckableValueAdapterItem(account.supportsRetrievingBalance, context, R.string.dialog_bank_account_settings_account_features_supports_retrieving_balance),
            CheckableValueAdapterItem(account.supportsRetrievingAccountTransactions, context, R.string.dialog_bank_account_settings_account_features_supports_retrieving_account_transactions),
            CheckableValueAdapterItem(account.supportsTransferringMoney, context, R.string.dialog_bank_account_settings_account_features_supports_money_transfer),
            CheckableValueAdapterItem(account.supportsRealTimeTransfer, context, R.string.dialog_bank_account_settings_account_features_supports_real_time_transfer)
        )
        FastAdapterRecyclerView(view.rcyAccountFeatures, accountFeaturesItems)
    }

    protected open fun getBankAccountTypeResId(type: BankAccountType): Int {
        return when (type) {
            BankAccountType.CheckingAccount -> R.string.checking_account
            BankAccountType.SavingsAccount -> R.string.savings_account
            BankAccountType.FixedTermDepositAccount -> R.string.fixed_term_deposit_account
            BankAccountType.SecuritiesAccount -> R.string.securities_account
            BankAccountType.LoanAccount -> R.string.loan_account
            BankAccountType.CreditCardAccount -> R.string.credit_card_account
            BankAccountType.FundDeposit -> R.string.fund_deposit
            BankAccountType.BuildingLoanContract -> R.string.building_loan_contract
            BankAccountType.InsuranceContract -> R.string.insurance_contract
            else -> R.string.other
        }
    }


    protected open fun shareAccountData() {
        val accountData = StringBuilder(account.accountHolderName + "\n" + account.bank.bankName)

        account.iban?.let { iban ->
            accountData.append("\n" + getString(R.string.share_account_data_iban, iban))
        }

        accountData.append("\n" + getString(R.string.share_account_data_bic, account.bank.bic))
        accountData.append("\n" + getString(R.string.share_account_data_bank_code, account.bank.bankCode))
        accountData.append("\n" + getString(R.string.share_account_data_account_number, account.identifier))


        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, accountData.toString())
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)

    }


    override val hasUnsavedChanges: Boolean
        get() = didChange(edtxtBankAccountName, account.displayName)
                || swtchHideAccount.isChecked != account.hideAccount
                || swtchIncludeInAutomaticAccountsUpdate.isChecked != account.includeInAutomaticAccountsUpdate

    override fun saveChanges() {
        account.userSetDisplayName = edtxtBankAccountName.text

        account.hideAccount = swtchHideAccount.isChecked
        account.includeInAutomaticAccountsUpdate = swtchIncludeInAutomaticAccountsUpdate.isChecked

        presenter.accountUpdated(account)
    }

}