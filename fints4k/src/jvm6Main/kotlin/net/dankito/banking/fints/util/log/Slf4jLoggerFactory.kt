package net.dankito.banking.fints.util.log

import org.slf4j.LoggerFactory


open class Slf4jLoggerFactory : CachedLoggerFactory() {

    override fun createLogger(name: String): Logger {
        return Slf4jLogger(LoggerFactory.getLogger(name))
    }

}