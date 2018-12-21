package com.snam.api

import com.snam.POJO.IssueTransactionPojo
import com.snam.POJO.ProposalPojo
import com.snam.POJO.ResponsePojo
import com.snam.flow.ProposalFlow
import com.snam.flow.TransactionFlow
import com.snam.schema.ProposalSchemaV1
import com.snam.state.ProposalState
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

@Path("proposal")
class ProposalApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<ProposalApi>()
    }


    @GET
    @Path("get/myProposals")
    @Produces(MediaType.APPLICATION_JSON)
    fun getMyProposalsByParams(@DefaultValue("1") @QueryParam("page") page: Int,
                                @DefaultValue("") @QueryParam("id") idProposal: String,
                                @DefaultValue("") @QueryParam("counterpart") counterpart: String,
                                @DefaultValue("1990-01-01") @QueryParam("from") from: String,
                                @DefaultValue("2050-12-31") @QueryParam("to") to: String): Response {

        try{
            var myPage = page

            if (myPage < 1){
                myPage = 1
            }


            var criteria : QueryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)

            val results = builder {

                val issuerEqual = ProposalSchemaV1.PersistentProposal::issuer.equal(myLegalName.toString())
                val firstCriteria = QueryCriteria.VaultCustomQueryCriteria(issuerEqual)
                criteria = criteria.and(firstCriteria)

                if(idProposal.length > 0){
                    val customCriteria = QueryCriteria.LinearStateQueryCriteria( uuid = listOf(UUID.fromString(idProposal)))
                    criteria = criteria.and(customCriteria)
                }

                if(counterpart.length > 0){
                    val idEqual = ProposalSchemaV1.PersistentProposal::counterpart.equal(counterpart)
                    val customCriteria = QueryCriteria.VaultCustomQueryCriteria(idEqual)
                    criteria = criteria.and(customCriteria)
                }

                if(from.length > 0 && to.length > 0){
                    val format = SimpleDateFormat("yyyy-MM-dd")
                    var myFrom = format.parse(from)
                    var myTo = format.parse(to)
                    var dateBetween = ProposalSchemaV1.PersistentProposal::data.between(myFrom, myTo)
                    val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateBetween)
                    criteria = criteria.and(customCriteria)
                }

                val results = rpcOps.vaultQueryBy<ProposalState>(
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

    @GET
    @Path("get/unconsumedReceivedProposals")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUnconsumedReceivedProposalsByParams(@DefaultValue("1") @QueryParam("page") page: Int,
                                @DefaultValue("") @QueryParam("id") idProposal: String,
                                @DefaultValue("") @QueryParam("issuer") issuer: String,
                                @DefaultValue("1990-01-01") @QueryParam("from") from: String,
                                @DefaultValue("2050-12-31") @QueryParam("to") to: String): Response {

        try {
        var myPage = page

        if (myPage < 1){
            myPage = 1
        }


        var criteria : QueryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

        val results = builder {

            val counterpartEqual = ProposalSchemaV1.PersistentProposal::counterpart.equal(myLegalName.toString())
            val firstCriteria = QueryCriteria.VaultCustomQueryCriteria(counterpartEqual)
            criteria = criteria.and(firstCriteria)

            if(idProposal.length > 0){
                val customCriteria = QueryCriteria.LinearStateQueryCriteria( uuid = listOf(UUID.fromString(idProposal)))
                criteria = criteria.and(customCriteria)
            }

            if(issuer.length > 0){
                val idEqual = ProposalSchemaV1.PersistentProposal::issuer.equal(issuer)
                val customCriteria = QueryCriteria.VaultCustomQueryCriteria(idEqual)
                criteria = criteria.and(customCriteria)
            }

            if(from.length > 0 && to.length > 0){
                val format = SimpleDateFormat("yyyy-MM-dd")
                var myFrom = format.parse(from)
                var myTo = format.parse(to)
                var dateBetween = ProposalSchemaV1.PersistentProposal::data.between(myFrom, myTo)
                val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateBetween)
                criteria = criteria.and(customCriteria)
            }

            val results = rpcOps.vaultQueryBy<ProposalState>(
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

    @GET
    @Path("get/receivedProposals")
    @Produces(MediaType.APPLICATION_JSON)
    fun getReceivedProposalsByParams(@DefaultValue("1") @QueryParam("page") page: Int,
                                     @DefaultValue("") @QueryParam("id") idProposal: String,
                                     @DefaultValue("") @QueryParam("issuer") issuer: String,
                                     @DefaultValue("1990-01-01") @QueryParam("from") from: String,
                                     @DefaultValue("2050-12-31") @QueryParam("to") to: String): Response {

        try {
            var myPage = page

            if (myPage < 1){
                myPage = 1
            }


            var criteria : QueryCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)

            val results = builder {

                val counterpartEqual = ProposalSchemaV1.PersistentProposal::counterpart.equal(myLegalName.toString())
                val firstCriteria = QueryCriteria.VaultCustomQueryCriteria(counterpartEqual)
                criteria = criteria.and(firstCriteria)

                if(idProposal.length > 0){
                    val customCriteria = QueryCriteria.LinearStateQueryCriteria( uuid = listOf(UUID.fromString(idProposal)))
                    criteria = criteria.and(customCriteria)
                }

                if(issuer.length > 0){
                    val idEqual = ProposalSchemaV1.PersistentProposal::issuer.equal(issuer)
                    val customCriteria = QueryCriteria.VaultCustomQueryCriteria(idEqual)
                    criteria = criteria.and(customCriteria)
                }

                if(from.length > 0 && to.length > 0){
                    val format = SimpleDateFormat("yyyy-MM-dd")
                    var myFrom = format.parse(from)
                    var myTo = format.parse(to)
                    var dateBetween = ProposalSchemaV1.PersistentProposal::data.between(myFrom, myTo)
                    val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateBetween)
                    criteria = criteria.and(customCriteria)
                }

                val results = rpcOps.vaultQueryBy<ProposalState>(
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
    fun createProposal(req : ProposalPojo): Response {

        try {
            val counterpart : Party = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(req.counterpart))!!
            val snam : Party = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse("O=Sman,L=Milan,C=IT"))!!

            val signedTx = rpcOps.startTrackedFlow(ProposalFlow::Starter,
                    counterpart,
                    snam,
                    req)
                    .returnValue.getOrThrow()

            val resp = ResponsePojo("SUCCESS","transaction "+signedTx.toString()+" committed to ledger.")
            return Response.status(CREATED).entity(resp).build()

        } catch (ex: Exception) {
            val msg = ex.message
            logger.error(ex.message, ex)
            val resp = ResponsePojo("ERROR", msg!!)
            return Response.status(BAD_REQUEST).entity(resp).build()
        }
    }

    @POST
    @Path("issueTransaction")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun issueTransaction(req : IssueTransactionPojo): Response {

        try {

            val signedTx = rpcOps.startTrackedFlow(TransactionFlow::Issuer,
                    req.id)
                    .returnValue.getOrThrow()

            val resp = ResponsePojo("SUCCESS","transaction "+signedTx.toString()+" committed to ledger.")
            return Response.status(CREATED).entity(resp).build()

        } catch (ex: Exception) {
            val msg = ex.message
            logger.error(ex.message, ex)
            val resp = ResponsePojo("ERROR", msg!!)
            return Response.status(BAD_REQUEST).entity(resp).build()
        }
    }
}
