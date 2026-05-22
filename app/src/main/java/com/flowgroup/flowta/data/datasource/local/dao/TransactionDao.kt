package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowgroup.flowta.data.model.entity.TransactionEntity
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE transaction_id = :id")
    suspend fun deleteById(id: String)
}
