package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_bank_settings.edtxtBankName
import kotlinx.android.synthetic.main.dialog_bank_settings.edtxtUserName
import kotlinx.android.synthetic.main.dialog_bank_settings.edtxtPassword
import kotlinx.android.synthetic.main.dialog_bank_settings.view.*
import kotlinx.android.synthetic.main.dialog_bank_settings.view.toolbar
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.DraggableBankAccountAdapterItem
import net.dankito.banking.ui.android.adapter.FastAdapterRecyclerView
import net.dankito.banking.ui.android.adapter.TanMethodAdapterItem
import net.dankito.banking.ui.android.alerts.AskDeleteAccountAlert
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.tan.TanMethod


open class BankSettingsDialog : SettingsDialogBase() {

    companion object {
        const val DialogTag = "BankSettingsDialog"
    }


    protected lateinit var bank: TypedBankData

    protected var selectedTanMethod: TanMethod? = null

    protected lateinit var bankAccountsAdapter: FastAdapterRecyclerView<DraggableBankAccountAdapterItem>

    protected var banksChangedListener = { _: List<TypedBankData> ->
        updateBankAccountsAdapterItems()
    }



    fun show(bank: TypedBankData, activity: AppCompatActivity) {
        this.bank = bank
        this.selectedTanMethod = bank.selectedTanMethod

        show(activity, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_bank_settings, container, false)

        setupUI(rootView)

        presenter.addBanksChangedListener(banksChangedListener)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        rootView.apply {
            toolbar.apply {
                setupToolbar(this, bank.displayName)
            }

            edtxtBankName.text = bank.displayName
            edtxtUserName.text = bank.userName
            edtxtPassword.text = bank.password

            val tanMethodItems = createTanMethodItems()
            val tanMethodsAdapter = FastAdapterRecyclerView(rootView.rcyTanMethods, tanMethodItems)
            tanMethodsAdapter.onClickListener = {
                selectedTanMethod = it.tanMethod
                tanMethodsAdapter.setItems(createTanMethodItems())
            }

            lvlBankCode.value = bank.bankCode
            lvlBic.value = bank.bic
            lvlCustomerName.value = bank.customerName
            lvlFinTsServerAddress.value = bank.finTsServerAddress

            val items = createBankAccountsAdapterItems()
            bankAccountsAdapter = FastAdapterRecyclerView(rootView.rcyBankAccounts, items, true)
            bankAccountsAdapter.onClickListener = { navigationToBankAccountSettingsDialog(it.account) }
            bankAccountsAdapter.itemDropped = { oldPosition, oldItem, newPosition, newItem -> reorderedBankAccounts(oldPosition, oldItem.account, newPosition, newItem.account) }

            btnDeleteAccount.setOnClickListener { askUserToDeleteAccount() }
        }
    }


    override fun onDestroy() {
        presenter.removeBanksChangedListener(banksChangedListener)

        super.onDestroy()
    }


    protected open fun createTanMethodItems(): List<TanMethodAdapterItem> {
        return bank.supportedTanMethods.map { TanMethodAdapterItem(it, it == selectedTanMethod) }
    }


    protected open fun createBankAccountsAdapterItems(): List<DraggableBankAccountAdapterItem> {
        return bank.accountsSorted.map { DraggableBankAccountAdapterItem(it) }
    }

    protected open fun updateBankAccountsAdapterItems() {
        bankAccountsAdapter.setItems(createBankAccountsAdapterItems())
    }


    protected open fun navigationToBankAccountSettingsDialog(account: TypedBankAccount) {
        BankAccountSettingsDialog().show(account, requireActivity() as AppCompatActivity)
    }

    protected open fun reorderedBankAccounts(oldPosition: Int, oldItem: TypedBankAccount, newPosition: Int, newItem: TypedBankAccount) {
        oldItem.displayIndex = oldPosition
        newItem.displayIndex = newPosition

        presenter.accountUpdated(oldItem)
        presenter.accountUpdated(newItem)
    }


    override val hasUnsavedChanges: Boolean
        get() = didChange(edtxtBankName, bank.displayName)
                || didChange(edtxtUserName, bank.userName)
                || didChange(edtxtPassword, bank.password)
                || bank.selectedTanMethod != selectedTanMethod

    override fun saveChanges() {
        bank.userSetDisplayName = edtxtBankName.text
        bank.userName = edtxtUserName.text
        bank.password = edtxtPassword.text

        bank.selectedTanMethod = selectedTanMethod

        presenter.bankUpdated(bank)
    }

    protected open fun askUserToDeleteAccount() {
        AskDeleteAccountAlert().show(bank, presenter, requireContext()) {
            closeDialog()
        }
    }

}