package com.flowgroup.flowta.domain.model

data class CustomerDeniDetail(
    val customer: Customer,
    val outstandingMinor: Long,
    val entries: List<DeniEntry>,
)
