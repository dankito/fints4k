package net.codinux.banking.tools.importerexporter

import net.codinux.banking.tools.importerexporter.model.AccountTransaction
import java.io.File
import java.io.Writer


interface IAccountTransactionsExporter {

    fun export(file: File, transactions: Collection<AccountTransaction>) {
        return export(file.outputStream().bufferedWriter(), transactions)
    }

    fun export(writer: Writer, transactions: Collection<AccountTransaction>)

}