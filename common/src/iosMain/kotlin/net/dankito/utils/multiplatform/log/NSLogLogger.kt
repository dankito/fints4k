package net.dankito.utils.multiplatform.log

import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter
import platform.Foundation.NSLog


open class NSLogLogger(name: String) : LoggerBase(name) {

    companion object {
        private val DateFormatter = DateFormatter("HH:mm:ss.SSS")
    }


    override fun log(level: LogLevel, message: String) {
        NSLog("${DateFormatter.format(Date())} $level $name - $message")
    }

}