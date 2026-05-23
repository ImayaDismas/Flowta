package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowgroup.flowta.data.model.entity.TransactionEntity
import com.flowgroup.flowta.data.model.entity.projection.TransactionTypeTotalProjection
import com.flowgroup.flowta.data.model.entity.projection.TransactionWithWalletProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        "SELECT t.transaction_id, t.business_id, t.wallet_id, t.type, t.amount_minor, " +
            "t.currency_code, t.note, t.occurred_at, t.created_at, t.updated_at, " +
            "w.name AS wallet_name, w.type AS wallet_type " +
            "FROM transactions t INNER JOIN wallets w ON w.wallet_id = t.wallet_id " +
            "WHERE t.business_id = :businessId " +
            "ORDER BY t.occurred_at DESC, t.created_at DESC"
    )
    fun observeHistoryForBusiness(businessId: String): Flow<List<TransactionWithWalletProjection>>

    @Query(
        "SELECT type, COALESCE(SUM(amount_minor), 0) AS total_minor " +
            "FROM transactions " +
            "WHERE business_id = :businessId " +
            "AND occurred_at >= :startEpochMillis " +
            "AND occurred_at < :endExclusiveEpochMillis " +
            "GROUP BY type"
    )
    fun observeTotalsBetween(
        businessId: String,
        startEpochMillis: Long,
        endExclusiveEpochMillis: Long,
    ): Flow<List<TransactionTypeTotalProjection>>

    @Query(
        "SELECT t.transaction_id, t.business_id, t.wallet_id, t.type, t.amount_minor, " +
            "t.currency_code, t.note, t.occurred_at, t.created_at, t.updated_at, " +
            "w.name AS wallet_name, w.type AS wallet_type " +
            "FROM transactions t INNER JOIN wallets w ON w.wallet_id = t.wallet_id " +
            "WHERE t.wallet_id = :walletId " +
            "ORDER BY t.occurred_at DESC, t.created_at DESC " +
            "LIMIT :limit"
    )
    fun observeRecentForWallet(
        walletId: String,
        limit: Int,
    ): Flow<List<TransactionWithWalletProjection>>

    @Query(
        "SELECT t.transaction_id, t.business_id, t.wallet_id, t.type, t.amount_minor, " +
            "t.currency_code, t.note, t.occurred_at, t.created_at, t.updated_at, " +
            "w.name AS wallet_name, w.type AS wallet_type " +
            "FROM transactions t INNER JOIN wallets w ON w.wallet_id = t.wallet_id " +
            "WHERE t.transaction_id = :id LIMIT 1"
    )
    fun observeByIdWithWallet(id: String): Flow<TransactionWithWalletProjection?>

    @Query(
        "SELECT transaction_id, business_id, wallet_id, type, amount_minor, currency_code, " +
            "note, occurred_at, created_at, updated_at " +
            "FROM transactions WHERE transaction_id = :id LIMIT 1"
    )
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE wallet_id = :walletId")
    fun observeCountForWallet(walletId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE wallet_id = :walletId")
    suspend fun countForWallet(walletId: String): Int

    @Query(
        "SELECT type, COALESCE(SUM(amount_minor), 0) AS total_minor " +
            "FROM transactions " +
            "WHERE wallet_id = :walletId " +
            "AND occurred_at >= :startEpochMillis " +
            "AND occurred_at < :endExclusiveEpochMillis " +
            "GROUP BY type"
    )
    fun observeWalletTotalsBetween(
        walletId: String,
        startEpochMillis: Long,
        endExclusiveEpochMillis: Long,
    ): Flow<List<TransactionTypeTotalProjection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE transaction_id = :id")
    suspend fun deleteById(id: String)
}
