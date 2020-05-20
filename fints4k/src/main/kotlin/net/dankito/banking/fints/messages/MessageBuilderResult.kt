package net.dankito.banking.fints.messages

import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.implementierte.ZweiSchrittTanEinreichung


open class MessageBuilderResult(
    val isJobAllowed: Boolean,
    val isJobVersionSupported: Boolean,
    val allowedVersions: List<Int>,
    val supportedVersions: List<Int>,
    val createdMessage: String?,
    val messageBodySegments: List<Segment> = listOf()
) {

    constructor(isJobAllowed: Boolean) : this(isJobAllowed, false, listOf(), listOf(), null)

    constructor(createdMessage: String, messageBodySegments: List<Segment>)
            : this(true, true, listOf(), listOf(), createdMessage, messageBodySegments)


    open fun isAllowed(version: Int): Boolean {
        return allowedVersions.contains(version)
    }

    open val getHighestAllowedVersion: Int?
        get() = allowedVersions.max()

    open fun isSendEnteredTanMessage(): Boolean {
        // contains only a ZweiSchrittTanEinreichung segment
        return messageBodySegments.size == 1
                && messageBodySegments.first() is ZweiSchrittTanEinreichung
    }

}