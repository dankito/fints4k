package net.dankito.banking.fints.rest

import net.dankito.banking.fints.rest.model.dto.request.AddAccountRequestDto
import net.dankito.banking.fints.rest.mapper.DtoMapper
import net.dankito.banking.fints.rest.model.dto.request.GetAccountsTransactionsRequestDto
import net.dankito.banking.fints.rest.model.dto.response.GetAccountsTransactionsResponseDto
import net.dankito.banking.fints.rest.service.fints4kService
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/")
class fints4kResource {

    @Inject
    protected val service = fints4kService()

    protected val mapper = DtoMapper()


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addaccount")
    fun addAccount(
        request: AddAccountRequestDto,
        @DefaultValue("false") @QueryParam("showRawResponse") showRawResponse: Boolean
    ): Any {
        val clientResponse = service.getAddAccountResponse(request)

        if (showRawResponse) {
            return clientResponse
        }

        return mapper.map(clientResponse)
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("transactions")
    fun getAccountTransactions(request: GetAccountsTransactionsRequestDto): GetAccountsTransactionsResponseDto {
        val accountsTransactions = service.getAccountTransactions(request)

        return mapper.mapTransactions(accountsTransactions)
    }

}