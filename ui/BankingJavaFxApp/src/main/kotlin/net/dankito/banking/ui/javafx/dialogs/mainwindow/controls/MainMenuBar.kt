package net.dankito.banking.ui.javafx.dialogs.mainwindow.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.dankito.utils.multiplatform.toFile
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResult
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResultType
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.javafx.ui.dialogs.JavaFXDialogService
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import tornadofx.*
import java.io.File


open class MainMenuBar(protected val presenter: BankingPresenter) : View() {

    protected val areAccountsThatCanTransferMoneyAdded = SimpleBooleanProperty()

    protected var lastSelectedFolder: File? = null


    init {
        presenter.addBanksChangedListener {
            runLater {
                checkIfThereAreAccountsThatCanTransferMoney()
            }
        }

        checkIfThereAreAccountsThatCanTransferMoney()
    }


    override val root =
        menubar {
            fixedHeight = 30.0

            menu(messages["main.window.menu.file"]) {
                menu(messages["main.window.menu.file.new"]) {
                    item(messages["main.window.menu.file.new.account"], KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN)) {
                        action { presenter.showAddAccountDialog() }
                    }

                    separator()

                    item(messages["main.window.menu.file.new.cash.transfer"], KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN)) {
                        enableWhen(areAccountsThatCanTransferMoneyAdded)

                        action { presenter.showTransferMoneyDialog() }
                    }

                    item(messages["main.window.menu.file.new.cash.transfer.from.pdf"], KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN, KeyCodeCombination.SHIFT_DOWN)) {
                        enableWhen(areAccountsThatCanTransferMoneyAdded)

                        action { showTransferMoneyDialogWithDataFromPdf() }
                    }
                }

                separator()

                item(messages["main.window.menu.file.quit"], KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)) {
                    action { primaryStage.close() }
                }
            }
        }


    protected open fun checkIfThereAreAccountsThatCanTransferMoney() {
        areAccountsThatCanTransferMoneyAdded.value = presenter.hasAccountsSupportTransferringMoney
    }

    protected open fun showTransferMoneyDialogWithDataFromPdf() {
        val fileChooser = FileChooser()

        fileChooser.initialDirectory = lastSelectedFolder
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("PDFs (*.pdf)", "*.pdf"))

        fileChooser.showOpenDialog(currentStage)?.let { pdfFile ->
            lastSelectedFolder = pdfFile.parentFile

            val result = presenter.showTransferMoneyDialogWithDataFromPdf(pdfFile.toFile())

            if (result.type != ExtractTransferMoneyDataFromPdfResultType.Success) {
                showTransferMoneyDialogWithDataFromPdfError(pdfFile, result)
            }
        }
    }

    protected open fun showTransferMoneyDialogWithDataFromPdfError(pdfFile: File, result: ExtractTransferMoneyDataFromPdfResult) {
        val errorMessageKey = when (result.type) {
            ExtractTransferMoneyDataFromPdfResultType.NotASearchablePdf -> "transfer.money.from.pdf.error.message.not.a.searchable.pdf"
            ExtractTransferMoneyDataFromPdfResultType.CouldNotExtractText -> "transfer.money.from.pdf.error.message.could.not.extract.text"
            ExtractTransferMoneyDataFromPdfResultType.CouldNotExtractInvoiceDataFromExtractedText -> "transfer.money.from.pdf.error.message.could.not.extract.invoice.data"
            ExtractTransferMoneyDataFromPdfResultType.Success -> "" // will never come to this
        }

        val errorMessage = String.format(messages[errorMessageKey], pdfFile.absolutePath)

        JavaFXDialogService().showErrorMessage(errorMessage, exception = result.error)
    }

}