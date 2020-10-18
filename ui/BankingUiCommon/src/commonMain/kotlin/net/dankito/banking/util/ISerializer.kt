package net.dankito.banking.util

import net.dankito.utils.multiplatform.File
import kotlin.reflect.KClass


interface ISerializer {

    fun serializeObjectToString(obj: Any): String?

    fun serializeObject(obj: Any, outputFile: File)


    fun <T : Any> deserializeObject(serializedObject: String, objectClass: KClass<T>, vararg genericParameterTypes: KClass<*>): T?

    fun <T : Any> deserializeObject(serializedObjectFile: File, objectClass: KClass<T>, vararg genericParameterTypes: KClass<*>): T?

    fun <T : Any> deserializeListOr(serializedObject: String, genericListParameterType: KClass<T>,
                                    defaultValue: List<T> = listOf()) : List<T>

    fun <T : Any> deserializeListOr(serializedObjectFile: File, genericListParameterType: KClass<T>,
                                    defaultValue: List<T> = listOf()) : List<T>

}