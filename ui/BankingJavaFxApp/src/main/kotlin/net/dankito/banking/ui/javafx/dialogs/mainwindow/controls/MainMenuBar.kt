package net.dankito.banking.ui.javafx.dialogs.mainwindow.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import net.codinux.banking.tools.importerexporter.CsvAccountTransactionsExporter
import net.codinux.banking.tools.importerexporter.model.AccountTransaction
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.utils.multiplatform.toFile
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResult
import net.dankito.banking.ui.model.moneytransfer.ExtractTransferMoneyDataFromPdfResultType
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.javafx.ui.dialogs.JavaFXDialogService
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import tornadofx.*
import java.io.File
import java.text.SimpleDateFormat


open class MainMenuBar(protected val presenter: BankingPresenter) : View() {

    companion object {
        val ExportTransactionsDateFormat = SimpleDateFormat("yyyyMMdd")
    }


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

                menu(messages["main.window.menu.file.export"]) {
                    item(messages["main.window.menu.file.export.csv"]) {
                        action { exportAccountTransactions() }
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


    protected open fun exportAccountTransactions() {
        val fileChooser = FileChooser()

        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("CSV files", "*.csv"),
            FileChooser.ExtensionFilter("All files", "*.*")
        )

        fileChooser.initialDirectory = presenter.appSettings.lastSelectedExportFolder?.let { net.dankito.utils.multiplatform.File(it) }
        fileChooser.initialFileName = getExportCsvSuggestedFilename()

        fileChooser.showSaveDialog(currentWindow)?.let { selectedFile ->
            presenter.appSettings.lastSelectedExportFolder = selectedFile.parent
            presenter.appSettingsChanged()

            var destinationFile = selectedFile
            if (destinationFile.extension.isNullOrBlank()) {
                destinationFile = File(destinationFile.absolutePath + ".csv")
            }

            val transactions = presenter.allTransactions.map { mapTransaction(it) }

            CsvAccountTransactionsExporter().export(destinationFile, transactions)
        }
    }

    // TODO: this is almost the same code as in JAndroid SettingsDialog.getExportCsvSuggestedFilename() -> merge
    protected open fun getExportCsvSuggestedFilename(): String? {
        val transactions = presenter.allTransactions
        val transactionsDates = transactions.map { it.valueDate }
        val transactionsStartDate = transactionsDates.min()
        val transactionsEndDate = transactionsDates.max()

        return String.format(messages["main.window.menu.file.export.csv.suggested.filename"], transactionsStartDate?.let { ExportTransactionsDateFormat.format(it) } ?: "",
            transactionsEndDate?.let { ExportTransactionsDateFormat.format(it) } ?: "")
    }

    // TODO: this is exactly the same code as in Android SettingsDialog.mapTransaction() -> merge
    protected open fun mapTransaction(transaction: IAccountTransaction): AccountTransaction {
        return AccountTransaction(
            transaction.account.iban ?: transaction.account.identifier,
            transaction.amount,
            transaction.currency,
            transaction.reference,
            transaction.bookingDate,
            transaction.valueDate,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText
        )
    }

}