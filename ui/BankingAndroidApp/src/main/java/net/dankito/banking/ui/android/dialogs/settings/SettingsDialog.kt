package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_settings.*
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


    fun show(activity: FragmentActivity) {
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
                setupToolbar(this, rootView.context.getString(R.string.settings))
            }

            val items = createBanksAdapterItems()
            banksAdapter = FastAdapterRecyclerView(rootView.rcyBankCredentials, items, true)
            banksAdapter.onClickListener = { navigationToBankSettingsDialog(it.bank) }
            banksAdapter.itemDropped = { oldPosition, oldItem, newPosition, newItem -> reorderedBanks(oldPosition, oldItem.bank, newPosition, newItem.bank) }

            selectUpdateAccountsAfter.periodInMinutes = presenter.appSettings.automaticallyUpdateAccountsAfterMinutes

            btnSetAppProtection.setOnClickListener { navigateToProtectAppSettingsDialog() }
            selectLockAppAfter.periodInMinutes = presenter.appSettings.lockAppAfterMinutes

            // on Pre Lollipop devices setting vector drawables in xml is not supported -> set left drawable here
            val sendIcon = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_send_24)
            btnShowSendMessageLogDialog.setCompoundDrawablesWithIntrinsicBounds(sendIcon, null, null, null)
            btnShowSendMessageLogDialog.setOnClickListener { presenter.showSendMessageLogDialog() }
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
        activity?.let { activity ->
            BankSettingsDialog().show(bank, activity)
        }
    }

    protected open fun navigateToProtectAppSettingsDialog() {
        activity?.let { activity ->
            ProtectAppSettingsDialog().show(activity)
        }
    }

    protected open fun reorderedBanks(oldPosition: Int, oldItem: TypedBankData, newPosition: Int, newItem: TypedBankData) {
        oldItem.displayIndex = oldPosition
        newItem.displayIndex = newPosition

        presenter.bankDisplayIndexUpdated(oldItem)
        presenter.bankDisplayIndexUpdated(newItem)
    }


    override val hasUnsavedChanges: Boolean
        get() = presenter.appSettings.automaticallyUpdateAccountsAfterMinutes != selectUpdateAccountsAfter.periodInMinutes
                || presenter.appSettings.lockAppAfterMinutes != selectLockAppAfter.periodInMinutes

    override fun saveChanges() {
        presenter.appSettings.automaticallyUpdateAccountsAfterMinutes = selectUpdateAccountsAfter.periodInMinutes
        presenter.appSettings.lockAppAfterMinutes = selectLockAppAfter.periodInMinutes
        presenter.appSettingsChanged()
    }

}