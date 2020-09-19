package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.response.Response
import net.dankito.banking.fints.response.segments.TanResponse


open class FinTsClientResponse(

    open val successful: Boolean,

    open val noTanProcedureSelected: Boolean,

    open val isStrongAuthenticationRequired: Boolean,
    open val tanRequired: TanResponse? = null,

    open val errorsToShowToUser: List<String> = listOf(),

    /**
     * When a serious error occurred during web request or response parsing.
     */
    open val errorMessage: String? = null,

    open val userCancelledAction: Boolean = false,

    open val tanRequiredButWeWereToldToAbortIfSo: Boolean = false,

    open val isJobAllowed: Boolean = true,
    open val isJobVersionSupported: Boolean = true,
    open val allowedVersions: List<Int> = listOf(),
    open val supportedVersions: List<Int> = listOf()
) {


    constructor(response: Response) : this(response.successful, response.noTanProcedureSelected,
        response.isStrongAuthenticationRequired, response.tanResponse, response.errorsToShowToUser,
        response.errorMessage, response.tanRequiredButUserDidNotEnterOne, response.tanRequiredButWeWereToldToAbortIfSo,
        response.messageCreationError?.isJobAllowed ?: true,
        response.messageCreationError?.isJobVersionSupported ?: true,
        response.messageCreationError?.allowedVersions ?: listOf(),
        response.messageCreationError?.supportedVersions ?: listOf())


    open fun toResponse(): Response {
        return Response(this.successful)
    }


    override fun toString(): String {
        if (noTanProcedureSelected) {
            return "Error: No TAN procedure selected"
        }

        if (isJobAllowed == false) {
            return "Error: Job is not allowed"
        }

        if (isJobVersionSupported == false) {
            return "Error: Job version is not supported. Supported versions are $supportedVersions"
        }

        return "successful = $successful"
    }

}