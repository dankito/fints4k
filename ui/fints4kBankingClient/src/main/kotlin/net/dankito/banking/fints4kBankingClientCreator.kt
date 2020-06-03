package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.util.IBase64Service
import net.dankito.banking.bankfinder.BankInfo
import net.dankito.utils.IThreadPool
import net.dankito.utils.web.client.IWebClient
import java.io.File


open class fints4kBankingClientCreator(
    protected val webClient: IWebClient, // TODO: remove?
    protected val base64Service: IBase64Service // TODO: remove?
) : IBankingClientCreator {

    override fun createClient(
        bankInfo: BankInfo,
        customerId: String,
        pin: String,
        dataFolder: File,
        threadPool: IThreadPool, // TODO: remove?
        callback: BankingClientCallback
    ): IBankingClient {

        return fints4kBankingClient(bankInfo, customerId, pin, dataFolder, callback = callback)
    }

}