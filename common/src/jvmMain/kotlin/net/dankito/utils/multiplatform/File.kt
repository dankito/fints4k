package net.dankito.utils.multiplatform

import java.io.File


fun File.toFile(): net.dankito.utils.multiplatform.File {
    return net.dankito.utils.multiplatform.File(this.absolutePath)
}


actual class File actual constructor(path: String) : File(path) {

    actual constructor(folder: net.dankito.utils.multiplatform.File, filename: String)
            : this(File(folder, filename).absolutePath)


    internal constructor() : this("") // for object deserializers


    actual override fun getAbsolutePath(): String {
        return super.getAbsolutePath()
    }

    actual val filename: String
        get() = super.getName()

    actual val fileExtension: String
        get() = this.extension


    actual override fun mkdirs(): Boolean {
        return super.mkdirs()
    }

}