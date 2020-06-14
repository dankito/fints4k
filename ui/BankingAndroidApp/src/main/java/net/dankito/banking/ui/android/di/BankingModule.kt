package net.dankito.banking.ui.android.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import net.dankito.banking.ui.android.RouterAndroid
import net.dankito.banking.ui.android.util.CurrentActivityTracker
import net.dankito.banking.ui.android.util.Base64ServiceAndroid
import net.dankito.banking.fints4kBankingClientCreator
import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.banking.persistence.LuceneBankingPersistence
import net.dankito.banking.search.IRemitteeSearcher
import net.dankito.banking.search.LuceneRemitteeSearcher
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.BankIconFinder
import net.dankito.banking.util.IBankIconFinder
import net.dankito.banking.bankfinder.IBankFinder
import net.dankito.banking.bankfinder.LuceneBankFinder
import net.dankito.text.extraction.ITextExtractorRegistry
import net.dankito.text.extraction.TextExtractorRegistry
import net.dankito.text.extraction.info.invoice.IInvoiceDataExtractor
import net.dankito.text.extraction.info.invoice.InvoiceDataExtractor
import net.dankito.text.extraction.pdf.PdfBoxAndroidPdfTextExtractor
import net.dankito.text.extraction.pdf.iText2PdfTextExtractor
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import java.io.File
import javax.inject.Named
import javax.inject.Singleton


@Module
class BankingModule(private val applicationContext: Context) {

    companion object {

        const val DataFolderKey = "data.folder"

        const val DatabaseFolderKey = "database.folder"

        const val IndexFolderKey = "index.folder"

        // TODO: implement activity listener to always get latest activity
        lateinit var mainActivity: AppCompatActivity
    }


    @Provides
    @Singleton
    fun provideApplicationContext() : Context {
        return applicationContext
    }

    @Provides
    @Singleton
    @Named(DataFolderKey)
    fun provideDataFolder(applicationContext: Context) : File {
        return ensureFolderExists(applicationContext.filesDir, "data")
    }

    @Provides
    @Singleton
    @Named(DatabaseFolderKey)
    fun provideDatabaseFolder(@Named(DataFolderKey) dataFolder: File) : File {
        return ensureFolderExists(dataFolder, "db")
    }

    @Provides
    @Singleton
    @Named(IndexFolderKey)
    fun provideIndexFolder(@Named(DataFolderKey) dataFolder: File) : File {
        return ensureFolderExists(dataFolder, "index")
    }

    private fun ensureFolderExists(parentFolder: File, folderName: String): File {
        val folder = File(parentFolder, folderName)

        folder.mkdirs()

        return folder
    }


    @Provides
    @Singleton
    fun provideBankingPresenter(bankingClientCreator: IBankingClientCreator, bankFinder: IBankFinder,
                                @Named(DataFolderKey) dataFolder: File,
                                persister: IBankingPersistence, remitteeSearcher: IRemitteeSearcher, bankIconFinder: IBankIconFinder,
                                textExtractorRegistry: ITextExtractorRegistry, router: IRouter, invoiceDataExtractor: IInvoiceDataExtractor,
                                serializer: ISerializer, threadPool: IThreadPool) : BankingPresenter {
        return BankingPresenter(bankingClientCreator, bankFinder, dataFolder, persister,
            remitteeSearcher, bankIconFinder, textExtractorRegistry, router, invoiceDataExtractor, serializer, threadPool)
    }

    @Provides
    @Singleton
    fun provideBankFinder(@Named(IndexFolderKey) indexFolder: File) : IBankFinder {
        return LuceneBankFinder(indexFolder)
    }

    @Provides
    @Singleton
    fun provideBankIconFinder() : IBankIconFinder {
        return BankIconFinder()
    }

    @Provides
    @Singleton
    fun provideBankingClientCreator(webClient: IWebClient, base64Service: net.dankito.banking.util.IBase64Service) : IBankingClientCreator {
        return fints4kBankingClientCreator(webClient, base64Service)
    }

    @Provides
    @Singleton
    fun provideBankingPersistence(@Named(IndexFolderKey) indexFolder: File, @Named(DatabaseFolderKey) databaseFolder: File, serializer: ISerializer) : IBankingPersistence {
        return LuceneBankingPersistence(indexFolder, databaseFolder, serializer)
    }

    @Provides
    @Singleton
    fun provideRemitteeSearcher(@Named(IndexFolderKey) indexFolder: File) : IRemitteeSearcher {
        return LuceneRemitteeSearcher(indexFolder)
    }

    @Provides
    @Singleton
    fun provideCurrentActivityTracker() : CurrentActivityTracker {
        return CurrentActivityTracker()
    }

    @Provides
    @Singleton
    fun provideRouter(currentActivityTracker: CurrentActivityTracker) : IRouter {
        return RouterAndroid(currentActivityTracker)
    }


    @Provides
    @Singleton
    fun provideTextExtractorRegistry(applicationContext: Context) : ITextExtractorRegistry {
        // TODO: add PdfTypeDetector
        return TextExtractorRegistry(listOf(
            iText2PdfTextExtractor(), PdfBoxAndroidPdfTextExtractor(applicationContext)
        ))
    }

    @Provides
    @Singleton
    fun provideInvoiceDataExtractor() : IInvoiceDataExtractor {
        return InvoiceDataExtractor()
    }


    @Provides
    @Singleton
    fun provideWebClient() : IWebClient {
        return OkHttpWebClient()
    }

    @Provides
    @Singleton
    fun provideSerializer() : ISerializer {
        return JacksonJsonSerializer()
    }

    @Provides
    @Singleton
    fun provideBase64Service() : net.dankito.banking.util.IBase64Service {
        return Base64ServiceAndroid()
    }

    @Provides
    @Singleton
    fun provideThreadPool() : IThreadPool {
        return ThreadPool()
    }

}