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

}