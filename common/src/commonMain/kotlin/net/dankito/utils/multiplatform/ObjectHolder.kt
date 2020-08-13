package net.dankito.utils.multiplatform


open class ObjectHolder<T>(
    var value: T
) {

    override fun toString(): String {
        return value?.toString() ?: "Value is null"
    }

}