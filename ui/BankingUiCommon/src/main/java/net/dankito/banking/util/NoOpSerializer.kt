package net.dankito.banking.util

import java.io.File


open class NoOpSerializer : ISerializer {

    override fun serializeObject(obj: Any, outputFile: File) {

    }

    override fun <T> deserializeObject(serializedObjectFile: File, objectClass: Class<T>, vararg genericParameterTypes: Class<*>): T? {
        return null
    }

}