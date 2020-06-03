package net.dankito.banking.fints.util.log


abstract class CachedLoggerFactory : ILoggerFactory {

    abstract fun createLogger(name: String): Logger


    protected open val loggerCache = mutableMapOf<String, Logger>() // TODO: make thread safe like with ConcurrentHashMap


    override fun getLogger(name: String): Logger {
        loggerCache[name]?.let {
            return it
        }

        val logger = createLogger(name)

        loggerCache[name] = logger

        return logger
    }

}