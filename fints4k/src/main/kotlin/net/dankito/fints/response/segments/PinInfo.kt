package net.dankito.fints.response.segments

import net.dankito.fints.messages.datenelemente.implementierte.tan.JobTanConfiguration


open class PinInfo(
    parameters: JobParameters,
    val minPinLength: Int?,
    val maxPinLength: Int?,
    val minTanLength: Int?,
    val userIdHint: String?,
    val customerIdHint: String?,
    val jobTanConfiguration: List<JobTanConfiguration>
)
    : JobParameters(parameters)