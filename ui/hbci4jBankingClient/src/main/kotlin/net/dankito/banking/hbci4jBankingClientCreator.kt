package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.fints.model.BankInfo
import net.dankito.utils.IThreadPool
import java.io.File


open class hbci4jBankingClientCreator : IBankingClientCreator {

    override fun createClient(
        bankInfo: BankInfo,
        customerId: String,
        pin: String,
        dataFolder: File,
        threadPool: IThreadPool,
        callback: BankingClientCallback
    ): IBankingClient {

        return hbci4jBankingClient(bankInfo, customerId, pin, dataFolder, threadPool, callback)
    }

}