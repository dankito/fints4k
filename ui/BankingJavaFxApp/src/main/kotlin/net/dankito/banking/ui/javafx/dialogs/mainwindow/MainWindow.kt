package net.dankito.banking.ui.javafx.dialogs.mainwindow

import javafx.scene.control.SplitPane
import net.dankito.banking.fints4javaBankingClientCreator
import net.dankito.banking.persistence.BankingPersistenceJson
import net.dankito.banking.ui.javafx.RouterJavaFx
import net.dankito.banking.ui.javafx.controls.AccountTransactionsView
import net.dankito.banking.ui.javafx.controls.AccountsView
import net.dankito.banking.ui.javafx.dialogs.mainwindow.controls.MainMenuBar
import net.dankito.banking.ui.javafx.util.Base64ServiceJava8
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.BankIconFinder
import net.dankito.fints.banks.LuceneBankFinder
import net.dankito.utils.web.client.OkHttpWebClient
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.io.File


class MainWindow : View(messages["application.title"]) {

    private val dataFolder = File("data")

    private val databaseFolder = File(dataFolder, "db")

    private val indexFolder = File(dataFolder, "index")

    private val presenter = BankingPresenter(fints4javaBankingClientCreator(OkHttpWebClient(), Base64ServiceJava8()),
        LuceneBankFinder(indexFolder), databaseFolder, dataFolder, BankingPersistenceJson(File(databaseFolder, "accounts.json")), BankIconFinder(), RouterJavaFx())
//    private val presenter = BankingPresenter(hbci4jBankingClientCreator(), LuceneBankFinder(indexFolder), databaseFolder,
//    dataFolder, BankingPersistenceJson(File(databaseFolder, "accounts.json")), BankIconFinder(), RouterJavaFx())



    override val root = borderpane {
        prefHeight = 620.0
        prefWidth = 1150.0

        top = MainMenuBar(presenter).root

        center {
            splitpane {
                add(AccountsView(presenter).apply {
                    SplitPane.setResizableWithParent(this.root, false)
                })

                add(AccountTransactionsView(presenter))

                setDividerPosition(0, 0.2)
            }
        }
    }

}