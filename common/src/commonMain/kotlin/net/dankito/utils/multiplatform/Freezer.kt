package net.dankito.utils.multiplatform


expect class Freezer() {

    companion object {

        fun <T> freeze(obj: T): T

        fun isFrozen(obj: Any): Boolean

    }

}