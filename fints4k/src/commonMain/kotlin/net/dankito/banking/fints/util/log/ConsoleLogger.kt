package net.dankito.banking.fints.util.log

import com.soywiz.klock.DateTime


open class ConsoleLogger(name: String) : LoggerBase(name) {

    companion object {
        private val DateFormat = com.soywiz.klock.DateFormat.invoke("HH:mm:ss.SSS")
    }


    override fun log(level: LogLevel, message: String) {
        println("${DateTime.nowLocal().toString(DateFormat)} $level $name - $message")
    }

}