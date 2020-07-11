package net.dankito.utils.multiplatform

import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.lastPathComponent


actual class File actual constructor(path: String) : NSURL(fileURLWithPath = path) {

    actual constructor(folder: File, filename: String)
            : this(NSURL(string = filename, relativeToURL = folder).absoluteString ?: "") // TODO: or use 'fileURLWithPath'?


    actual fun getAbsolutePath(): String {
        return absoluteString ?: absoluteURL?.absoluteString ?: path ?: ""
    }

    actual val filename: String
        get() = lastPathComponent ?: ""

    actual val fileExtension: String
        get() = filename.substringAfterLast('.', "")


    actual fun mkdirs(): Boolean {
        return NSFileManager.defaultManager.createDirectoryAtURL(this, true, null, null)
    }

}