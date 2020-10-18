package net.dankito.utils.multiplatform.log


interface ILoggerFactory {

    fun getLogger(name: String): Logger

}