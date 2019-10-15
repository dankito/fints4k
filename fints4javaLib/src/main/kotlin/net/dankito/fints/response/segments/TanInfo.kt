package net.dankito.fints.response.segments


open class TanInfo(
    jobName: String,
    maxCountJobs: Int,
    minimumCountSignatures: Int,
    securityClass: Int?,
    val tanProcedureParameters: TwoStepTanProcedureParameters,

    segmentString: String
)
    : SupportedJob(jobName, maxCountJobs, minimumCountSignatures, securityClass, segmentString) {

    constructor(supportedJob: SupportedJob, tanProcedureParameters: TwoStepTanProcedureParameters)
            : this(supportedJob.jobName, supportedJob.maxCountJobs, supportedJob.minimumCountSignatures,
                    supportedJob.securityClass, tanProcedureParameters, supportedJob.segmentString)

}