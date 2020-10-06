package net.dankito.banking.ui.android.activities

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import net.dankito.banking.ui.android.MainActivity
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.authentication.AuthenticationService
import net.dankito.banking.ui.android.authentication.AuthenticationType
import net.dankito.banking.ui.android.di.BankingComponent
import javax.inject.Inject


open class LoginActivity : BaseActivity() {

    @Inject
    protected lateinit var authenticationService: AuthenticationService


    init {
        BankingComponent.component.inject(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initUi()
    }


    protected open fun initUi() {
        setContentView(R.layout.activity_login)

        if (authenticationService.authenticationType == AuthenticationType.Password) {
            lytBiometricAuthentication.visibility = View.GONE

            btnLogin.setOnClickListener { checkEnteredPasswordAndLogIn() }
        }
        else {
            lytPasswordAuthentication.visibility = View.GONE

            btnBiometricAuthentication.authenticationSuccessful = { biometricAuthenticationSuccessful() }

            btnBiometricAuthentication.showBiometricPrompt()
        }
    }


    protected open fun checkEnteredPasswordAndLogIn() {
        if (authenticationService.isCorrectUserPassword(edtxtLoginPassword.text)) {
            logIn()
        }
        else {
            Toast.makeText(this, R.string.activity_login_incorrect_password_entered, Toast.LENGTH_SHORT).show()
        }
    }

    protected open fun biometricAuthenticationSuccessful() {
        logIn()
    }

    protected open fun logIn() {
        navigateToActivity(MainActivity::class.java)
    }

}