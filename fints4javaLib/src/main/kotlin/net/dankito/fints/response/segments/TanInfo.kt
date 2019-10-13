package net.dankito.fints.response.segments


open class TanInfo(
    jobName: String,
    maxCountJobs: Int,
    minimumCountSignatures: Int,
    securityClass: Int?,
    val tanProcedureParameters: TwoStepTanProcedureParameters,

    segmentString: String
)
    : AllowedJob(jobName, maxCountJobs, minimumCountSignatures, securityClass, segmentString) {

    constructor(allowedJob: AllowedJob, tanProcedureParameters: TwoStepTanProcedureParameters)
            : this(allowedJob.jobName, allowedJob.maxCountJobs, allowedJob.minimumCountSignatures,
                    allowedJob.securityClass, tanProcedureParameters, allowedJob.segmentString)

}