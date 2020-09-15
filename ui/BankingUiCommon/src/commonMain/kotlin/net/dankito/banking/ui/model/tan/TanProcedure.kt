package net.dankito.banking.ui.model.tan

import net.dankito.banking.ui.model.Displayable
import net.dankito.utils.multiplatform.UUID
import kotlin.jvm.Transient


open class TanProcedure(
    override val displayName: String,
    open val type: TanProcedureType,
    open val bankInternalProcedureCode: String,
    open val maxTanInputLength: Int? = null,
    open val allowedTanFormat: AllowedTanFormat = AllowedTanFormat.Alphanumeric
) : Displayable {


    internal constructor() : this("", TanProcedureType.EnterTan, "") // for object deserializers


    @Transient
    open val isNumericTan: Boolean = allowedTanFormat == AllowedTanFormat.Numeric


    open var technicalId: String = UUID.random()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TanProcedure) return false

        if (displayName != other.displayName) return false
        if (type != other.type) return false
        if (bankInternalProcedureCode != other.bankInternalProcedureCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = displayName.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + bankInternalProcedureCode.hashCode()
        return result
    }


    override fun toString(): String {
        return "$displayName ($type, ${bankInternalProcedureCode})"
    }

}