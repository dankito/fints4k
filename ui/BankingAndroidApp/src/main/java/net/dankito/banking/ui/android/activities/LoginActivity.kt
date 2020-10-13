package net.dankito.banking.ui.android.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.btnBiometricAuthentication
import kotlinx.android.synthetic.main.view_biometric_authentication_button.*
import net.dankito.banking.ui.android.MainActivity
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.authentication.AuthenticationService
import net.dankito.banking.ui.android.authentication.AuthenticationType
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.extensions.addEnterPressedListener
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

            edtxtLoginPassword.actualEditText.addEnterPressedListener {
                checkEnteredPasswordAndLogIn()
                true
            }

            btnLogin.setOnClickListener { checkEnteredPasswordAndLogIn() }
        }
        else {
            lytPasswordAuthentication.visibility = View.GONE

            btnBiometricAuthentication.customButtonClickHandler = {
                authenticationService.authenticateUserWithBiometric { result ->
                    if (result) {
                        btnStartBiometricAuthentication.isEnabled = false

                        biometricAuthenticationSuccessful()
                    }
                }
            }

            btnBiometricAuthentication.showBiometricPrompt()
        }
    }


    protected open fun checkEnteredPasswordAndLogIn() {
        btnLogin.isEnabled = false

        if (authenticationService.authenticateUserWithPassword(edtxtLoginPassword.chars)) {
            navigateToMainActivity()
        }
        else {
            btnLogin.isEnabled = true

            Toast.makeText(this, R.string.activity_login_incorrect_password_entered, Toast.LENGTH_SHORT).show()
        }
    }

    protected open fun biometricAuthenticationSuccessful() {
        navigateToMainActivity()
    }

    protected open fun navigateToMainActivity() {
        navigateToActivity(MainActivity::class.java)
    }

}