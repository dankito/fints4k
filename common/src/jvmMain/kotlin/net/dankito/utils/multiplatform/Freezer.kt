package net.dankito.utils.multiplatform


actual class Freezer {

    actual companion object {

        actual fun <T> freeze(obj: T): T {
            return obj // no op
        }

        actual fun isFrozen(obj: Any): Boolean {
            return false // freezing is only possible on Kotlin/Native
        }

    }

}