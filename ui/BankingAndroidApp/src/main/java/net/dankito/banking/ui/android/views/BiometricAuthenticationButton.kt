package net.dankito.banking.ui.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_biometric_authentication_button.view.*
import net.dankito.banking.ui.android.R


open class BiometricAuthenticationButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    open var authenticationSuccessful: (() -> Unit)? = null


    init {
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
        authenticationSuccessful?.invoke()
    }

}