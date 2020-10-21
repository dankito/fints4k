package net.dankito.banking.ui.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MotionEvent
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.os.postDelayed
import androidx.drawerlayout.widget.DrawerLayout
import com.github.clans.fab.FloatingActionMenu
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import net.codinux.banking.tools.epcqrcode.ParseEpcQrCodeResult
import net.dankito.banking.ui.android.activities.BaseActivity
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.di.BankingModule
import net.dankito.banking.ui.android.views.DrawerView
import net.dankito.banking.ui.android.views.MainActivityFloatingActionMenuButton
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.android.permissions.IPermissionsService
import net.dankito.utils.android.permissions.PermissionsService
import javax.inject.Inject


class MainActivity : BaseActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var drawerView: DrawerView

    private lateinit var floatingActionMenuButton: MainActivityFloatingActionMenuButton

    private val permissionsService: IPermissionsService = PermissionsService(this)


    @Inject
    protected lateinit var presenter: BankingPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BankingModule.mainActivity = this

        BankingComponent.component.inject(this)

        initUi()
    }

    private fun initUi() {
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
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

        slider?.let { slider ->
            drawerView = DrawerView(this, slider, presenter)
        }

        val floatingActionMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenu)
        floatingActionMenuButton = MainActivityFloatingActionMenuButton(floatingActionMenu, permissionsService, presenter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults)

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanQrCodeResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (scanQrCodeResult != null) {
            // at this point camera activity is still displayed and not returned yet to MainActivity -> app would crash if we don't wait
            Handler(Looper.getMainLooper()).postDelayed(250) {
                handleQrCodeScanResult(scanQrCodeResult)
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleQrCodeScanResult(scanQrCodeResult: IntentResult) {
        scanQrCodeResult.contents?.let { decodedQrCode ->
            val result = presenter.showTransferMoneyDialogWithDataFromQrCode(decodedQrCode)

            if (result.successful == false) {
                showParseQrCodeError(result)
            }
        }
    }

    protected fun showParseQrCodeError(result: ParseEpcQrCodeResult) {
        // TODO: show localized error message that matches ParseEpcQrCodeResultCode
        val errorMessage = getString(R.string.money_transfer_from_scanning_qr_code_error, result.error, result.decodedQrCode)

        AlertDialog.Builder(this)
            .setMessage(errorMessage)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }


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
