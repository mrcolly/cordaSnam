package com.snam.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.bouncycastle.asn1.dvcs.Data
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object TransactionSchema


object TransactionSchemaV1 : MappedSchema(
        schemaFamily = TransactionSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentTransaction::class.java)) {
    @Entity
    @Table(name = "transaction_states")
    class PersistentTransaction(

            @Column(name = "buyer")
            var buyerName: String,

            @Column(name = "seller")
            var sellerName: String,

            @Column(name = "snam")
            var snamName: String,

            @Column(name = "codTransazione")
            var codTransazione: String,

            @Column(name = "codBuyer")
            var codBuyer: String,

            @Column(name = "codSeller")
            var codSeller: String,

            @Column(name = "data")
            var data: Date,

            @Column(name = "energia")
            var energia: Double,

            @Column(name = "pricePerUnit")
            var pricePerUnit: Double,

            @Column(name = "idVendita")
            var idVendita: String,

            @Column(name = "idAcquisto")
            var idAcquisto: String,

            @Column(name = "linear_id")
            var linearId: UUID

    ) : PersistentState() {
        // Default constructor required by hibernate.
        //constructor(): this("", "", "", , UUID.randomUUID())
        constructor() : this("","","","", "", "", Date(), 0.0 , 0.0, "", "", UUID.randomUUID())
    }
}