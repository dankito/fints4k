package net.dankito.fints.response.segments


open class TanInfo(
    val maxCountJobs: Int,
    val minimumCountSignatures: Int,
    val securityClass: String, // not used for PIN/TAN
    val tanProcedureParameters: TwoStepTanProcedureParameters,

    segmentString: String
)
    : ReceivedSegment(segmentString)