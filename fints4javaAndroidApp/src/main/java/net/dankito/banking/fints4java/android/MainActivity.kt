package net.dankito.banking.fints4java.android

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import androidx.navigation.findNavController
import net.dankito.banking.fints4java.android.mapper.fints4javaModelMapper
import net.dankito.banking.fints4java.android.ui.MainWindowPresenter
import net.dankito.banking.fints4java.android.ui.dialogs.AddAccountDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterAtcDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterTanDialog
import net.dankito.fints.FinTsClientCallback
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.model.CustomerData
import net.dankito.fints.model.EnterTanGeneratorAtcResult
import net.dankito.fints.model.EnterTanResult
import net.dankito.fints.model.TanChallenge
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


class MainActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration

    val presenter = MainWindowPresenter(Base64ServiceAndroid(), object : FinTsClientCallback {

        override fun enterTan(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
            return getTanFromUserOffUiThread(customer, tanChallenge)
        }

        override fun enterTanGeneratorAtc(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
            return getAtcFromUserOffUiThread(customer, tanMedium)
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

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            AddAccountDialog().show(this, presenter)
        }

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
//        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }

    private fun getTanFromUserOffUiThread(customer: CustomerData, tanChallenge: TanChallenge): EnterTanResult {
        val enteredTan = AtomicReference<EnterTanResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        val account = presenter.getAccountForCustomer(customer)

        runOnUiThread {
            EnterTanDialog().show(account, tanChallenge, presenter, this@MainActivity, false) {
                enteredTan.set(it)
                tanEnteredLatch.countDown()
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return enteredTan.get()
    }

    private fun getAtcFromUserOffUiThread(customer: CustomerData, tanMedium: TanGeneratorTanMedium): EnterTanGeneratorAtcResult {
        val result = AtomicReference<EnterTanGeneratorAtcResult>(null)
        val tanEnteredLatch = CountDownLatch(1)

        runOnUiThread {
            // TODO: don't create a fints4javaModelMapper instance here, let MainWindowPresenter do the job
            EnterAtcDialog().show(fints4javaModelMapper().mapTanMedium(tanMedium), this@MainActivity, false) { enteredResult ->
                result.set(enteredResult)
                tanEnteredLatch.countDown()
            }
        }

        try { tanEnteredLatch.await() } catch (ignored: Exception) { }

        return result.get()
    }

}
