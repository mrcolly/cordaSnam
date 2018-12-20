package com.snam.schema


import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object ProposalSchema

object ProposalSchemaV1 : MappedSchema(
        schemaFamily = ProposalSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentProposal::class.java)) {
    @Entity
    @Table(name = "proposal_states")
    class PersistentProposal(

            @Column(name = "issuer")
            var issuer: String,

            @Column(name = "counterpart")
            var counterpart: String,

            @Column(name = "snam")
            var snamName: String,

            @Column(name = "data")
            var data: Date,

            @Column(name = "energia")
            var energia: Double,

            @Column(name = "pricePerUnit")
            var pricePerUnit: Double,

            @Column(name = "validity")
            var validity: Date,

            @Column(name = "type")
            var type: Char,

            @Column(name = "linear_id")
            var linearId: UUID

    ) : PersistentState() {
        // Default constructor required by hibernate.
        //constructor(): this("", "", "", , UUID.randomUUID())
        constructor() : this("","","",  Date(), 0.0 , 0.0, Date(), '-', UUID.randomUUID())
    }
}
