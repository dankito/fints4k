package net.dankito.banking.persistence.entities

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.io.Serializable
import java.util.*
import javax.persistence.*


/**
 * Base class for common data fields of all entities.
 */
@MappedSuperclass
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id")
abstract class BaseEntity : Serializable {

    companion object {
        const val IdColumnName = "id"

        const val CreatedOnColumnName = "created_on"

        const val ModifiedOnColumnName = "modified_on"

        const val VersionColumnName = "version"

        const val DeletedColumnName = "deleted"
    }


    @Column(name = IdColumnName)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: String? = null

    @Column(name = CreatedOnColumnName, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var createdOn: Date = Date()
        private set

    @Column(name = ModifiedOnColumnName)
    @Temporal(TemporalType.TIMESTAMP)
    var modifiedOn: Date = createdOn
        private set

    @Version
    @Column(name = VersionColumnName, nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    var version: Long? = null
        private set

    @Column(name = DeletedColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    var deleted = false
        private set


    @Transient
    fun isPersisted(): Boolean {
        return id != null
    }

    @PrePersist
    protected open fun prePersist() {
        createdOn = Date()
        modifiedOn = createdOn
        version = 1L
    }

    @PreUpdate
    protected open fun preUpdate() {
        modifiedOn = Date()
    }

    @PreRemove
    protected open fun preRemove() {
        modifiedOn = Date()
        deleted = true
    }

}