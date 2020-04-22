package net.dankito.banking.fints4java.android

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SubMenu
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.action_view_account_menu_item.view.*
import net.dankito.banking.fints4java.android.ui.views.MainActivityFloatingActionMenuButton
import net.dankito.banking.fints4java.android.util.Base64ServiceAndroid
import net.dankito.banking.fints4javaBankingClientCreator
import net.dankito.banking.persistence.BankingPersistenceJson
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.web.client.OkHttpWebClient
import org.slf4j.LoggerFactory
import java.io.File


class MainActivity : AppCompatActivity() {

    companion object {
        private val log = LoggerFactory.getLogger(MainActivity::class.java)
    }


//    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var floatingActionMenuButton: MainActivityFloatingActionMenuButton


    lateinit var presenter: BankingPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataFolder = File(this.filesDir, "data/accounts")

        presenter = BankingPresenter(fints4javaBankingClientCreator(OkHttpWebClient(), Base64ServiceAndroid()), dataFolder,
            BankingPersistenceJson(File(dataFolder, "accounts.json")), RouterAndroid(this))

        initUi()
    }

    private fun initUi() {
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
//        val navController = findNavController(R.id.nav_host_fragment)

//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
//                R.id.nav_tools, R.id.nav_share, R.id.nav_send
//            ), drawerLayout
//        )
//
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navigationView.setupWithNavController(navController)

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        setupNavigationView(navigationView, drawerLayout)

        val floatingActionMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenu)
        floatingActionMenuButton = MainActivityFloatingActionMenuButton(floatingActionMenu, presenter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }


    private fun setupNavigationView(navigationView: NavigationView, drawerLayout: DrawerLayout) {
        showAppVersion(navigationView)

        val navigationMenu = navigationView.menu
        val accountsMenuItem = navigationMenu.findItem(R.id.navBankAccountsSectionItem)
        val accountsMenu = accountsMenuItem.subMenu

        presenter.addAccountsChangedListener {
            runOnUiThread { updateNavigationMenuItems(accountsMenu) }
        }

        updateNavigationMenuItems(accountsMenu)

        navigationView.setNavigationItemSelectedListener { navigationItemSelected(it) }
    }

    private fun updateNavigationMenuItems(accountsMenu: SubMenu) {
        accountsMenu.findItem(R.id.navAllBankAccounts).isVisible = presenter.accounts.isNotEmpty()

        // removes previously shown accounts; index 0 = 'Add account', 1 = 'All accounts', don't remove these
        for (index in (accountsMenu.size() - 1) downTo 2) {
            accountsMenu.removeItem(accountsMenu.getItem(index).itemId)
        }

        presenter.accounts.forEach { account ->
            val accountMenuItem = accountsMenu.add("")

            accountMenuItem.setActionView(R.layout.action_view_account_menu_item)
            accountMenuItem.actionView.txtvwAccountName.text = account.displayName
            accountMenuItem.actionView.imgvwEditAccount.setImageResource(R.drawable.ic_build_white_48dp)
            accountMenuItem.actionView.imgvwEditAccount.setOnClickListener { editAccount(account) }

            accountMenuItem.setOnMenuItemClickListener { setSelectedAccount(account) }
        }
    }

    private fun setSelectedAccount(account: Account): Boolean {
        presenter.selectedAccount(account)

        closeDrawer()

        return true
    }

    private fun editAccount(account: Account) {
        // TODO: implement
        log.info("Edit account $account")

        closeDrawer()
    }

    private fun showAppVersion(navigationView: NavigationView) {
        try {
            val packageInfo = this.packageManager.getPackageInfo(packageName, 0)
            val version = packageInfo.versionName
            (navigationView.getHeaderView(0).findViewById(R.id.txtAppVersion) as? TextView)?.text = version
        } catch (e: Exception) {
            log.error("Could not read application version")
        }
    }

    private fun navigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navAddBankAccount -> presenter.showAddAccountDialog()
            R.id.navAllBankAccounts -> presenter.selectedAllBankAccounts()
        }

        closeDrawer()

        return true
    }

    private fun closeDrawer() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if(floatingActionMenuButton.handlesTouch(event)) { // close menu when menu is opened and touch is outside floatingActionMenuButton
            return true
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onBackPressed() {
        if (floatingActionMenuButton.handlesBackButtonPress()) { // close menu when menu is opened and back button gets pressed
            return
        }

        super.onBackPressed()
    }

}
