package com.snam.POJO

import net.corda.core.serialization.CordaSerializable
import java.util.*

@CordaSerializable
data class TransactionPojo(
        val buyer: String = "",
        val seller: String = "",
        val codTransazione: String = "",
        val codBuyer: String = "",
        val codSeller: String = "",
        val data: Date = Date(),
        val energia: Double = 0.0,
        val pricePerUnit: Double = 0.0,
        val idVendita: String = "",
        val idAcquisto: String = ""
        )