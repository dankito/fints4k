package net.dankito.utils.multiplatform

import kotlin.String


expect class StringHelper {

    companion object {

        fun format(format: String, vararg args: Any?): String

    }

}