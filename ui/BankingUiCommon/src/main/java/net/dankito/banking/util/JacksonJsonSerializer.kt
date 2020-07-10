package net.dankito.banking.util

import net.dankito.utils.serialization.JacksonJsonSerializer
import java.io.File


open class JacksonJsonSerializer(
    protected val serializer: net.dankito.utils.serialization.ISerializer = JacksonJsonSerializer()
) : ISerializer {

    override fun serializeObject(obj: Any, outputFile: File) {
        return serializer.serializeObject(obj, outputFile)
    }

    override fun <T> deserializeObject(serializedObjectFile: File, objectClass: Class<T>,
                                       vararg genericParameterTypes: Class<*>): T? {
        return serializer.deserializeObject(serializedObjectFile, objectClass, *genericParameterTypes)
    }

    override fun <T> deserializeListOr(serializedObjectFile: File, genericListParameterType: Class<T>, defaultValue: List<T>): List<T> {
        return serializer.deserializeListOr(serializedObjectFile, genericListParameterType, defaultValue)
    }

}