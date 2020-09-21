package net.dankito.banking.fints.response.segments


open class TwoStepTanProcedureParameters(
    val oneStepProcedureAllowed: Boolean,
    val moreThanOneTanDependentJobPerMessageAllowed: Boolean,
    val jobHashValue: String, // not evaluated for PIN/TAN
    val methodParameters: List<TanMethodParameters>
) {

    internal constructor() : this(false, false, "", listOf()) // for object deserializers

}