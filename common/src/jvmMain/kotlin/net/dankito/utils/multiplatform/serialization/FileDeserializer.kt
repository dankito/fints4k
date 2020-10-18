package net.dankito.utils.multiplatform.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.utils.multiplatform.File


open class FileDeserializer : StdDeserializer<File>(File::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): File? {
        val path = parser.readValueAs(String::class.java)

        if (path == null || path == "null") {
            return null
        }
        
        return File(path)
    }

}