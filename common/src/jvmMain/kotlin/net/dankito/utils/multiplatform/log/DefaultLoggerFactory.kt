package net.dankito.utils.multiplatform.log


actual class DefaultLoggerFactory {

    actual fun createDefaultLoggerFactory(): ILoggerFactory {
        if (isClassAvailable("org.slf4j.Logger")) {
            return Slf4jLoggerFactory()
        }

        return LogToConsoleLoggerFactory()
    }

    private fun isClassAvailable(qualifiedClassName: String): Boolean {
        try {
            Class.forName(qualifiedClassName)

            return true
        } catch (ignored: Exception) { }

        return false
    }

}