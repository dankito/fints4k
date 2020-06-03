package net.dankito.banking.fints.util.log


interface Logger {

    val name: String


    val isFatalEnabled: Boolean

    val isErrorEnabled: Boolean

    val isWarnEnabled: Boolean

    val isInfoEnabled: Boolean

    val isDebugEnabled: Boolean

    val isTraceEnabled: Boolean


    fun fatal(exception: Throwable? = null, vararg arguments: Any, message: () -> String)

    fun error(exception: Throwable? = null, vararg arguments: Any, message: () -> String)

    fun warn(exception: Throwable? = null, vararg arguments: Any, message: () -> String)

    fun info(exception: Throwable? = null, vararg arguments: Any, message: () -> String)

    fun debug(exception: Throwable? = null, vararg arguments: Any, message: () -> String)

    fun trace(exception: Throwable? = null, vararg arguments: Any, message: () -> String)

}