package net.dankito.fints.response.segments


open class TanInfo(
    parameters: JobParameters,
    val tanProcedureParameters: TwoStepTanProcedureParameters
)
    : JobParameters(parameters)