package net.dankito.fints.response.segments


open class TanInfo(
    jobName: String,
    maxCountJobs: Int,
    minimumCountSignatures: Int,
    securityClass: Int?,
    val tanProcedureParameters: TwoStepTanProcedureParameters,

    segmentString: String
)
    : JobParameters(jobName, maxCountJobs, minimumCountSignatures, securityClass, segmentString) {

    constructor(parameters: JobParameters, tanProcedureParameters: TwoStepTanProcedureParameters)
            : this(parameters.jobName, parameters.maxCountJobs, parameters.minimumCountSignatures,
                    parameters.securityClass, tanProcedureParameters, parameters.segmentString)

}