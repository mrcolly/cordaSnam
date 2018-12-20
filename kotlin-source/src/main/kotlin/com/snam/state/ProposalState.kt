package com.snam.state


import com.snam.schema.ProposalSchemaV1
import com.snam.schema.TransactionSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*


data class ProposalState(
        val issuer: Party,
        val counterpart: Party,
        val snam: Party,
        val data: Date,
        val energia: Double,
        val pricePerUnit: Double,
        val validity: Date,
        val type: Char,
        override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState {

    override val participants: List<AbstractParty> get() = listOf(issuer, counterpart, snam)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ProposalSchemaV1 -> ProposalSchemaV1.PersistentProposal(
                    this.issuer.name.toString(),
                    this.counterpart.name.toString(),
                    this.snam.name.toString(),
                    this.data,
                    this.energia,
                    this.pricePerUnit,
                    this.validity,
                    this.type,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ProposalSchemaV1)
}
