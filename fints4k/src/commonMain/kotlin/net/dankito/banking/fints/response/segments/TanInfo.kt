package net.dankito.banking.fints.response.segments


open class TanInfo(
    parameters: JobParameters,
    val tanProcedureParameters: TwoStepTanProcedureParameters
) : JobParameters(parameters) {

    internal constructor() : this(JobParameters(), TwoStepTanProcedureParameters()) // for object deserializers

}