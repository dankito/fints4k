package net.dankito.banking.util.extraction

import java.io.File


interface ITextExtractorRegistry {

    fun extractTextWithBestExtractorForFile(file: File): ExtractionResult

}