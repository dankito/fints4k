package net.dankito.utils.multiplatform.log

import kotlin.reflect.KClass


class LoggerFactory {

    companion object {

        var loggerFactory: ILoggerFactory = createDefaultLoggerFactory()

        private fun createDefaultLoggerFactory(): ILoggerFactory {
            return DefaultLoggerFactory().createDefaultLoggerFactory()
        }


        fun getLogger(name: String): Logger {
            return loggerFactory.getLogger(name)
        }

        fun getLogger(kClass: KClass<*>): Logger {
            return getLogger(kClass.simpleName ?: kClass.toString()) // TODO: use qualifiedName on JVM
        }

    }

}