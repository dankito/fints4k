package net.dankito.banking.ui

import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.util.IAsyncRunner
import java.io.File


interface IBankingClientCreator {

    fun createClient(
        bankInfo: BankInfo, // TODO: create own value object to get rid off fints4k dependency
        customerId: String,
        pin: String,
        dataFolder: File,
        asyncRunner: IAsyncRunner,
        callback: BankingClientCallback
    ): IBankingClient

}