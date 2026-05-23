package com.flowgroup.flowta.domain.usecase.wallet

import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.WeekRange
import com.flowgroup.flowta.domain.model.WalletDetail
import com.flowgroup.flowta.domain.repository.TransactionRepository
import com.flowgroup.flowta.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import javax.inject.Inject

class ObserveWalletDetailUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val clock: Clock,
) {
    operator fun invoke(walletId: String): Flow<Result<WalletDetail?>> {
        val zone = TimeZone.currentSystemDefault()
        val week = WeekRange.thisWeek(clock.now(), zone)
        return combine(
            walletRepository.observeWithBalanceById(walletId),
            transactionRepository.observeRecentForWallet(walletId, RECENT_LIMIT),
            transactionRepository.observeWalletTotalsBetween(walletId, week.start, week.endExclusive),
        ) { walletResult, recentResult, totalsResult ->
            if (walletResult is Result.Error) return@combine walletResult
            if (recentResult is Result.Error) return@combine recentResult
            if (totalsResult is Result.Error) return@combine totalsResult

            val walletWithBalance = (walletResult as Result.Success).data
            val recent = (recentResult as Result.Success).data
            val totals = (totalsResult as Result.Success).data

            if (walletWithBalance == null) {
                Result.Success(null)
            } else {
                Result.Success(
                    WalletDetail(
                        wallet = walletWithBalance.wallet,
                        currentBalanceMinor = walletWithBalance.currentBalanceMinor,
                        recentTransactions = recent,
                        weekTotals = totals,
                    )
                )
            }
        }
    }

    private companion object { const val RECENT_LIMIT = 7 }
}
