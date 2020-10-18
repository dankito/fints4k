package net.dankito.banking.util.extraction

import net.dankito.utils.multiplatform.File


interface ITextExtractorRegistry {

    fun extractTextWithBestExtractorForFile(file: File): ExtractionResult

}