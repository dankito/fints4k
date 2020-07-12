package net.dankito.banking.ui.model.tan


open class TanProcedure(
    val displayName: String,
    val type: TanProcedureType,
    val bankInternalProcedureCode: String
) {


    internal constructor() : this("", TanProcedureType.EnterTan, "") // for object deserializers


    override fun toString(): String {
        return "$displayName ($type, ${bankInternalProcedureCode})"
    }

}