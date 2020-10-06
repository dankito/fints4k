package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_biometric_authentication_button.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.authentication.IBiometricAuthenticationService
import net.dankito.banking.ui.android.di.BankingComponent
import javax.inject.Inject


open class BiometricAuthenticationButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    @Inject
    protected lateinit var biometricAuthenticationService: IBiometricAuthenticationService


    open var authenticationSuccessful: (() -> Unit)? = null


    init {
        BankingComponent.component.inject(this)

        setupUi(context)
    }

    private fun setupUi(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.view_biometric_authentication_button, this, true)

        rootView.apply {
            btnStartBiometricAuthentication.setOnClickListener { doBiometricAuthenticationAndLogIn() }
        }
    }

    protected open fun doBiometricAuthenticationAndLogIn() {
        biometricAuthenticationService.authenticate { result ->
            if (result.successful) {
                authenticationSuccessful?.invoke()
            }
        }
    }


    open fun showBiometricPrompt() {
        doBiometricAuthenticationAndLogIn()
    }

}