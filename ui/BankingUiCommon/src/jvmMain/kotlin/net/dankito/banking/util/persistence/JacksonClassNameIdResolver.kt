package net.dankito.banking.util.persistence

import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver
import com.fasterxml.jackson.databind.type.SimpleType
import com.fasterxml.jackson.databind.type.TypeFactory
import net.dankito.banking.fints.response.segments.JobParameters
import net.dankito.banking.fints.response.segments.RetrieveAccountTransactionsInMt940Parameters
import net.dankito.banking.fints.response.segments.SepaAccountInfoParameters
import kotlin.reflect.jvm.jvmName


open class JacksonClassNameIdResolver : ClassNameIdResolver(SimpleType.construct(JobParameters::class.java), TypeFactory.defaultInstance()) {

    override fun idFromValue(value: Any?): String {
        if (value is RetrieveAccountTransactionsInMt940Parameters) {
            return RetrieveAccountTransactionsInMt940Parameters::class.jvmName
        }
        else if (value is SepaAccountInfoParameters) {
            return SepaAccountInfoParameters::class.jvmName
        }

        return super.idFromValue(value)
    }

    override fun typeFromId(context: DatabindContext?, id: String?): JavaType {
        return when (id) {
            RetrieveAccountTransactionsInMt940Parameters::class.jvmName -> _typeFactory.constructSpecializedType(_baseType, RetrieveAccountTransactionsInMt940Parameters::class.java)
            SepaAccountInfoParameters::class.jvmName -> _typeFactory.constructSpecializedType(_baseType, SepaAccountInfoParameters::class.java)
            else -> super.typeFromId(context, id)
        }
    }

}