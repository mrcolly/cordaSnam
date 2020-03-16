package com.snam.state

import com.snam.schema.TransactionSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.time.Instant
import java.util.*


data class TransactionState(
        val buyer: Party,
        val seller: Party,
        val snam: Party,
        val data: Instant,
        val energia: Double,
        val pricePerUnit: Double,
        val totalPrice: Double,
        val idProposal: String,
        override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState {

    override val participants: List<AbstractParty> get() = listOf(buyer, seller, snam)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is TransactionSchemaV1 -> TransactionSchemaV1.PersistentTransaction(
                    this.buyer.name.toString(),
                    this.seller.name.toString(),
                    this.snam.name.toString(),
                    this.data,
                    this.energia,
                    this.pricePerUnit,
                    this.totalPrice,
                    this.idProposal,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(TransactionSchemaV1)
}
