package net.dankito.banking.ui.model.tan

import net.dankito.banking.ui.model.Displayable
import net.dankito.utils.multiplatform.UUID
import kotlin.jvm.Transient


open class TanMethod(
    @Transient
    override val displayName: String,
    @Transient
    open val type: TanMethodType,
    @Transient
    open val bankInternalMethodCode: String,
    @Transient
    open val maxTanInputLength: Int? = null,
    @Transient
    open val allowedTanFormat: AllowedTanFormat = AllowedTanFormat.Alphanumeric
) : Displayable {


    internal constructor() : this("", TanMethodType.EnterTan, "") // for object deserializers


    @Transient
    open val isNumericTan: Boolean = allowedTanFormat == AllowedTanFormat.Numeric


    open var technicalId: String = UUID.random()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TanMethod) return false

        if (displayName != other.displayName) return false
        if (type != other.type) return false
        if (bankInternalMethodCode != other.bankInternalMethodCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = displayName.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + bankInternalMethodCode.hashCode()
        return result
    }


    override fun toString(): String {
        return "$displayName ($type, ${bankInternalMethodCode})"
    }

}