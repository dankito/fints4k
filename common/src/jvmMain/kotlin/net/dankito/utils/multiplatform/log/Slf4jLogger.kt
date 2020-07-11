package net.dankito.utils.multiplatform.log


open class Slf4jLogger(protected val slf4jLogger: org.slf4j.Logger) : Logger {

    override val name: String
        get() = slf4jLogger.name


    override val isFatalEnabled: Boolean
        get() = isErrorEnabled

    override val isErrorEnabled: Boolean
        get() = slf4jLogger.isErrorEnabled

    override val isWarnEnabled: Boolean
        get() = slf4jLogger.isWarnEnabled

    override val isInfoEnabled: Boolean
        get() = slf4jLogger.isInfoEnabled

    override val isDebugEnabled: Boolean
        get() = slf4jLogger.isDebugEnabled

    override val isTraceEnabled: Boolean
        get() = slf4jLogger.isTraceEnabled


    override fun fatal(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        error(exception, *arguments, message = message)
    }

    override fun error(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        log(exception, arguments, message, { msg, args -> slf4jLogger.error(msg, *args) } )
    }

    override fun warn(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        log(exception, arguments, message, { msg, args -> slf4jLogger.warn(msg, *args) } )
    }

    override fun info(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        log(exception, arguments, message, { msg, args -> slf4jLogger.info(msg, *args) } )
    }

    override fun debug(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        log(exception, arguments, message, { msg, args -> slf4jLogger.debug(msg, *args) } )
    }

    override fun trace(exception: Throwable?, vararg arguments: Any, message: () -> String) {
        log(exception, arguments, message, { msg, args -> slf4jLogger.trace(msg, *args) } )
    }

    protected open fun log(exception: Throwable?, arguments: Array<out Any>, messageCreator: () -> String,
                 logOnLevel: (String, Array<out Any>) -> Unit) {

        val message = messageCreator()

        if (exception != null) {
            if (arguments.isEmpty()) {
                logOnLevel(message, arrayOf(exception))
            }
            else {
                val argumentsIncludingException: MutableList<Any> = mutableListOf(exception)
                argumentsIncludingException.addAll(0, arguments.toList())

                logOnLevel(message, argumentsIncludingException.toTypedArray())
            }
        }
        else {
            logOnLevel(message, arguments)
        }
    }

}