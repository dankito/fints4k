package net.dankito.banking.util

import net.dankito.utils.multiplatform.File
import kotlin.reflect.KClass


open class NoOpSerializer : ISerializer {

    override fun serializeObject(obj: Any, outputFile: File) {

    }

    override fun <T : Any> deserializeObject(serializedObjectFile: File, objectClass: KClass<T>, vararg genericParameterTypes: KClass<*>): T? {
        return null
    }

    override fun <T : Any> deserializeListOr(serializedObjectFile: File, genericListParameterType: KClass<T>, defaultValue: List<T>): List<T> {
        return defaultValue
    }

}