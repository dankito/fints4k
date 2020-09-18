package net.dankito.banking.fints.response.segments


open class JobParameters(
    open val jobName: String,
    open val maxCountJobs: Int,
    open val minimumCountSignatures: Int,
    open val securityClass: Int?,
    segmentString: String
)
    : ReceivedSegment(segmentString) {


    internal constructor() : this("", 0, 0, null, "0:0:0") // for object deserializers


    constructor(parameters: JobParameters)
            : this(parameters.jobName, parameters.maxCountJobs, parameters.minimumCountSignatures,
                    parameters.securityClass, parameters.segmentString)


    override fun toString(): String {
        return "$jobName $segmentVersion"
    }

}