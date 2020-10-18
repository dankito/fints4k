package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.banking.fints.webclient.KtorWebClient
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.banking.util.IAsyncRunner
import net.dankito.banking.util.ISerializer
import net.dankito.utils.multiplatform.File


open class fints4kBankingClientCreator(
    protected val modelCreator: IModelCreator,
    protected val serializer: ISerializer,
    protected val webClient: IWebClient = KtorWebClient()
) : IBankingClientCreator {

    override fun createClient(
        bank: TypedBankData,
        dataFolder: File,
        asyncRunner: IAsyncRunner,
        callback: BankingClientCallback
    ): IBankingClient {

        return fints4kBankingClient(bank, modelCreator, dataFolder, serializer, webClient, callback = callback)
    }

}