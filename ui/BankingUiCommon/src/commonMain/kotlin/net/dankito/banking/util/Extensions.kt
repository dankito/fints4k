package net.dankito.banking.util


fun String.ofMaxLength(maxLength: Int): String {
    if(this.length > maxLength && maxLength > 0) {
        return this.substring(0, maxLength)
    }

    return this
}


fun <T> Collection<T>.containsExactly(vararg items: T): Boolean {
    return containsExactly(items.toList())
}

fun <T> Collection<T>.containsExactly(otherCollection: Collection<T>): Boolean {
    if (this.size != otherCollection.size) {
        return false
    }

    for (otherItem in otherCollection) {
        if (this.contains(otherItem) == false) {
            return false
        }
    }

    return true
}