package net.dankito.banking.ui.android.di

import dagger.Component
import net.dankito.banking.ui.android.MainActivity
import net.dankito.banking.ui.android.activities.BaseActivity
import net.dankito.banking.ui.android.activities.LandingActivity
import net.dankito.banking.ui.android.activities.LoginActivity
import net.dankito.banking.ui.android.dialogs.AddAccountDialog
import net.dankito.banking.ui.android.dialogs.EnterTanDialog
import net.dankito.banking.ui.android.dialogs.SendMessageLogDialog
import net.dankito.banking.ui.android.dialogs.TransferMoneyDialog
import net.dankito.banking.ui.android.dialogs.settings.ProtectAppSettingsDialog
import net.dankito.banking.ui.android.dialogs.settings.SettingsDialog
import net.dankito.banking.ui.android.dialogs.settings.SettingsDialogBase
import net.dankito.banking.ui.android.home.HomeFragment
import net.dankito.banking.ui.android.views.BiometricAuthenticationButton
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(BankingModule::class))
interface BankingComponent {

    companion object {
        lateinit var component: BankingComponent
    }


    fun inject(baseActivity: BaseActivity)

    fun inject(landingActivity: LandingActivity)

    fun inject(loginActivity: LoginActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(homeFragment: HomeFragment)

    fun inject(addAccountDialog: AddAccountDialog)

    fun inject(enterTanDialog: EnterTanDialog)

    fun inject(transferMoneyDialog: TransferMoneyDialog)

    fun inject(settingsDialogBase: SettingsDialogBase)

    fun inject(settingsDialog: SettingsDialog)

    fun inject(protectAppSettingsDialog: ProtectAppSettingsDialog)

    fun inject(biometricAuthenticationButton: BiometricAuthenticationButton)

    fun inject(sendMessageLogDialog: SendMessageLogDialog)

}