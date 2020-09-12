package net.dankito.banking.ui.android.di

import dagger.Component
import net.dankito.banking.ui.android.MainActivity
import net.dankito.banking.ui.android.activities.BaseActivity
import net.dankito.banking.ui.android.dialogs.AddAccountDialog
import net.dankito.banking.ui.android.dialogs.EnterTanDialog
import net.dankito.banking.ui.android.dialogs.SendMessageLogDialog
import net.dankito.banking.ui.android.dialogs.TransferMoneyDialog
import net.dankito.banking.ui.android.dialogs.settings.BankSettingsDialog
import net.dankito.banking.ui.android.home.HomeFragment
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(BankingModule::class))
interface BankingComponent {

    companion object {
        lateinit var component: BankingComponent
    }


    fun inject(baseActivity: BaseActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(homeFragment: HomeFragment)

    fun inject(addAccountDialog: AddAccountDialog)

    fun inject(enterTanDialog: EnterTanDialog)

    fun inject(transferMoneyDialog: TransferMoneyDialog)

    fun inject(bankSettingsDialog: BankSettingsDialog)

    fun inject(sendMessageLogDialog: SendMessageLogDialog)

}