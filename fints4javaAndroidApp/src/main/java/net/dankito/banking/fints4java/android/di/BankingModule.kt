package net.dankito.banking.fints4java.android.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import net.dankito.banking.fints4java.android.RouterAndroid
import net.dankito.banking.fints4java.android.util.Base64ServiceAndroid
import net.dankito.banking.fints4javaBankingClientCreator
import net.dankito.banking.persistence.BankingPersistenceJson
import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.banking.ui.IBankingClientCreator
import net.dankito.banking.ui.IRouter
import net.dankito.banking.ui.presenter.BankingPresenter
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
class BankingModule(internal val mainActivity: AppCompatActivity) {

    companion object {

        const val DataFolderKey = "data.folder"

    }


    private val applicationContext = mainActivity.applicationContext


    @Provides
    @Singleton
    fun provideApplicationContext() : Context {
        return applicationContext
    }

    @Provides
    @Singleton
    @Named(DataFolderKey)
    fun provideDataFolder(applicationContext: Context) : File {
        return File(applicationContext.filesDir, "data/accounts")
    }


    @Provides
    @Singleton
    fun provideBankingPresenter(bankingClientCreator: IBankingClientCreator, @Named(DataFolderKey) dataFolder: File,
                                persister: IBankingPersistence, router: IRouter, threadPool: IThreadPool) : BankingPresenter {
        return BankingPresenter(bankingClientCreator, dataFolder, persister, router, threadPool)
    }

    @Provides
    @Singleton
    fun provideBankingClientCreator(webClient: IWebClient, base64Service: net.dankito.banking.util.IBase64Service) : IBankingClientCreator {
        return fints4javaBankingClientCreator(webClient, base64Service)
    }

    @Provides
    @Singleton
    fun provideBankingPersistence(@Named(DataFolderKey) dataFolder: File, serializer: ISerializer) : IBankingPersistence {
        return BankingPersistenceJson(File(dataFolder, "accounts.json"), serializer)
    }

    @Provides
    @Singleton
    fun provideRouter() : IRouter {
        return RouterAndroid(mainActivity)
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