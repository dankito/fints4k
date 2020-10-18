package net.dankito.banking.bankfinder.rest

import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.bankfinder.InMemoryBankFinder
import org.jboss.resteasy.annotations.jaxrs.PathParam
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Path("/bankfinder")
class BankFinderResource {

    protected var bankFinder = InMemoryBankFinder()


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{query}")
    fun findBank(@PathParam query: String): List<BankInfo> {
        return bankFinder.findBankByNameBankCodeOrCity(query)
    }

}