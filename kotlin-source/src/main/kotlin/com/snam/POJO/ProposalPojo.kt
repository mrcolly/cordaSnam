package com.snam.POJO

import net.corda.core.serialization.CordaSerializable
import java.util.*


@CordaSerializable
data class ProposalPojo(
        val counterpart: String = "",
        val data: Date = Date(),
        val energia: Double = 0.0,
        val pricePerUnit: Double = 0.0,
        val validity: Date = addMinutesToDate(data, 15),
        val type: Char = '-'
)


fun addMinutesToDate( beforeTime: Date, minutes: Int): Date {
    val ONE_MINUTE_IN_MILLIS: Long = 60000//millisecs

    val curTimeInMs = beforeTime.time
    return Date(curTimeInMs + minutes * ONE_MINUTE_IN_MILLIS)
}