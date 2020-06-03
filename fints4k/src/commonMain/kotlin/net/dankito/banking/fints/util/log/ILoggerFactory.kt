package net.dankito.banking.fints.util.log


interface ILoggerFactory {

    fun getLogger(name: String): Logger

}