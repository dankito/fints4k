package net.dankito.banking.persistence.mapper

import org.mapstruct.BeforeMapping
import org.mapstruct.MappingTarget
import org.mapstruct.TargetType
import java.util.*


open class CycleAvoidingMappingContext {

    private val knownInstances: MutableMap<Any, Any> = IdentityHashMap()


    /**
     * Gets an instance out of this context if it is already mapped.
     */
    @BeforeMapping
    open fun <T> getMappedInstance(source: Any, @TargetType targetType: Class<T>): T {
        return targetType.cast(knownInstances[source])
    }

    /**
     * Puts an instance into the cache, so that it can be remembered to avoid endless mapping.
     */
    @BeforeMapping
    open fun storeMappedInstance(source: Any, @MappingTarget target: Any) {
        knownInstances[source] = target
    }

}