package net.dankito.banking

import net.dankito.banking.ui.BankingClientCallback
import net.dankito.banking.ui.IBankingClient
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.ui.model.TypedBankData
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.banking.util.IAsyncRunner
import net.dankito.utils.multiplatform.File


open class hbci4jBankingClientCreator(
    protected val modelCreator: IModelCreator
) : IBankingClientCreator {

    override fun createClient(
        bank: TypedBankData,
        dataFolder: File,
        asyncRunner: IAsyncRunner,
        callback: BankingClientCallback
    ): IBankingClient {

        return hbci4jBankingClient(bank, modelCreator, dataFolder, asyncRunner, callback)
    }

}