package com.snam.POJO

import net.corda.core.serialization.CordaSerializable
import java.util.*

@CordaSerializable
data class TransactionPojo(
        val buyer: String = "",
        val seller: String = "",
        val externalId: String = "",
        val data: Date = Date(),
        val energia: Double = 0.0,
        val pricePerUnit: Double = 0.0,
        val idProposal: String = ""
        )