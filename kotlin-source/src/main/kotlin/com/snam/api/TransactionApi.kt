package com.snam.api

import com.snam.POJO.ResponsePojo
import com.snam.POJO.TransactionPojo
import com.snam.flow.TransactionFlow
import com.snam.schema.TransactionSchemaV1

import com.snam.state.TransactionState
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
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
import net.corda.core.node.services.vault.QueryCriteria
import java.text.SimpleDateFormat
import java.util.*


//val SERVICE_NAMES = listOf("Notary", "Network Map Service")


@Path("transaction")
class TransactionApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<TransactionApi>()
    }

    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    fun getTransactionsByParams(@DefaultValue("1") @QueryParam("page") page: Int,
                                @DefaultValue("") @QueryParam("id") idTransaction: String,
                                @DefaultValue("") @QueryParam("buyer") buyer: String,
                                @DefaultValue("") @QueryParam("seller") seller: String,
                                @DefaultValue("1990-01-01") @QueryParam("from") from: String,
                                @DefaultValue("2050-12-31") @QueryParam("to") to: String,
                                @DefaultValue("unconsumed") @QueryParam("status") status: String): Response {


        try{
        var myPage = page

        if (myPage < 1){
            myPage = 1
        }

        var myStatus = Vault.StateStatus.UNCONSUMED

        when(status){
            "consumed" -> myStatus = Vault.StateStatus.CONSUMED
            "all" -> myStatus = Vault.StateStatus.ALL
        }

        var criteria : QueryCriteria = QueryCriteria.VaultQueryCriteria(myStatus)
        val results = builder {


            if(idTransaction.length > 0){
                val customCriteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(UUID.fromString(idTransaction)), status = myStatus)
                criteria = criteria.and(customCriteria)
            }

            if(seller.length > 0){
                val idEqual = TransactionSchemaV1.PersistentTransaction::sellerName.equal(seller)
                val customCriteria = QueryCriteria.VaultCustomQueryCriteria(idEqual, myStatus)
                criteria = criteria.and(customCriteria)
            }

            if(buyer.length > 0){
                val idEqual = TransactionSchemaV1.PersistentTransaction::buyerName.equal(buyer)
                val customCriteria = QueryCriteria.VaultCustomQueryCriteria(idEqual, myStatus)
                criteria = criteria.and(customCriteria)
            }


            if(from.length > 0 && to.length > 0){
                val format = SimpleDateFormat("yyyy-MM-dd")
                var myFrom = format.parse(from)
                var myTo = format.parse(to)
                var dateBetween = TransactionSchemaV1.PersistentTransaction::data.between(myFrom.toInstant(), myTo.toInstant())
                val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateBetween, myStatus)
                criteria = criteria.and(customCriteria)
            }

            val results = rpcOps.vaultQueryBy<TransactionState>(
                    criteria,
                    PageSpecification(myPage, DEFAULT_PAGE_SIZE),
                    Sort(setOf(Sort.SortColumn(SortAttribute.Standard(Sort.VaultStateAttribute.RECORDED_TIME), Sort.Direction.DESC)))
             ).states

            return Response.ok(results).build()
        }
    }catch (ex: Exception){
        val msg = ex.message
        logger.error(ex.message, ex)
        val resp = ResponsePojo("ERROR", msg!!)
        return Response.status(BAD_REQUEST).entity(resp).build()
    }

    }


    @POST
    @Path("insert")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createTransaction(req : TransactionPojo): Response {

        try {
            val buyer : Party = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(req.buyer))!!
            val seller : Party = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(req.seller))!!
            val snam : Party = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse("O=Sman,L=Milan,C=IT"))!!

            val signedTx = rpcOps.startTrackedFlow(TransactionFlow::Starter,
                    buyer,
                    seller,
                    snam,
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
