package net.dankito.banking

import net.dankito.banking.bankfinder.InMemoryBankFinder
import net.dankito.banking.fints.webclient.IWebClient
import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.banking.search.NoOpRemitteeSearcher
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.IAsyncRunner
import net.dankito.banking.util.NoOpBankIconFinder
import net.dankito.banking.util.NoOpSerializer
import net.dankito.banking.util.extraction.NoOpInvoiceDataExtractor
import net.dankito.banking.util.extraction.NoOpTextExtractorRegistry
import net.dankito.utils.multiplatform.File


class BankingPresenterSwift(dataFolder: File, router: IRouter, webClient: IWebClient, persistence: IBankingPersistence, asyncRunner: IAsyncRunner)
    : BankingPresenter(fints4kBankingClientCreator(NoOpSerializer(), webClient), InMemoryBankFinder(), dataFolder, persistence, router,
    NoOpRemitteeSearcher(), NoOpBankIconFinder(), NoOpTextExtractorRegistry(), NoOpInvoiceDataExtractor(), NoOpSerializer(), asyncRunner) {

}