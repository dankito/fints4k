package net.dankito.utils.multiplatform.log


open class LogToConsoleLoggerFactory : CachedLoggerFactory() {

    override fun createLogger(name: String): Logger {
        return ConsoleLogger(name)
    }

}