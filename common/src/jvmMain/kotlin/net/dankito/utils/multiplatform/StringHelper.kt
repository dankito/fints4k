package net.dankito.utils.multiplatform

import kotlin.String


actual class StringHelper {

    actual companion object {

        actual fun format(format: String, vararg args: Any?): String {
            return String.format(format, *args)
        }

    }

}