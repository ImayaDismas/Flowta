package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowgroup.flowta.data.model.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query(
        "SELECT wallet_id, business_id, name, type, currency_code, opening_balance_minor, created_at, updated_at " +
            "FROM wallets WHERE business_id = :businessId ORDER BY created_at ASC"
    )
    fun observeForBusiness(businessId: String): Flow<List<WalletEntity>>

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
