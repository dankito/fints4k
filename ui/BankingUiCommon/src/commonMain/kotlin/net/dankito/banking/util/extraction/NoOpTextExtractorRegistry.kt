package net.dankito.banking.util.extraction

import net.dankito.utils.multiplatform.File


open class NoOpTextExtractorRegistry : ITextExtractorRegistry {

    override fun extractTextWithBestExtractorForFile(file: File): ExtractionResult {
        return ExtractionResult(false, null, null, true)
    }

}