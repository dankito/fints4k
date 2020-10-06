package net.dankito.banking.ui.android.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnNextLayout
import kotlinx.android.synthetic.main.dialog_protect_app_settings.*
import kotlinx.android.synthetic.main.dialog_protect_app_settings.view.*
import net.dankito.banking.ui.android.R
import net.dankito.banking.ui.android.authentication.AuthenticationService
import net.dankito.banking.ui.android.authentication.AuthenticationType
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.util.StandardTextWatcher
import net.dankito.utils.android.extensions.hideKeyboardDelayed
import org.slf4j.LoggerFactory
import javax.inject.Inject


open class ProtectAppSettingsDialog : SettingsDialogBase() {

    companion object {
        const val DialogTag = "ProtectAppSettingsDialog"

        private val log = LoggerFactory.getLogger(ProtectAppSettingsDialog::class.java)
    }


    @Inject
    protected lateinit var authenticationService: AuthenticationService


    init {
        BankingComponent.component.inject(this)
    }


    fun show(activity: AppCompatActivity) {
        show(activity, SettingsDialog.DialogTag)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_protect_app_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI(view)
    }

    protected open fun setupUI(rootView: View) {
        rootView.apply {
            toolbar.apply {
                setupToolbar(this, context.getString(R.string.settings), false)
            }

            val isBiometricAuthenticationSupported = authenticationService.isBiometricAuthenticationSupported

            segmentedGroup.doOnNextLayout {
                val segmentedControlButtonWidth = segmentedGroup.measuredWidth / 3
                btnShowBiometricAuthenticationSection.layoutParams.width = segmentedControlButtonWidth
                btnShowPasswordAuthenticationSection.layoutParams.width = segmentedControlButtonWidth
                btnShowRemoveAppProtectionSection.layoutParams.width = segmentedControlButtonWidth
            }

            btnShowBiometricAuthenticationSection.visibility = if (isBiometricAuthenticationSupported) View.VISIBLE else View.GONE
            btnShowBiometricAuthenticationSection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showAuthenticationLayout(rootView, lytBiometricAuthentication, false)
                }
            }

            btnShowPasswordAuthenticationSection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showAuthenticationLayout(rootView, lytPasswordAuthentication, false)
                    checkIfEnteredPasswordsMatch()
                }
            }

            btnShowRemoveAppProtectionSection.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showAuthenticationLayout(rootView, lytRemoveAppProtection, true)
                }
            }

            btnBiometricAuthentication.authenticationSuccessful = { btnSetAuthenticationMethod.isEnabled = true }

            edtxtPassword.actualEditText.addTextChangedListener(StandardTextWatcher { checkIfEnteredPasswordsMatch() } )
            edtxtPasswordConfirmation.actualEditText.addTextChangedListener(StandardTextWatcher { checkIfEnteredPasswordsMatch() } )

            btnSetAuthenticationMethod.setOnClickListener { setAuthenticationMethod() }

            if (isBiometricAuthenticationSupported && authenticationService.authenticationType == AuthenticationType.Biometric) {
                btnShowBiometricAuthenticationSection.isChecked = true
            }
            else {
                btnShowPasswordAuthenticationSection.isChecked = true
            }

        }
    }

    protected open fun showAuthenticationLayout(rootView: View, authenticationLayoutToShow: ViewGroup, isRemoveAppProtectionLayout: Boolean) {
        lytBiometricAuthentication.visibility = View.GONE
        lytPasswordAuthentication.visibility = View.GONE
        lytRemoveAppProtection.visibility = View.GONE

        authenticationLayoutToShow.visibility = View.VISIBLE

        if (isRemoveAppProtectionLayout) {
            btnSetAuthenticationMethod.setText(R.string.dialog_protect_app_settings_button_remove_app_protection_title)
            btnSetAuthenticationMethod.setBackgroundResource(R.color.destructiveColor)
            btnSetAuthenticationMethod.isEnabled = true
        }
        else {
            btnSetAuthenticationMethod.setText(R.string.dialog_protect_app_settings_button_set_new_authentication_method_title)
            btnSetAuthenticationMethod.setBackgroundResource(R.drawable.conditionally_disabled_view_background)
            btnSetAuthenticationMethod.isEnabled = false
        }

        authenticationLayoutToShow.hideKeyboardDelayed(10)
    }


    protected open fun checkIfEnteredPasswordsMatch() {
        val enteredPassword = edtxtPassword.text

        if (enteredPassword.isNotBlank() && enteredPassword == edtxtPasswordConfirmation.text) {
            btnSetAuthenticationMethod.isEnabled = true
        }
        else {
            btnSetAuthenticationMethod.isEnabled = false
        }
    }

    protected open fun setAuthenticationMethod() {
        when {
            btnShowBiometricAuthenticationSection.isChecked -> authenticationService.setAuthenticationMethodToBiometric()
            btnShowPasswordAuthenticationSection.isChecked -> authenticationService.setAuthenticationMethodToPassword(edtxtPassword.text)
            btnShowRemoveAppProtectionSection.isChecked -> authenticationService.removeAppProtection()
            else -> log.error("This should never occur! Has there a new authentication method been added?")
        }

        closeDialog()
    }


    override val hasUnsavedChanges: Boolean
        get() = false

    override fun saveChanges() {

    }

}