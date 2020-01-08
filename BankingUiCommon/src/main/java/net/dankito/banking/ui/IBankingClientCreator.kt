package net.dankito.banking.ui

import net.dankito.banking.util.IBase64Service
import net.dankito.fints.model.BankInfo
import net.dankito.utils.IThreadPool
import net.dankito.utils.web.client.IWebClient


interface IBankingClientCreator {

    fun createClient(bankInfo: BankInfo, // TODO: create own value object to get rid off fints4java dependency
                     customerId: String,
                     pin: String,
                     webClient: IWebClient, // TODO: wrap away JavaUtils IWebClient
                     base64Service: IBase64Service,
                     threadPool: IThreadPool, // TODO: wrap away JavaUtils IThreadPool
                     callback: BankingClientCallback
    ): IBankingClient

}