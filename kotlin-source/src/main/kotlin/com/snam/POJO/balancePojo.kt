package com.snam.POJO

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class balancePojo(
        val totalSold: Double = 0.0,
        val totalBought: Double = 0.0,
        val delta: Double = totalSold - totalBought
)