package net.dankito.utils.multiplatform.log

import platform.Foundation.NSLog


open class NSLogLogger(name: String) : LoggerBase(name) {

    override fun log(level: LogLevel, message: String) {
        NSLog(createLogOutput(level, message))
    }

}