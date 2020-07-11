package net.dankito.utils.multiplatform

import platform.Foundation.NSUUID


actual class UUID {

    actual companion object {

        actual fun random(): String {
            return NSUUID().UUIDString
        }

    }

}