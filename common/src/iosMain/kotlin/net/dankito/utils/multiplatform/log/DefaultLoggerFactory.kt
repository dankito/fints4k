package net.dankito.utils.multiplatform.log


actual class DefaultLoggerFactory {

    actual fun createDefaultLoggerFactory(): ILoggerFactory {
        return NSLogLoggerFactory()
    }

}