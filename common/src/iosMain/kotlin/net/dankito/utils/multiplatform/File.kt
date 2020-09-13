package net.dankito.utils.multiplatform

import platform.Foundation.*


actual class File actual constructor(path: String) : NSURL(fileURLWithPath = path) {

    actual constructor(folder: File, filename: String)
            : this(folder.URLByAppendingPathComponent(filename)?.path ?: "")


    actual fun getAbsolutePath(): String {
        return absoluteString ?: absoluteURL?.absoluteString ?: path ?: ""
    }

    actual val filename: String
        get() = lastPathComponent ?: ""

    actual val fileExtension: String
        get() = this.pathExtension ?: filename.substringAfterLast('.', "")

    actual val parent: File?
        get() = this.URLByDeletingLastPathComponent?.absoluteString?.let { File(it) }


    actual fun mkdirs(): Boolean {
        return NSFileManager.defaultManager.createDirectoryAtURL(this, true, null, null)
    }

    actual fun delete(): Boolean {
        return NSFileManager.defaultManager.removeItemAtURL(this, null)
    }


    override fun description(): String? {
        return getAbsolutePath()
    }

}