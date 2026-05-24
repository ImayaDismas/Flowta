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
            "COALESCE((SELECT SUM(CASE WHEN t.type = 'SALE' THEN t.amount_minor " +
            "WHEN t.type = 'EXPENSE' THEN -t.amount_minor ELSE 0 END) " +
            "FROM transactions t WHERE t.wallet_id = w.wallet_id), 0) + " +
            "COALESCE((SELECT SUM(CASE WHEN d.type = 'PAYMENT' THEN d.amount_minor " +
            "WHEN d.type = 'CREDIT' THEN -d.amount_minor ELSE 0 END) " +
            "FROM deni_entries d WHERE d.wallet_id = w.wallet_id), 0) AS net_minor " +
            "FROM wallets w " +
            "WHERE w.business_id = :businessId " +
            "ORDER BY w.created_at ASC"
    )
    fun observeWithBalanceForBusiness(businessId: String): Flow<List<WalletWithBalanceProjection>>

    @Query(
        "SELECT wallet_id, business_id, name, type, currency_code, opening_balance_minor, created_at, updated_at " +
            "FROM wallets WHERE wallet_id = :id LIMIT 1"
    )
    suspend fun getById(id: String): WalletEntity?

    @Query(
        "SELECT wallet_id, business_id, name, type, currency_code, opening_balance_minor, created_at, updated_at " +
            "FROM wallets WHERE wallet_id = :id LIMIT 1"
    )
    fun observeById(id: String): Flow<WalletEntity?>

    @Query(
        "SELECT w.*, " +
            "COALESCE((SELECT SUM(CASE WHEN t.type = 'SALE' THEN t.amount_minor " +
            "WHEN t.type = 'EXPENSE' THEN -t.amount_minor ELSE 0 END) " +
            "FROM transactions t WHERE t.wallet_id = w.wallet_id), 0) + " +
            "COALESCE((SELECT SUM(CASE WHEN d.type = 'PAYMENT' THEN d.amount_minor " +
            "WHEN d.type = 'CREDIT' THEN -d.amount_minor ELSE 0 END) " +
            "FROM deni_entries d WHERE d.wallet_id = w.wallet_id), 0) AS net_minor " +
            "FROM wallets w " +
            "WHERE w.wallet_id = :id"
    )
    fun observeWithBalanceById(id: String): Flow<WalletWithBalanceProjection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WalletEntity)

    @Query(
        "UPDATE wallets SET name = :name, type = :type, updated_at = :updatedAtEpochMillis " +
            "WHERE wallet_id = :id"
    )
    suspend fun updateNameAndType(
        id: String,
        name: String,
        type: com.flowgroup.flowta.domain.model.WalletType,
        updatedAtEpochMillis: Long,
    )

    @Query("DELETE FROM wallets WHERE wallet_id = :id")
    suspend fun deleteById(id: String)
}
