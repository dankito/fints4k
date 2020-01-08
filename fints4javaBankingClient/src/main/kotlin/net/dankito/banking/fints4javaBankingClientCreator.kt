package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.util.IBase64Service
import net.dankito.banking.util.UiCommonBase64ServiceWrapper
import net.dankito.fints.model.BankInfo
import net.dankito.utils.IThreadPool
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient


open class fints4javaBankingClientCreator : IBankingClientCreator {

    override fun createClient(bankInfo: BankInfo, customerId: String, pin: String, webClient: IWebClient,
        base64Service: IBase64Service, threadPool: IThreadPool, callback: BankingClientCallback): IBankingClient {

        return fints4javaBankingClient(bankInfo, customerId, pin, OkHttpWebClient(), UiCommonBase64ServiceWrapper(base64Service), threadPool, callback)
    }

}