package net.dankito.banking.fints4java.android.di

import dagger.Component
import net.dankito.banking.fints4java.android.MainActivity
import net.dankito.banking.fints4java.android.ui.activities.BaseActivity
import net.dankito.banking.fints4java.android.ui.dialogs.AddAccountDialog
import net.dankito.banking.fints4java.android.ui.dialogs.EnterTanDialog
import net.dankito.banking.fints4java.android.ui.dialogs.SendMessageLogDialog
import net.dankito.banking.fints4java.android.ui.dialogs.TransferMoneyDialog
import net.dankito.banking.fints4java.android.ui.home.HomeFragment
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

    fun inject(sendMessageLogDialog: SendMessageLogDialog)

}