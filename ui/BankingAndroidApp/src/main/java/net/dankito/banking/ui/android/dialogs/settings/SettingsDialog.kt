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


    protected lateinit var banksAdapter: FastAdapterRecyclerView<BankDataAdapterItem>

    protected var banksChangedListener = { _: List<TypedBankData> ->
        updateBanksAdapterItems()
    }


    fun show(activity: AppCompatActivity) {
        show(activity, DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_settings, container, false)

        setupUI(rootView)

        presenter.addBanksChangedListener(banksChangedListener)

        return rootView
    }

    protected open fun setupUI(rootView: View) {
        rootView.apply {
            toolbar.apply {
                setupToolbar(this, rootView.context.getString(R.string.settings), false)
            }

            val items = createBanksAdapterItems()
            banksAdapter = FastAdapterRecyclerView(rootView.rcyBankCredentials, items, true)
            banksAdapter.onClickListener = { navigationToBankSettingsDialog(it.bank) }
            banksAdapter.itemDropped = { oldPosition, oldItem, newPosition, newItem -> reorderedBanks(oldPosition, oldItem.bank, newPosition, newItem.bank) }

            rootView.btnShowSendMessageLogDialog.setOnClickListener { presenter.showSendMessageLogDialog() }
        }
    }


    override fun onDestroy() {
        presenter.removeBanksChangedListener(banksChangedListener)

        super.onDestroy()
    }


    protected open fun createBanksAdapterItems(): List<BankDataAdapterItem> {
        return presenter.allBanksSortedByDisplayIndex.map { BankDataAdapterItem(it) }
    }

    protected open fun updateBanksAdapterItems() {
        banksAdapter.setItems(createBanksAdapterItems())
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