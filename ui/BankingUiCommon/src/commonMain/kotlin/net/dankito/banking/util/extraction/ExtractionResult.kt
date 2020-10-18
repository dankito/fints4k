package net.dankito.banking.util.extraction


open class ExtractionResult(
    open val couldExtractText: Boolean,
    open val text: String?,
    open val exception: Exception? = null,
    open val noExtractorFound: Boolean = false
) {

}