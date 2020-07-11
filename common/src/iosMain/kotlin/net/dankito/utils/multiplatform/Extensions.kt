package net.dankito.utils.multiplatform

import platform.Foundation.NSArray
import platform.Foundation.NSDictionary


fun <T> NSArray.toList(): List<T> {
    val result = mutableListOf<T>()

    for (i in 0L until this.count.toLong()) {
        result.add(this.objectAtIndex(i.toULong()) as T)
    }

    return result
}


fun NSDictionary.getString(key: String): String? {
    return this.objectForKey(key) as? String
}

fun NSDictionary.getString(key: String, defaultValue: String): String {
    return this.getString(key) ?: defaultValue
}

fun NSDictionary.getStringOrEmpty(key: String): String {
    return this.getString(key, "")
}