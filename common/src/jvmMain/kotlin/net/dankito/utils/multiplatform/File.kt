package net.dankito.utils.multiplatform

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import net.dankito.utils.multiplatform.serialization.FileDeserializer


fun java.io.File.toFile(): File {
    return File(this.absolutePath)
}


@JsonDeserialize(using = FileDeserializer::class)
actual class File actual constructor(path: String) : java.io.File(path) {

    actual constructor(folder: File, filename: String)
            : this(java.io.File(folder, filename).absolutePath)


    internal constructor() : this("") // for object deserializers


    actual override fun getAbsolutePath(): String {
        return super.getAbsolutePath()
    }

    actual val filename: String
        get() = super.getName()

    actual val fileExtension: String
        get() = this.extension

    actual val parent: File?
        get() = this.parentFile?.absolutePath?.let { File(it) }


    actual override fun mkdirs(): Boolean {
        return super.mkdirs()
    }

}