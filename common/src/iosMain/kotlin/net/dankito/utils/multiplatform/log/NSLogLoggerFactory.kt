package net.dankito.utils.multiplatform.log


open class NSLogLoggerFactory : ILoggerFactory {

    override fun getLogger(name: String): Logger {
        return NSLogLogger(name)
    }

}