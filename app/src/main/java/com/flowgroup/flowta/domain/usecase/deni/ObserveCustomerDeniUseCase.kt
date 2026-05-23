package com.flowgroup.flowta.domain.usecase.deni

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.CustomerDeniDetail
import com.flowgroup.flowta.domain.repository.DeniRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveCustomerDeniUseCase @Inject constructor(
    private val deniRepository: DeniRepository,
) {
    operator fun invoke(customerId: String): Flow<Result<CustomerDeniDetail?>> =
        combine(
            deniRepository.observeCustomerWithBalance(customerId),
            deniRepository.observeEntriesForCustomer(customerId),
        ) { customerResult, entriesResult ->
            if (customerResult is Result.Error) return@combine customerResult
            if (entriesResult is Result.Error) return@combine entriesResult

            val customerDeni = (customerResult as Result.Success).data
            val entries = (entriesResult as Result.Success).data

            if (customerDeni == null) {
                Result.Success(null)
            } else {
                Result.Success(
                    CustomerDeniDetail(
                        customer = customerDeni.customer,
                        outstandingMinor = customerDeni.outstandingMinor,
                        entries = entries,
                    )
                )
            }
        }
}
