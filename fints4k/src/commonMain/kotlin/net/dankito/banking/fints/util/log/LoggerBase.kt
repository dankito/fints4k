package net.dankito.banking.fints.util.log


abstract class LoggerBase(
    override val name: String,
    open var level: LogLevel = LogLevel.Info
) : Logger {

    abstract fun log(level: LogLevel, message: String)


    override val isFatalEnabled get() = isEnabled(LogLevel.Fatal)

    override val isErrorEnabled get() = isEnabled(LogLevel.Error)

    override val isWarnEnabled get() = isEnabled(LogLevel.Warn)

    override val isInfoEnabled get() = isEnabled(LogLevel.Info)

    override val isDebugEnabled get() = isEnabled(LogLevel.Debug)

    override val isTraceEnabled get() = isEnabled(LogLevel.Trace)

    open fun isEnabled(level: LogLevel) = level.priority <= this.level.priority


    override fun fatal(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Fatal, exception, message, *arguments)
    }

    override fun error(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Error, exception, message, *arguments)
    }

    override fun warn(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Warn, exception, message, *arguments)
    }

    override fun info(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Info, exception, message, *arguments)
    }

    override fun debug(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        logIfEnabled(LogLevel.Debug, exception, message, *arguments)
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

}