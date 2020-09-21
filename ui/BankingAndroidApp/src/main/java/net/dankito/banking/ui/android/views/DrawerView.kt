package net.dankito.banking.ui.android.views

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.util.addItemsAtPosition
import com.mikepenz.materialdrawer.util.getDrawerItem
import com.mikepenz.materialdrawer.util.removeItemByPosition
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.dialogs.settings.BankSettingsDialog
import net.dankito.banking.ui.android.extensions.withIcon
import net.dankito.banking.ui.model.TypedCustomer
import net.dankito.banking.ui.presenter.BankingPresenter
import org.slf4j.LoggerFactory


open class DrawerView(
    protected val activity: AppCompatActivity,
    protected val slider: MaterialDrawerSliderView,
    protected val presenter: BankingPresenter
) {

    companion object {
        private const val AccountLevel = 2

        private const val BankAccountLevel = 7

        private const val AccountsSectionHeaderId = 1000L
        private const val AllAccountsId = 1001L
        private const val AddAccountId = 1002L

        private const val CountDefaultAccountItems = 5
        private const val CountDefaultAccountItemsAtTop = 3


        private val log = LoggerFactory.getLogger(DrawerView::class.java)
    }


    init {
        setupDrawerView()
    }


    protected open fun setupDrawerView() {
        slider.headerView = activity.layoutInflater.inflate(R.layout.nav_header_main, null)

        showAppVersion(slider.headerView)

        setDefaultDrawerItems()

        presenter.addAccountsChangedListener {
            activity.runOnUiThread { updateDrawerItems() }
        }

        presenter.addSelectedBankAccountsChangedListener {
            activity.runOnUiThread { updateDrawerItems() }
        }

        updateDrawerItems()
    }

    private fun setDefaultDrawerItems() {
        slider.apply {
            addItems(
                SectionDrawerItem()
                    .withName(R.string.drawer_menu_bank_accounts_section_title)
                    .withIdentifier(AccountsSectionHeaderId)
                    .withDivider(false)
                ,

                PrimaryDrawerItem()
                    .withName(R.string.drawer_menu_all_bank_accounts_title)
                    .withIdentifier(AllAccountsId)
                    .withLevel(AccountLevel)
                    .withSelected(true)
                    .withIcon(activity, GoogleMaterial.Icon.gmd_account_balance, R.color.primaryTextColor_Dark)
                    .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.selectedAllBankAccounts() } }
                ,

                PrimaryDrawerItem()
                    .withName(R.string.add_account)
                    .withIdentifier(AddAccountId)
                    .withLevel(AccountLevel)
                    .withIcon(activity, GoogleMaterial.Icon.gmd_add, R.color.primaryTextColor_Dark)
                    .withSelectable(false)
                    .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.showAddAccountDialog() } }
                ,

                DividerDrawerItem()
                    .withSelectable(false)
                ,

                PrimaryDrawerItem()
                    .withName(R.string.drawer_menu_send_message_log_title)
                    .withIcon(activity, GoogleMaterial.Icon.gmd_mail, R.color.primaryTextColor_Dark)
                    .withSelectable(false)
                    .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.showSendMessageLogDialog() } }

            )
        }
    }

    private fun updateDrawerItems() {
        // removes previously shown accounts; index 1 = 'Accounts header', 1 = 'All accounts', index 2 = 'Add account', don't remove these
        while (slider.itemAdapter.adapterItems.size > CountDefaultAccountItems) {
            slider.removeItemByPosition(CountDefaultAccountItemsAtTop)
        }

        val accountItems = createAccountsDrawerItems()

        slider.addItemsAtPosition(CountDefaultAccountItemsAtTop, *accountItems.toTypedArray())

        slider.getDrawerItem(AllAccountsId)?.let { allAccountsItem ->
            if (presenter.areAllAccountSelected) slider.selectExtension.select(allAccountsItem, false)
            else slider.selectExtension.deselectByItems(setOf(allAccountsItem))
        }
    }

    private fun createAccountsDrawerItems(): List<IDrawerItem<*>> {
        return presenter.customers.map { account ->
            val accountItem = createAccountDrawerItem(account)

            val bankAccountsItems = createBankAccountsDrawerItems(account).toMutableList()
            bankAccountsItems.add(0, accountItem)

            return@map bankAccountsItems
        }.flatten()
    }

    private fun createAccountDrawerItem(customer: TypedCustomer): IDrawerItem<*> {

        val accountItem = AccountDrawerItem()
            .withName(customer.displayName)
            .withLevel(AccountLevel)
            .withSecondaryIcon(R.drawable.ic_baseline_settings_24)
            .withSecondaryIconColor(activity, R.color.primaryTextColor_Dark)
            .withOnSecondaryIconClickedListener { closeDrawerAndEditAccount(customer) }
            .withIcon(customer.iconUrl ?: "")
            .withSelected(presenter.isSingleSelectedAccount(customer))
            .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.selectedAccount(customer) } }

        if (customer.iconUrl == null) {
            accountItem.withIcon(activity, FontAwesome.Icon.faw_piggy_bank, R.color.primaryTextColor_Dark)
        }


        return accountItem
    }

    private fun createBankAccountsDrawerItems(customer: TypedCustomer): List<IDrawerItem<*>> {
        return customer.accounts.map { bankAccount ->
            SecondaryDrawerItem()
                .withName(bankAccount.displayName)
                .withLevel(BankAccountLevel)
                .withSelected(presenter.isSingleSelectedBankAccount(bankAccount))
                .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.selectedBankAccount(bankAccount) } }
        }
    }

    private fun itemClicked(action: () -> Unit): Boolean {
        action()

        return false
    }

    private fun closeDrawerAndEditAccount(customer: TypedCustomer) {
        closeDrawer()

        editAccount(customer)
    }

    private fun editAccount(customer: TypedCustomer) {
        BankSettingsDialog().show(customer, activity, true)
    }

    private fun showAppVersion(navigationHeaderView: View?) {
        try {
            val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            val version = packageInfo.versionName
            (navigationHeaderView?.findViewById(R.id.txtAppVersion) as? TextView)?.text = version
        } catch (e: Exception) {
            log.error("Could not read application version")
        }
    }

    private fun closeDrawer() {
        val drawerLayout = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
    }

}