package net.dankito.banking.ui.android.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.util.CurrentActivityTracker
import org.slf4j.LoggerFactory
import javax.inject.Inject


abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private val log = LoggerFactory.getLogger(BaseActivity::class.java)
    }


    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker


    init {
        BankingComponent.component.inject(this)
    }


    open val isRunningOnTablet: Boolean
        get() = resources.getBoolean(R.bool.isTablet)


    override fun onCreate(savedInstanceState: Bundle?) {
        log.info("Creating Activity $this")

        // a bit bad as it not clearly visible that orientation lock is done here
        if (isRunningOnTablet == false) { // lock screen to portrait mode on phones as there app really looks bad and some EditTexts are almost unusable
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        currentActivityTracker.currentActivity = this

        log.info("Started Activity $this")
    }

    override fun onResume() {
        super.onResume()

        currentActivityTracker.currentActivity = this

        log.info("Resumed Activity $this")
    }

    override fun onPause() {
        log.info("Paused Activity $this")

        super.onPause()
    }

    override fun onStop() {
        log.info("Stopped Activity $this")

        super.onStop()
    }

    override fun onDestroy() {
        log.info("Destroyed Activity $this")

        super.onDestroy()
    }

}