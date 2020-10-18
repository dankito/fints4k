package net.dankito.utils.multiplatform.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.utils.multiplatform.Date


open class DateDeserializer : StdDeserializer<Date>(Date::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Date? {
        val millisSinceEpoch = parser.readValueAs(Long::class.java)

        if (millisSinceEpoch == null) {
            return null
        }
        
        return Date(millisSinceEpoch)
    }

}