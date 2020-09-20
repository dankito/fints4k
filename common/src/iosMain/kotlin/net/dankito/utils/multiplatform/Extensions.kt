package net.dankito.utils.multiplatform

import kotlinx.cinterop.*
import platform.Foundation.*


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


fun NSComparisonResult.toCompareToResult(): Int {
    return when (this) {
        NSOrderedSame -> 0
        NSOrderedAscending -> -1
        NSOrderedDescending -> 1
        else -> 0
    }
}


fun String.toNSData(): NSData {
    return this.encodeToByteArray().toNSData()
}

fun ByteArray.toNSData(): NSData = NSMutableData().apply {
    if (isEmpty()) return@apply
    this@toNSData.usePinned {
        appendBytes(it.addressOf(0), size.convert())
    }
}

fun NSData.toByteArray(): ByteArray {
    val data: CPointer<ByteVar> = bytes!!.reinterpret()

    return ByteArray(length.toInt()) { index -> data[index] }
}