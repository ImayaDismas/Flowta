package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowgroup.flowta.data.model.entity.DeniEntryEntity
import com.flowgroup.flowta.data.model.entity.projection.DeniEntryWithClientProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface DeniEntryDao {
    @Query(
        "SELECT deni_entry_id, business_id, customer_id, type, amount_minor, currency_code, " +
            "note, wallet_id, occurred_at, created_at, updated_at " +
            "FROM deni_entries WHERE customer_id = :clientId " +
            "ORDER BY occurred_at DESC, created_at DESC"
    )
    fun observeForClient(clientId: String): Flow<List<DeniEntryEntity>>

    @Query(
        "SELECT de.*, c.name AS client_name " +
            "FROM deni_entries de " +
            "INNER JOIN customers c ON de.customer_id = c.customer_id " +
            "WHERE de.wallet_id = :walletId " +
            "ORDER BY de.occurred_at DESC, de.created_at DESC"
    )
    fun observeForWallet(walletId: String): Flow<List<DeniEntryWithClientProjection>>

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount_minor " +
            "WHEN type = 'PAYMENT' THEN -amount_minor ELSE 0 END), 0) " +
            "FROM deni_entries WHERE business_id = :businessId"
    )
    fun observeTotalOutstandingForBusiness(businessId: String): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DeniEntryEntity)

    @Query("DELETE FROM deni_entries WHERE deni_entry_id = :id")
    suspend fun deleteById(id: String)
}
