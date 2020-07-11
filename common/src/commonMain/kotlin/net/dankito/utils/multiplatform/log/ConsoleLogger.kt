package net.dankito.utils.multiplatform.log

import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter


open class ConsoleLogger(name: String) : LoggerBase(name) {

    companion object {
        private val DateFormatter = DateFormatter("HH:mm:ss.SSS")
    }


    override fun log(level: LogLevel, message: String) {
        println("${DateFormatter.format(Date())} $level $name - $message")
    }

}