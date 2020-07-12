package net.dankito.utils.multiplatform.log


actual class DefaultLoggerFactory {

    actual fun createDefaultLoggerFactory(): ILoggerFactory {
        // i cannot recommend using NSLog from Kotlin, has many bugs (e.g. cutting messages, printing non Ansi characters in Ansi messages, ...)
//        return NSLogLoggerFactory()
        return LogToConsoleLoggerFactory()
    }

}