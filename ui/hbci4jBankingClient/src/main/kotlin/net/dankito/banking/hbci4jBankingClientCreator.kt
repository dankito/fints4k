package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.ui.model.Customer
import net.dankito.banking.util.IAsyncRunner
import net.dankito.utils.multiplatform.File


open class hbci4jBankingClientCreator : IBankingClientCreator {

    override fun createClient(
        customer: Customer,
        dataFolder: File,
        asyncRunner: IAsyncRunner,
        callback: BankingClientCallback
    ): IBankingClient {

        return hbci4jBankingClient(customer, dataFolder, asyncRunner, callback)
    }

}