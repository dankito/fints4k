package net.dankito.fints.response.client

import net.dankito.fints.response.Response
import net.dankito.fints.response.segments.TanResponse


open class FinTsClientResponse(

    val isSuccessful: Boolean,

    val isStrongAuthenticationRequired: Boolean,
    val tanRequired: TanResponse? = null,

    val errorsToShowToUser: List<String> = listOf(),

    /**
     * When a serious error occurred during web request or response parsing.
     */
    val exception: Exception? = null,

    val isJobAllowed: Boolean = true,
    val isJobVersionSupported: Boolean = true,
    val allowedVersions: List<Int> = listOf(),
    val supportedVersions: List<Int> = listOf()
) {


    constructor(response: Response) : this(response.successful, response.isStrongAuthenticationRequired,
        response.tanResponse, response.errorsToShowToUser, response.exception,
        response.messageCreationError?.isJobAllowed ?: true,
        response.messageCreationError?.isJobVersionSupported ?: true,
        response.messageCreationError?.allowedVersions ?: listOf(),
        response.messageCreationError?.supportedVersions ?: listOf())

}