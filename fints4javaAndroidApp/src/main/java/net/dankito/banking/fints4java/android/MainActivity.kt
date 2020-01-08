package net.dankito.banking.fints4java.android

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import androidx.navigation.findNavController
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.action_view_account_menu_item.view.*
import net.dankito.banking.fints4java.android.ui.MainWindowPresenter
import net.dankito.banking.fints4java.android.ui.dialogs.AddAccountDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterAtcDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterTanDialog
import net.dankito.banking.fints4java.android.ui.views.MainActivityFloatingActionMenuButton
import net.dankito.banking.fints4javaBankingClientCreator
import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.tan.EnterTanGeneratorAtcResult
import net.dankito.banking.ui.model.tan.EnterTanResult
import net.dankito.banking.ui.model.tan.TanChallenge
import net.dankito.banking.ui.model.tan.TanGeneratorTanMedium
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


class MainActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var floatingActionMenuButton: MainActivityFloatingActionMenuButton


    val presenter = MainWindowPresenter(fints4javaBankingClientCreator(), Base64ServiceAndroid(), object : BankingClientCallback {

        override fun enterTan(account: Account, tanChallenge: TanChallenge): EnterTanResult {
            return getTanFromUserOffUiThread(account, tanChallenge)
        }

        override fun enterTanGeneratorAtc(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            return getAtcFromUserOffUiThread(tanMedium)
        }

    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUi()
    }

    private fun initUi() {
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

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

        val floatingActionMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenu)
        floatingActionMenuButton = MainActivityFloatingActionMenuButton(floatingActionMenu, presenter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
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


    private fun getTanFromUserOffUiThread(account: Account, tanChallenge: TanChallenge): EnterTanResult {
        val enteredTan = AtomicReference<EnterTanResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        runOnUiThread {
            EnterTanDialog().show(account, tanChallenge, presenter, this@MainActivity, false) {
                enteredTan.set(it)
                tanEnteredLatch.countDown()
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return enteredTan.get()
    }

    private fun getAtcFromUserOffUiThread(tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        val result = AtomicReference<EnterTanGeneratorAtcResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        runOnUiThread {
            EnterAtcDialog().show(tanMedium, this@MainActivity, false) { enteredResult ->
                result.set(enteredResult)
                tanEnteredLatch.countDown()
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return result.get()
    }

}
