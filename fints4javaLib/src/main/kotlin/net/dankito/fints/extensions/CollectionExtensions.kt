package net.dankito.fints.extensions


fun <T> Collection<T>.containsAny(otherCollection: Collection<T>): Boolean {
    for (otherItem in otherCollection) {
        if (this.contains(otherItem)) {
            return true
        }
    }

    return false
}