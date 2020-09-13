package net.dankito.utils.multiplatform


expect class File(path: String) {

    constructor(folder: File, filename: String)


    // have to specify it as method as property would conflict with java.io.File's getAbsolutePath
    fun getAbsolutePath(): String

    val filename: String

    val fileExtension: String

    val parent: File?


    fun mkdirs(): Boolean

    fun delete(): Boolean

}