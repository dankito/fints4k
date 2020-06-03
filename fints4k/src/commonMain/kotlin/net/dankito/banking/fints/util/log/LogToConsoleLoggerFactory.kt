package net.dankito.banking.fints.util.log


open class LogToConsoleLoggerFactory : CachedLoggerFactory() {

    override fun createLogger(name: String): Logger {
        return ConsoleLogger(name)
    }

}