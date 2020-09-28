package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.dialog_settings.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.BankDataAdapterItem
import net.dankito.banking.ui.android.adapter.FastAdapterRecyclerView
import net.dankito.banking.ui.model.TypedBankData


open class SettingsDialog : SettingsDialogBase() {

    companion object {
        const val DialogTag = "SettingsDialog"
    }



    fun show(activity: AppCompatActivity) {
        show(activity, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_settings, container, false)

        setupUI(rootView)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        rootView.apply {
            toolbar.apply {
                setupToolbar(this, rootView.context.getString(R.string.dialog_settings_title), false)
            }

            val items = presenter.allBanksSortedByDisplayIndex.map { BankDataAdapterItem(it) }
            val adapter = FastAdapterRecyclerView(rootView.rcyBankCredentials, items, true)
            adapter.onClickListener = { navigationToBankSettingsDialog(it.bank) }
            adapter.itemDropped = { oldPosition, oldItem, newPosition, newItem -> reorderedBanks(oldPosition, oldItem.bank, newPosition, newItem.bank) }

            rootView.btnShowSendMessageLogDialog.setOnClickListener { presenter.showSendMessageLogDialog() }
        }
    }


    protected open fun navigationToBankSettingsDialog(bank: TypedBankData) {
        BankSettingsDialog().show(bank, requireActivity() as AppCompatActivity)
    }

    protected open fun reorderedBanks(oldPosition: Int, oldItem: TypedBankData, newPosition: Int, newItem: TypedBankData) {
        oldItem.displayIndex = oldPosition
        newItem.displayIndex = newPosition

        presenter.bankDisplayIndexUpdated(oldItem)
        presenter.bankDisplayIndexUpdated(newItem)
    }


    override val hasUnsavedChanges: Boolean
        get() = false

    override fun saveChanges() {

    }

}