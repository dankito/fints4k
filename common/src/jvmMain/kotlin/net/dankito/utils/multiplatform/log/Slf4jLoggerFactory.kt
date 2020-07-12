package net.dankito.utils.multiplatform.log

import org.slf4j.LoggerFactory


open class Slf4jLoggerFactory : ILoggerFactory {

    override fun getLogger(name: String): Logger {
        return Slf4jLogger(LoggerFactory.getLogger(name))
    }

}