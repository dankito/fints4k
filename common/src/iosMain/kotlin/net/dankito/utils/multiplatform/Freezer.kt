package net.dankito.utils.multiplatform

import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen


actual class Freezer {

    actual companion object {

        actual fun <T> freeze(obj: T): T {
            return obj.freeze()
        }

        actual fun isFrozen(obj: Any): Boolean {
            return obj.isFrozen
        }

    }

}