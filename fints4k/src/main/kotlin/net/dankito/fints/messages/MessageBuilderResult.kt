package net.dankito.fints.messages

import net.dankito.fints.messages.segmente.Segment


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

}