package com.flowgroup.flowta.data.model.mapper

import com.flowgroup.flowta.data.model.entity.BusinessEntity
import com.flowgroup.flowta.domain.model.Business

fun BusinessEntity.toDomain(): Business = Business(
    id = businessId,
    name = name,
    currency = currencyCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Business.toEntity(): BusinessEntity = BusinessEntity(
    businessId = id,
    name = name,
    currencyCode = currency,
    createdAt = createdAt,
    updatedAt = updatedAt,
)