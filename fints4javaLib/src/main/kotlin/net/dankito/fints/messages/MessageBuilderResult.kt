package net.dankito.fints.messages


open class MessageBuilderResult(
    val isJobAllowed: Boolean,
    val isJobVersionSupported: Boolean,
    val allowedVersions: List<Int>,
    val supportedVersions: List<Int>,
    val createdMessage: String?
) {

    constructor(isJobAllowed: Boolean) : this(isJobAllowed, false, listOf(), listOf(), null)

    constructor(createdMessage: String) : this(true, true, listOf(), listOf(), createdMessage)


    open fun isAllowed(version: Int): Boolean {
        return allowedVersions.contains(version)
    }

}