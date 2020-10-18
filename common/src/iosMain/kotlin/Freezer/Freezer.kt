package Freezer

import kotlin.native.concurrent.freeze


fun <T> freeze(obj: T): T {
    return obj.freeze()
}