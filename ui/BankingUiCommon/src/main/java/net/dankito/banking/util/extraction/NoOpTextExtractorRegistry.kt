package net.dankito.banking.util.extraction

import java.io.File


open class NoOpTextExtractorRegistry : ITextExtractorRegistry {

    override fun extractTextWithBestExtractorForFile(file: File): ExtractionResult {
        return ExtractionResult(false, null, null, true)
    }

}