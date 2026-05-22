package com.flowgroup.flowta.domain.usecase.pin

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.repository.PinRepository
import javax.inject.Inject

class SetPinUseCase @Inject constructor(
    private val pinRepository: PinRepository,
) {
    suspend operator fun invoke(pin: CharArray): Result<Unit> = pinRepository.setPin(pin)
}