package net.dankito.banking.fints4java.android

import android.os.Bundle
import android.view.Menu
import android.view.MotionEvent
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.github.clans.fab.FloatingActionMenu
import kotlinx.android.synthetic.main.activity_main.*
import net.dankito.banking.fints4java.android.di.BankingComponent
import net.dankito.banking.fints4java.android.di.BankingModule
import net.dankito.banking.fints4java.android.ui.activities.BaseActivity
import net.dankito.banking.fints4java.android.ui.views.DrawerView
import net.dankito.banking.fints4java.android.ui.views.MainActivityFloatingActionMenuButton
import net.dankito.banking.ui.presenter.BankingPresenter
import javax.inject.Inject


class MainActivity : BaseActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var drawerView: DrawerView

    private lateinit var floatingActionMenuButton: MainActivityFloatingActionMenuButton


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
        floatingActionMenuButton = MainActivityFloatingActionMenuButton(floatingActionMenu, presenter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
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
