package net.dankito.fints.response


open class GetUserTanProceduresResponse(bankResponse: Response)
    : Response(bankResponse.didReceiveResponse, bankResponse.receivedResponse, bankResponse.receivedSegments,
    bankResponse.exception, bankResponse.noTanProcedureSelected, bankResponse.messageCreationError) {

    /**
     * comdirect sends "9955::Unzulässiges TAN-Verfahren." even though '999' is a valid TAN procedure
     * for init dialog if user's TAN procedures are not known yet
     * -> check if the only error is '9955', then it's still a success.
     */
    override val successful: Boolean
        get() = noTanProcedureSelected == false && couldCreateMessage && didReceiveResponse
                && tanRequiredButUserDidNotEnterOne == false
                && (responseContainsErrors == false || containsOnlyInvalidTanProcedureError())

    protected open fun containsOnlyInvalidTanProcedureError(): Boolean {
        val errorFeedbacks = segmentFeedbacks.flatMap { it.feedbacks }.filter { it.isError }

        return errorFeedbacks.size == 1 && errorFeedbacks.first().responseCode == 9955
    }

}