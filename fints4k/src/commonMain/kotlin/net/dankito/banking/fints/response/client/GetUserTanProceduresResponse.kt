package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.response.Response
import net.dankito.banking.fints.response.ResponseParser


open class GetUserTanProceduresResponse(bankResponse: Response)
    : Response(bankResponse.didReceiveResponse, bankResponse.receivedResponse, bankResponse.receivedSegments,
    bankResponse.errorMessage, bankResponse.noTanProcedureSelected, bankResponse.messageCreationError) {

    /**
     * comdirect sends "9955::Unzulässiges TAN-Verfahren." even though '999' is a valid TAN procedure
     * for init dialog if user's TAN procedures are not known yet and it contains a '3920:' feedback with user's TAN procedures
     * -> if it contains a '3920:' feedback with user's TAN procedures, then it's still a success.
     */
    override val successful: Boolean
        get() = noTanProcedureSelected == false && couldCreateMessage && didReceiveResponse
                && tanRequiredButUserDidNotEnterOne == false
                && (responseContainsErrors == false || containsUsersTanProceduresFeedback())

    protected open fun containsUsersTanProceduresFeedback(): Boolean {
        val usersSupportedTanProceduresFeedback = segmentFeedbacks.flatMap { it.feedbacks }
            .firstOrNull { it.responseCode == ResponseParser.SupportedTanProceduresForUserResponseCode }

        return usersSupportedTanProceduresFeedback != null
    }

}