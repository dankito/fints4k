package net.dankito.utils.multiplatform.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.utils.multiplatform.BigDecimal


open class BigDecimalDeserializer : StdDeserializer<BigDecimal>(BigDecimal::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): BigDecimal? {
        val doubleString = parser.readValueAs(String::class.java)

        if (doubleString.isNullOrBlank() || doubleString == "null") {
            return null
        }

        return BigDecimal(doubleString)
    }

}