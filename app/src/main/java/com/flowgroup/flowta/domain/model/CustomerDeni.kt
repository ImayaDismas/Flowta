package com.flowgroup.flowta.domain.model

/** A customer and how much they currently owe (credits minus payments), in minor units. */
data class CustomerDeni(
    val customer: Customer,
    val outstandingMinor: Long,
)
