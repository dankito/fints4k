package net.dankito.fints.response.segments


open class JobParameters(
    val jobName: String,
    val maxCountJobs: Int,
    val minimumCountSignatures: Int,
    val securityClass: Int?,
    segmentString: String // TODO: when serializing / deserializing we don't care for segment string -> remove it
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