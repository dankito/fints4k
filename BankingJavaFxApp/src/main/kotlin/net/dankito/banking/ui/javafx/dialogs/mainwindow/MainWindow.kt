package net.dankito.banking.javafx.dialogs.mainwindow

import javafx.scene.control.SplitPane
import net.dankito.banking.hbci4jBankingClientCreator
import net.dankito.banking.persistence.BankingPersistenceJson
import net.dankito.banking.ui.javafx.RouterJavaFx
import net.dankito.banking.ui.javafx.controls.AccountTransactionsView
import net.dankito.banking.ui.javafx.controls.AccountsView
import net.dankito.banking.ui.javafx.dialogs.mainwindow.controls.MainMenuBar
import net.dankito.banking.ui.presenter.BankingPresenter
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.io.File


class MainWindow : View(messages["application.title"]) {

    private val dataFolder = File("data", "accounts")

//    private val presenter = MainWindowPresenter(fints4javaBankingClientCreator(OkHttpWebClient(), Base64ServiceJava8()), dataFolder, BankingPersistenceJson(File(dataFolder, "accounts.json")), RouterJavaFx())
    private val presenter = BankingPresenter(hbci4jBankingClientCreator(), dataFolder, BankingPersistenceJson(File(dataFolder, "accounts.json")), RouterJavaFx())



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