package net.dankito.banking.ui.android.views

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import net.dankito.banking.ui.android.dialogs.settings.SettingsDialog
import net.dankito.banking.ui.android.extensions.toDrawable
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.extensions.getColorFromResource
import org.slf4j.LoggerFactory


open class DrawerView(
    protected val activity: AppCompatActivity,
    protected val slider: MaterialDrawerSliderView,
    protected val presenter: BankingPresenter
) {

    companion object {
        private const val BankLevel = 2

        private const val AccountLevel = 7

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

        presenter.addBanksChangedListener {
            activity.runOnUiThread { updateDrawerItems() }
        }

        presenter.addSelectedAccountsChangedListener {
            activity.runOnUiThread { updateDrawerItems() }
        }

        updateDrawerItems()
    }

    private fun setDefaultDrawerItems() {
        slider.apply {
            addItems(
                SectionDrawerItem()
                    .withName(R.string.accounts)
                    .withIdentifier(AccountsSectionHeaderId)
                    .withDivider(false)
                ,

                PrimaryDrawerItem()
                    .withName(R.string.drawer_menu_all_bank_accounts_title)
                    .withIdentifier(AllAccountsId)
                    .withLevel(BankLevel)
                    .withSelected(true)
                    .withIcon(getVectorDrawable(R.drawable.ic_accounts, R.color.primaryTextColor_Dark))
                    .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.selectedAllAccounts() } }
                ,

                PrimaryDrawerItem()
                    .withName(R.string.add_account)
                    .withIdentifier(AddAccountId)
                    .withLevel(BankLevel)
                    .withIcon(getVectorDrawable(R.drawable.ic_baseline_add_24, R.color.primaryTextColor_Dark))
                    .withSelectable(false)
                    .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.showAddAccountDialog() } }
                ,

                DividerDrawerItem()
                    .withSelectable(false)
                ,

                PrimaryDrawerItem()
                    .withName(R.string.settings)
                    .withIcon(getVectorDrawable(R.drawable.ic_baseline_settings_24, R.color.primaryTextColor_Dark))
                    .withSelectable(false)
                    .withOnDrawerItemClickListener { _, _, _ -> itemClicked { SettingsDialog().show(activity) } }

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
        return presenter.allBanksSortedByDisplayIndex.map { account ->
            val accountItem = createAccountDrawerItem(account)

            val accountsItems = createBankAccountsDrawerItems(account).toMutableList()
            accountsItems.add(0, accountItem)

            return@map accountsItems
        }.flatten()
    }

    private fun createAccountDrawerItem(bank: TypedBankData): IDrawerItem<*> {

        val accountItem = AccountDrawerItem()
            .withName(bank.displayName)
            .withLevel(BankLevel)
            .withSecondaryIcon(getVectorDrawable(R.drawable.ic_baseline_settings_24, R.color.primaryTextColor_Dark))
            .withOnSecondaryIconClickedListener { closeDrawerAndEditAccount(bank) }
            .withIcon(bank.iconData?.toDrawable(activity.resources))
            .withSelected(presenter.isSingleSelectedBank(bank))
            .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.selectedBank(bank) } }

        if (bank.iconData == null) {
            accountItem.withIcon(getVectorDrawable(R.drawable.ic_accounts, R.color.primaryTextColor_Dark))
        }


        return accountItem
    }

    private fun createBankAccountsDrawerItems(bank: TypedBankData): List<IDrawerItem<*>> {
        return bank.visibleAccountsSorted.map { account ->
            SecondaryDrawerItem()
                .withName(account.displayName)
                .withLevel(AccountLevel)
                .withSelected(presenter.isSingleSelectedAccount(account))
                .withOnDrawerItemClickListener { _, _, _ -> itemClicked { presenter.selectedAccount(account) } }
        }
    }

    private fun itemClicked(action: () -> Unit): Boolean {
        action()

        return false
    }

    private fun closeDrawerAndEditAccount(bank: TypedBankData) {
        closeDrawer()

        editAccount(bank)
    }

    private fun editAccount(bank: TypedBankData) {
        BankSettingsDialog().show(bank, activity)
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


    private fun getVectorDrawable(@DrawableRes drawableResId: Int, @ColorRes tintColorResId: Int? = null): Drawable? {
        val drawable = AppCompatResources.getDrawable(activity, drawableResId)

        if (tintColorResId != null && drawable != null) {
            DrawableCompat.setTint(drawable, activity.getColorFromResource(tintColorResId))
        }

        return drawable
    }

}