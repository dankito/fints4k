package net.dankito.fints.response.segments


open class JobParameters(
    val jobName: String,
    val maxCountJobs: Int,
    val minimumCountSignatures: Int,
    val securityClass: Int?,
    segmentString: String
)
    : ReceivedSegment(segmentString) {


    constructor(parameters: JobParameters)
            : this(parameters.jobName, parameters.maxCountJobs, parameters.minimumCountSignatures,
                    parameters.securityClass, parameters.segmentString)


    override fun toString(): String {
        return "$jobName $segmentVersion"
    }

}