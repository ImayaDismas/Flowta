package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowgroup.flowta.data.model.entity.WalletEntity
import com.flowgroup.flowta.data.model.entity.projection.WalletWithBalanceProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query(
        "SELECT wallet_id, business_id, name, type, currency_code, opening_balance_minor, created_at, updated_at " +
            "FROM wallets WHERE business_id = :businessId ORDER BY created_at ASC"
    )
    fun observeForBusiness(businessId: String): Flow<List<WalletEntity>>

    @Query(
        "SELECT w.*, " +
            "COALESCE(SUM(CASE WHEN t.type = 'SALE' THEN t.amount_minor " +
            "WHEN t.type = 'EXPENSE' THEN -t.amount_minor ELSE 0 END), 0) AS net_minor " +
            "FROM wallets w LEFT JOIN transactions t ON t.wallet_id = w.wallet_id " +
            "WHERE w.business_id = :businessId " +
            "GROUP BY w.wallet_id " +
            "ORDER BY w.created_at ASC"
    )
    fun observeWithBalanceForBusiness(businessId: String): Flow<List<WalletWithBalanceProjection>>

    @Query(
        "SELECT wallet_id, business_id, name, type, currency_code, opening_balance_minor, created_at, updated_at " +
            "FROM wallets WHERE wallet_id = :id LIMIT 1"
    )
    suspend fun getById(id: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WalletEntity)

    @Query("DELETE FROM wallets WHERE wallet_id = :id")
    suspend fun deleteById(id: String)
}
