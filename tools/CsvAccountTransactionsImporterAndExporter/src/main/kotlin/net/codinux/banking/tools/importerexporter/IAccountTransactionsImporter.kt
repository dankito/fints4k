package net.codinux.banking.tools.importerexporter

import net.codinux.banking.tools.importerexporter.model.AccountTransaction
import java.io.File


interface IAccountTransactionsImporter {

    fun import(file: File): List<AccountTransaction>

}