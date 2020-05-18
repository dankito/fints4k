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
import net.dankito.banking.fints.banks.IBankFinder
import net.dankito.banking.fints.banks.LuceneBankFinder
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
        return File(applicationContext.filesDir, "data")
    }

    @Provides
    @Singleton
    @Named(DatabaseFolderKey)
    fun provideDatabaseFolder(@Named(DataFolderKey) dataFolder: File) : File {
        return File(dataFolder, "db")
    }

    @Provides
    @Singleton
    @Named(IndexFolderKey)
    fun provideIndexFolder(@Named(DataFolderKey) dataFolder: File) : File {
        return File(dataFolder, "index")
    }


    @Provides
    @Singleton
    fun provideBankingPresenter(bankingClientCreator: IBankingClientCreator, bankFinder: IBankFinder,
                                @Named(DatabaseFolderKey) databaseFolder: File, @Named(DataFolderKey) dataFolder: File,
                                persister: IBankingPersistence, bankIconFinder: IBankIconFinder,
                                router: IRouter, threadPool: IThreadPool) : BankingPresenter {
        return BankingPresenter(bankingClientCreator, bankFinder, databaseFolder, dataFolder, persister, bankIconFinder, router, threadPool)
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