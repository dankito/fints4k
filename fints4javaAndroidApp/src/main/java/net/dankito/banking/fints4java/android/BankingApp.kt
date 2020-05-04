package net.dankito.banking.fints4java.android

import android.app.Application
import net.dankito.banking.fints4java.android.di.BankingComponent
import net.dankito.banking.fints4java.android.di.BankingModule
import net.dankito.banking.fints4java.android.di.DaggerBankingComponent


class BankingApp : Application() {

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