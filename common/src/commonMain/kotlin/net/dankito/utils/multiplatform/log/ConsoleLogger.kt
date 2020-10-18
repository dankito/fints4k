package net.dankito.utils.multiplatform.log


open class ConsoleLogger(name: String) : LoggerBase(name) {

    override fun log(level: LogLevel, message: String) {
        println(createLogOutput(level, message))
    }

}