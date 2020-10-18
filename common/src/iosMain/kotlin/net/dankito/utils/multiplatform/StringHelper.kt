package net.dankito.utils.multiplatform

import kotlin.String
import platform.Foundation.*


actual class StringHelper {

    actual companion object {

        actual fun format(format: String, vararg args: Any?): String {
            val adjustedFormat = format.replace("%s", "%@") // Objective-C uses %@ for strings // TODO: also replace %$1s, ...

//            return NSString.stringWithFormat("%@ %@ %d", "один" as NSString, "two" as NSString, 3)
            //return NSString.stringWithFormat(adjustedFormat, args.firstOrNull()) // spread operator is not supported for varadic functions, seehttps://github.com/JetBrains/kotlin-native/issues/1834

            // as spread operator is not supported for varadic functions tried to pass arguments one by one, but didn't work either
            return when (args.size) {
                0 -> format
                1 -> format(adjustedFormat, args.first())
                2 -> format(adjustedFormat, args.first(), args.get(2))
                else -> format(adjustedFormat, args.first(), args.get(2), args.get(3))
            }
        }

        fun format(format: String, arg1: Any): String {
            return NSString.stringWithFormat(format, arg1 as? NSString ?: arg1)
        }

        fun format(format: String, arg1: Any, arg2: Any): String {
            return NSString.stringWithFormat(format, arg1 as? NSString ?: arg1, arg2 as? NSString ?: arg2)
        }

        fun format(format: String, arg1: Any, arg2: Any, arg3: Any): String {
            return NSString.stringWithFormat(format, arg1 as? NSString ?: arg1, arg2 as? NSString ?: arg2, arg3 as? NSString ?: arg3)
        }

    }

}