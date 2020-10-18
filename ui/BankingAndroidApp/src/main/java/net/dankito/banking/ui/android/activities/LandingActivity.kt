package net.dankito.banking.ui.android.activities

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.dankito.banking.ui.android.MainActivity
import net.dankito.banking.ui.android.authentication.AuthenticationService
import net.dankito.banking.ui.android.authentication.AuthenticationType
import net.dankito.banking.ui.android.di.BankingComponent
import javax.inject.Inject


open class LandingActivity : AppCompatActivity() {

    @Inject
    protected lateinit var authenticationService: AuthenticationService


    init {
        BankingComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authenticationService.authenticationType == AuthenticationType.None) {
            launchActivity(MainActivity::class.java)
        }
        else {
            launchActivity(LoginActivity::class.java)
        }
    }


    protected open fun <T : Activity> launchActivity(activityClass: Class<T>) {
        navigateToActivity(activityClass)

        finish()
    }

}