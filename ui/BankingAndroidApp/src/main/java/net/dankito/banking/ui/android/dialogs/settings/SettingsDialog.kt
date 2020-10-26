package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_settings.*
import kotlinx.android.synthetic.main.dialog_settings.view.*
import net.codinux.banking.tools.importerexporter.CsvAccountTransactionsExporter
import net.codinux.banking.tools.importerexporter.model.AccountTransaction
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.adapter.BankDataAdapterItem
import net.dankito.banking.ui.android.adapter.FastAdapterRecyclerView
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.filechooserdialog.FileChooserDialog
import net.dankito.filechooserdialog.model.FileChooserDialogConfig
import net.dankito.utils.android.permissions.IPermissionsService
import java.io.File
import java.text.SimpleDateFormat
import javax.inject.Inject


open class SettingsDialog : SettingsDialogBase() {

    companion object {
        val ExportTransactionsDateFormat = SimpleDateFormat("yyyyMMdd")

        const val DialogTag = "SettingsDialog"
    }


    @Inject
    protected lateinit var permissionsService: IPermissionsService

    protected lateinit var banksAdapter: FastAdapterRecyclerView<BankDataAdapterItem>

    protected var banksChangedListener = { _: List<TypedBankData> ->
        updateBanksAdapterItems()
    }


    init {
        BankingComponent.component.inject(this)
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

            btnAddAccount.setOnClickListener { presenter.showAddAccountDialog() }

            selectUpdateAccountsAfter.periodInMinutes = presenter.appSettings.automaticallyUpdateAccountsAfterMinutes

            btnSetAppProtection.setOnClickListener { navigateToProtectAppSettingsDialog() }
            selectLockAppAfter.periodInMinutes = presenter.appSettings.lockAppAfterMinutes

            btnExportAccountTransactions.setOnClickListener { exportAccountTransactions() }

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
        activity?.runOnUiThread {
            banksAdapter.setItems(createBanksAdapterItems())
        }
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


    protected open fun exportAccountTransactions() {
        val initialDirectory = presenter.appSettings.lastSelectedExportFolder?.let { File(it) }
        val suggestedFilename = getExportCsvSuggestedFilename()

//        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        intent.type = "text/csv"
//
//        intent.putExtra(Intent.EXTRA_TITLE, suggestedFilename)
//
//        startActivityForResult(intent, 1)

        activity?.let { activity ->
            val config = FileChooserDialogConfig(initialDirectory = initialDirectory, suggestedFilenameForSaveFileDialog = suggestedFilename)
            FileChooserDialog().showSaveFileInFullscreenDialog(activity, permissionsService, config) { _, selectedFile ->
                selectedFile?.let {
                    val transactions = presenter.allTransactions.map { mapTransaction(it) }

                    CsvAccountTransactionsExporter().export(selectedFile, transactions)

                    presenter.appSettings.lastSelectedExportFolder = selectedFile.parentFile.absolutePath
                    presenter.appSettingsChanged()
                }
            }
        }
    }

    // TODO: this is almost the same code as in JavaFX MainMenuBar.getExportCsvSuggestedFilename() -> merge
    protected open fun getExportCsvSuggestedFilename(): String? {
        val transactions = presenter.allTransactions
        val transactionsDates = transactions.map { it.valueDate }
        val transactionsStartDate = transactionsDates.min()
        val transactionsEndDate = transactionsDates.max()

        return context?.getString(R.string.dialog_settings_export_account_transactions_suggested_file_name,
            transactionsStartDate?.let { ExportTransactionsDateFormat.format(it) } ?: "", transactionsEndDate?.let { ExportTransactionsDateFormat.format(it) } ?: "")
    }

    // TODO: this is exactly the same code as in JavaFX MainMenuBar.mapTransaction() -> merge
    protected open fun mapTransaction(transaction: IAccountTransaction): AccountTransaction {
        return AccountTransaction(
            transaction.account.iban ?: transaction.account.identifier,
            transaction.amount,
            transaction.currency,
            transaction.reference,
            transaction.bookingDate,
            transaction.valueDate,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText
        )
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