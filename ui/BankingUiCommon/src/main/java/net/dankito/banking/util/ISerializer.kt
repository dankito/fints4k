package net.dankito.banking.util

import java.io.File


interface ISerializer {

    fun serializeObject(obj: Any, outputFile: File)

    fun <T> deserializeObject(serializedObjectFile: File, objectClass: Class<T>, vararg genericParameterTypes: Class<*>): T?

    fun <T> deserializeListOr(serializedObjectFile: File, genericListParameterType: Class<T>,
                              defaultValue: List<T> = listOf()) : List<T>

}