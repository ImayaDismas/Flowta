package com.flowgroup.flowta.domain.model

/** A client and how much they currently owe (credits minus payments), in minor units. */
data class ClientDeni(
    val client: Client,
    val outstandingMinor: Long,
)
