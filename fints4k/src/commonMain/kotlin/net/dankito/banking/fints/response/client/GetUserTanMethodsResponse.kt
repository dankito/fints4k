package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.response.BankResponse
import net.dankito.banking.fints.response.ResponseParser


open class GetUserTanMethodsResponse(bankResponse: BankResponse)
    : BankResponse(bankResponse.didReceiveResponse, bankResponse.receivedResponse, bankResponse.receivedSegments,
    bankResponse.errorMessage, bankResponse.noTanMethodSelected, bankResponse.messageCreationError) {

    /**
     * comdirect sends "9955::Unzulässiges TAN-Verfahren." even though '999' is a valid TAN method
     * for init dialog if user's TAN methods are not known yet and it contains a '3920:' feedback with user's TAN methods
     * -> if it contains a '3920:' feedback with user's TAN methods, then it's still a success.
     */
    override val successful: Boolean
        get() = noTanMethodSelected == false && couldCreateMessage && didReceiveResponse
                && tanRequiredButUserDidNotEnterOne == false && wrongCredentialsEntered == false
                && (responseContainsErrors == false || containsUsersTanMethodsFeedback())

    protected open fun containsUsersTanMethodsFeedback(): Boolean {
        val usersSupportedTanMethodsFeedback = segmentFeedbacks.flatMap { it.feedbacks }
            .firstOrNull { it.responseCode == ResponseParser.SupportedTanMethodsForUserResponseCode }

        return usersSupportedTanMethodsFeedback != null
    }

}