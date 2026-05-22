package com.flowgroup.flowta.domain.repository

import com.flowgroup.flowta.domain.common.Result

interface PinRepository {
    suspend fun isSet(): Result<Boolean>
    suspend fun setPin(pin: CharArray): Result<Unit>
    suspend fun verifyPin(pin: CharArray): Result<Boolean>
    suspend fun clear(): Result<Unit>
}