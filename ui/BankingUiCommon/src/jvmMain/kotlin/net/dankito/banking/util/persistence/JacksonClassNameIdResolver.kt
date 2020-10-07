package net.dankito.banking.util.persistence

import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver
import com.fasterxml.jackson.databind.type.SimpleType
import com.fasterxml.jackson.databind.type.TypeFactory
import net.dankito.banking.fints.response.segments.JobParameters
import net.dankito.banking.fints.response.segments.RetrieveAccountTransactionsParameters
import net.dankito.banking.fints.response.segments.SepaAccountInfoParameters
import kotlin.reflect.jvm.jvmName


open class JacksonClassNameIdResolver : ClassNameIdResolver(SimpleType.construct(JobParameters::class.java), TypeFactory.defaultInstance()) {

    override fun idFromValue(value: Any?): String {
        println("value class is ${value?.javaClass?.simpleName}")

        if (value is RetrieveAccountTransactionsParameters) {
            return RetrieveAccountTransactionsParameters::class.jvmName
        }
        else if (value is SepaAccountInfoParameters) {
            println("Returning SepaAccountInfoParameters")
            return SepaAccountInfoParameters::class.jvmName
        }

        return super.idFromValue(value)
    }

    override fun typeFromId(context: DatabindContext, id: String): JavaType {
        return when (id) {
            RetrieveAccountTransactionsParameters::class.jvmName -> _typeFactory.constructSpecializedType(_baseType, RetrieveAccountTransactionsParameters::class.java)
            SepaAccountInfoParameters::class.jvmName -> _typeFactory.constructSpecializedType(_baseType, SepaAccountInfoParameters::class.java)
            else -> _typeFactory.constructFromCanonical(id) // don't know why classes of Lists and Sets also get written as id to output, but for deserialization call to type factory is needed, super.typeFromId() does not work
        }
    }

}