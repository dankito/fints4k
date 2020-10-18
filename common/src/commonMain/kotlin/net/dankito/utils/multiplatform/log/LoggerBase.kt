package net.dankito.utils.multiplatform.log

import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatter
import net.dankito.utils.multiplatform.Thread


abstract class LoggerBase(
    override val name: String,
    open var level: LogLevel = LogLevel.Info
) : Logger {

    companion object {
        private val DateFormatter = DateFormatter("HH:mm:ss.SSS")
    }


    abstract fun log(level: LogLevel, message: String)


    override val isFatalEnabled get() = isEnabled(LogLevel.Fatal)

    override val isErrorEnabled get() = isEnabled(LogLevel.Error)

    override val isWarnEnabled get() = isEnabled(LogLevel.Warn)

    override val isInfoEnabled get() = isEnabled(LogLevel.Info)

    override val isDebugEnabled get() = isEnabled(LogLevel.Debug)

    override val isTraceEnabled get() = isEnabled(LogLevel.Trace)

    open fun isEnabled(level: LogLevel) = level.priority <= this.level.priority


    override fun fatal(message: String, exception: Throwable?, vararg arguments: Any) {
        logIfEnabled(LogLevel.Fatal, exception, { message }, *arguments)
    }

    override fun fatal(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Fatal, exception, message, *arguments)
    }


    override fun error(message: String, exception: Throwable?, vararg arguments: Any) {
        logIfEnabled(LogLevel.Error, exception, { message }, *arguments)
    }

    override fun error(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Error, exception, message, *arguments)
    }


    override fun warn(message: String, exception: Throwable?, vararg arguments: Any) {
        logIfEnabled(LogLevel.Warn, exception, { message }, *arguments)
    }

    override fun warn(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Warn, exception, message, *arguments)
    }


    override fun info(message: String, exception: Throwable?, vararg arguments: Any) {
        logIfEnabled(LogLevel.Info, exception, { message }, *arguments)
    }

    override fun info(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Info, exception, message, *arguments)
    }


    override fun debug(message: String, exception: Throwable?, vararg arguments: Any) {
        logIfEnabled(LogLevel.Debug, exception, { message }, *arguments)
    }

    override fun debug(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Debug, exception, message, *arguments)
    }


    override fun trace(message: String, exception: Throwable?, vararg arguments: Any) {
        logIfEnabled(LogLevel.Trace, exception, { message }, *arguments)
    }

    override fun trace(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Trace, exception, message, *arguments)
    }


    open fun logIfEnabled(level: LogLevel, exception: Throwable? = null, message: () -> String, vararg arguments: Any) {
        if (isEnabled(level)) {
            log(level, createMessage(exception, message(), *arguments))
        }
    }

    open fun createMessage(exception: Throwable?, message: String, vararg arguments: Any): String {
        return message // really, there's not String.format() ?! // TODO: add arguments and exception
    }


    protected open fun createLogOutput(level: LogLevel, message: String): String {
        return "${DateFormatter.format(Date())} [$level] ${Thread.current.threadName} $name - $message"
    }

}