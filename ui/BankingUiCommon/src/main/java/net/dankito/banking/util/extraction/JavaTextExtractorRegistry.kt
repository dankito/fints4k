package net.dankito.banking.util.extraction

import net.dankito.text.extraction.TextExtractorRegistry
import net.dankito.text.extraction.model.ErrorType
import java.io.File


open class JavaTextExtractorRegistry(
    protected val textExtractorRegistry: net.dankito.text.extraction.ITextExtractorRegistry = TextExtractorRegistry()
) : ITextExtractorRegistry {

    override fun extractTextWithBestExtractorForFile(file: File): ExtractionResult {
        val result = textExtractorRegistry.extractTextWithBestExtractorForFile(file)

        return ExtractionResult(
            result.couldExtractText,
            result.text,
            result.error?.exception,
            result.error?.type == ErrorType.NoExtractorFoundForFileType
        )
    }

}