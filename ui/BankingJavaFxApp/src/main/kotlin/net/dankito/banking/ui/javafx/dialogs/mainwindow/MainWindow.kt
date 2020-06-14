package net.dankito.banking.ui.javafx.dialogs.mainwindow

import javafx.scene.control.SplitPane
import net.dankito.banking.fints4kBankingClientCreator
import net.dankito.banking.ui.javafx.RouterJavaFx
import net.dankito.banking.ui.javafx.controls.AccountTransactionsView
import net.dankito.banking.ui.javafx.controls.AccountsView
import net.dankito.banking.ui.javafx.dialogs.mainwindow.controls.MainMenuBar
import net.dankito.banking.ui.javafx.util.Base64ServiceJava8
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.banking.util.BankIconFinder
import net.dankito.banking.bankfinder.LuceneBankFinder
import net.dankito.banking.persistence.LuceneBankingPersistence
import net.dankito.banking.search.LuceneRemitteeSearcher
import net.dankito.text.extraction.TextExtractorRegistry
import net.dankito.text.extraction.TikaTextExtractor
import net.dankito.text.extraction.image.Tesseract4CommandlineImageTextExtractor
import net.dankito.text.extraction.image.model.OcrLanguage
import net.dankito.text.extraction.image.model.TesseractConfig
import net.dankito.text.extraction.pdf.*
import net.dankito.utils.web.client.OkHttpWebClient
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.io.File


class MainWindow : View(messages["application.title"]) {

    private val dataFolder = ensureFolderExists(File("."), "data")

    private val databaseFolder = ensureFolderExists(dataFolder, "db")

    private val indexFolder = ensureFolderExists(dataFolder, "index")

    private val tesseractTextExtractor = Tesseract4CommandlineImageTextExtractor(TesseractConfig(listOf(OcrLanguage.English, OcrLanguage.German)))

    private val textExtractorRegistry = TextExtractorRegistry(pdffontsPdfTypeDetector(), listOf(
        pdfToTextPdfTextExtractor(), PdfBoxPdfTextExtractor(), iText2PdfTextExtractor(),
        ImageOnlyPdfTextExtractor(tesseractTextExtractor, pdfimagesImagesFromPdfExtractor()),
        tesseractTextExtractor, TikaTextExtractor()
    ))

    private val presenter = BankingPresenter(fints4kBankingClientCreator(),
        LuceneBankFinder(indexFolder), dataFolder, LuceneBankingPersistence(indexFolder, databaseFolder),
        LuceneRemitteeSearcher(indexFolder), BankIconFinder(), textExtractorRegistry, RouterJavaFx())
//    private val presenter = BankingPresenter(hbci4jBankingClientCreator(), LuceneBankFinder(indexFolder),
//    dataFolder, LuceneBankingPersistence(indexFolder, databaseFolder), LuceneRemitteeSearcher(indexFolder),
//    BankIconFinder(), textExtractorRegistry, RouterJavaFx())



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


    private fun ensureFolderExists(parentFolder: File, folderName: String): File {
        val folder = File(parentFolder, folderName)

        folder.mkdirs()

        return folder
    }

}