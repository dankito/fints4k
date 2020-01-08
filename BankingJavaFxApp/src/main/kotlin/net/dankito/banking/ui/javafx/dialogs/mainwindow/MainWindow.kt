package net.dankito.banking.javafx.dialogs.mainwindow

import net.dankito.banking.fints4javaBankingClientCreator
import net.dankito.banking.ui.javafx.RouterJavaFx
import net.dankito.banking.ui.javafx.dialogs.mainwindow.controls.MainMenuBar
import net.dankito.banking.ui.javafx.util.Base64ServiceJava8
import net.dankito.banking.ui.presenter.MainWindowPresenter
import tornadofx.FX.Companion.messages
import tornadofx.View
import tornadofx.borderpane
import tornadofx.get


class MainWindow : View(messages["main.window.title"]) {

    private val presenter = MainWindowPresenter(fints4javaBankingClientCreator(), Base64ServiceJava8(), RouterJavaFx())



    override val root = borderpane {
        prefHeight = 620.0
        prefWidth = 1150.0

        top = MainMenuBar().root
    }

}