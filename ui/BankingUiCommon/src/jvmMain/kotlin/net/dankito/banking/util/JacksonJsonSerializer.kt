package net.dankito.banking.util

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import net.dankito.banking.util.persistence.JacksonClassNameIdResolver
import net.dankito.utils.multiplatform.File
import kotlin.reflect.KClass


open class JacksonJsonSerializer(
    protected val serializer: net.dankito.utils.serialization.ISerializer = net.dankito.utils.serialization.JacksonJsonSerializer { objectMapper ->
        val typeResolver = ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE)
        typeResolver.init(JsonTypeInfo.Id.CLASS, JacksonClassNameIdResolver())
        typeResolver.inclusion(JsonTypeInfo.As.PROPERTY)
        typeResolver.typeProperty("@CLASS")
        objectMapper.setDefaultTyping(typeResolver)
    }
) : ISerializer {

    override fun serializeObject(obj: Any, outputFile: File) {
        return serializer.serializeObject(obj, outputFile)
    }

    override fun <T : Any> deserializeObject(serializedObjectFile: File, objectClass: KClass<T>,
                                             vararg genericParameterTypes: KClass<*>): T? {
        return serializer.deserializeObject(serializedObjectFile, objectClass.java, *genericParameterTypes.map { it.java }.toTypedArray())
    }

    override fun <T : Any> deserializeListOr(serializedObjectFile: File, genericListParameterType: KClass<T>, defaultValue: List<T>): List<T> {
        return serializer.deserializeListOr(serializedObjectFile, genericListParameterType.java, defaultValue)
    }

}