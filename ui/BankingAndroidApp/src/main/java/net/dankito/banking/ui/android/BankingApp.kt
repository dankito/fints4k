package net.dankito.banking.ui.android

import androidx.multidex.MultiDexApplication
import net.dankito.banking.ui.android.di.BankingComponent
import net.dankito.banking.ui.android.di.BankingModule
import net.dankito.banking.ui.android.di.DaggerBankingComponent


class BankingApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        setupDependencyInjection()
    }

    private fun setupDependencyInjection() {
        val component = DaggerBankingComponent.builder()
            .bankingModule(BankingModule(this))
            .build()

        BankingComponent.component = component
    }

}