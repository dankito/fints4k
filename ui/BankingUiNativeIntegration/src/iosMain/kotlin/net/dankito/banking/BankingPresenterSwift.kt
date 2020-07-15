package net.dankito.banking

import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.banking.persistence.NoOpBankingPersistence
import net.dankito.banking.search.NoOpRemitteeSearcher
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.IAsyncRunner
import net.dankito.banking.util.NoOpBankIconFinder
import net.dankito.banking.util.NoOpSerializer
import net.dankito.banking.util.extraction.NoOpInvoiceDataExtractor
import net.dankito.banking.util.extraction.NoOpTextExtractorRegistry
import net.dankito.utils.multiplatform.File


class BankingPresenterSwift(dataFolder: File, router: IRouter, webClient: IWebClient, asyncRunner: IAsyncRunner)
    : BankingPresenter(fints4kBankingClientCreator(NoOpSerializer(), webClient), BankFinder(), dataFolder, NoOpBankingPersistence(), router,
    NoOpRemitteeSearcher(), NoOpBankIconFinder(), NoOpTextExtractorRegistry(), NoOpInvoiceDataExtractor(), NoOpSerializer(), asyncRunner) {

}