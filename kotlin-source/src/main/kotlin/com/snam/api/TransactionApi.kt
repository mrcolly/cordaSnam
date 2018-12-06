package com.snam.api

import com.snam.POJO.ResponsePojo
import com.snam.POJO.TransactionPojo
import com.snam.flow.TransactionFlow.Starter
import com.snam.schema.TransactionSchemaV1

import com.snam.state.TransactionState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.*
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED

val SERVICE_NAMES = listOf("Notary", "Network Map Service")


@Path("transaction")
class TransactionApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<TransactionApi>()
    }

    @GET
    @Path("getAll")
    @Produces(MediaType.APPLICATION_JSON)
    fun getTransactions(@QueryParam("page") page: Int): List<StateAndRef<TransactionState>> {

        var myPage = page

        if (myPage < 1){
            myPage = 1
        }

        // val customSort = SortAttribute.Custom(TransactionSchemaV1.PersistentTransaction::class.java, TransactionSchemaV1.PersistentTransaction::buyerName.toString())

        return rpcOps.vaultQueryBy<TransactionState>(
                QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED),
                PageSpecification(myPage, DEFAULT_PAGE_SIZE),
                Sort(setOf(Sort.SortColumn(SortAttribute.Standard(Sort.CommonStateAttribute.STATE_REF_TXN_ID), Sort.Direction.DESC)))
        ).states
    }

    /**
     * Initiates a flow to agree an IOU between two parties.
     *
     * Once the flow finishes it will have written the IOU to ledger. Both the lender and the borrower will be able to
     * see it when calling /api/example/ious on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */
    @POST
    @Path("insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createTransaction(req : TransactionPojo): Response {

        try {
            val buyer : Party = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(req.buyer))!!
            val seller : Party = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(req.seller))!!

            if(buyer == null || seller == null)
            {
                val resp = ResponsePojo("ERROR", "Cannot find buyer or seller")
                return Response.status(BAD_REQUEST).entity(resp).build()
            }


            val signedTx = rpcOps.startTrackedFlow(::Starter,
                    buyer,
                    seller,
                    req)
                    .returnValue.getOrThrow()

            val resp = ResponsePojo("SUCCESS","transaction "+signedTx.toString()+" committed to ledger.")
            return Response.status(CREATED).entity(resp).build()

        } catch (ex: Throwable) {
            val msg = ex.message
            logger.error(ex.message, ex)
            val resp = ResponsePojo("ERROR", msg!!)
            return Response.status(BAD_REQUEST).entity(resp).build()
        }
    }
}