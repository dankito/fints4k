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
import net.dankito.banking.ui.android.alerts.AskDeleteAccountAlert
import net.dankito.banking.ui.model.TypedBankAccount
import net.dankito.banking.ui.model.TypedBankData


open class BankSettingsDialog : SettingsDialogBase() {

    companion object {
        const val DialogTag = "BankSettingsDialog"
    }


    protected lateinit var bank: TypedBankData



    fun show(bank: TypedBankData, activity: AppCompatActivity) {
        this.bank = bank

        show(activity, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_bank_settings, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        rootView.apply {
            toolbar.apply {
                setupToolbar(this, bank.bankName)
            }

            edtxtBankName.text = bank.displayName
            edtxtUserName.text = bank.userName
            edtxtPassword.text = bank.password

            val items = bank.accountsSorted.map { DraggableBankAccountAdapterItem(it) }
            val adapter = FastAdapterRecyclerView(rootView.rcyBankAccounts, items, true)
            adapter.itemDropped = { oldPosition, oldItem, newPosition, newItem -> reorderedBankAccounts(oldPosition, oldItem.account, newPosition, newItem.account) }

            btnDeleteAccount.setOnClickListener { askUserToDeleteAccount() }
        }
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

    override fun saveChanges() {
        bank.userSetDisplayName = edtxtBankName.text
        bank.userName = edtxtUserName.text
        bank.password = edtxtPassword.text

        presenter.bankUpdated(bank)
    }

    protected open fun askUserToDeleteAccount() {
        AskDeleteAccountAlert().show(bank, presenter, requireContext()) {
            closeDialog()
        }
    }

}