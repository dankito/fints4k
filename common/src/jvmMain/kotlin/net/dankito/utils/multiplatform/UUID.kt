package net.dankito.utils.multiplatform

import java.util.UUID


actual class UUID {

    actual companion object {

        actual fun random(): String {
            return UUID.randomUUID().toString()
        }

    }

}