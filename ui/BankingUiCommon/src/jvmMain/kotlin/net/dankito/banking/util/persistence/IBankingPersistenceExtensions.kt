package net.dankito.banking.util.persistence

import net.dankito.banking.persistence.IBankingPersistence
import net.dankito.utils.multiplatform.File
import java.io.FileOutputStream
import java.net.URL


fun IBankingPersistence.doSaveUrlToFile(url: String, file: File) {
    URL(url).openConnection().getInputStream().buffered().use { iconInputStream ->
        FileOutputStream(file).use { fileOutputStream ->
            iconInputStream.copyTo(fileOutputStream)
        }
    }
}

fun IBankingPersistence.downloadIcon(url: String): ByteArray {
    URL(url).openConnection().getInputStream().buffered().use { iconInputStream ->
        return iconInputStream.readBytes()
    }
}