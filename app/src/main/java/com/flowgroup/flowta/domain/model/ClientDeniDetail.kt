package com.flowgroup.flowta.domain.model

data class ClientDeniDetail(
    val client: Client,
    val outstandingMinor: Long,
    val entries: List<DeniEntry>,
)
