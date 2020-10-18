package net.dankito.utils.multiplatform.log


open class LogToConsoleLoggerFactory : ILoggerFactory {

    override fun getLogger(name: String): Logger {
        return ConsoleLogger(name)
    }

}