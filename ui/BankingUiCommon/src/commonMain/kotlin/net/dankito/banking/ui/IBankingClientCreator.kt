package net.dankito.banking.ui

import net.dankito.utils.multiplatform.File
import net.dankito.banking.ui.model.TypedCustomer
import net.dankito.banking.util.IAsyncRunner


interface IBankingClientCreator {

    fun createClient(
        customer: TypedCustomer,
        dataFolder: File,
        asyncRunner: IAsyncRunner,
        callback: BankingClientCallback
    ): IBankingClient

}