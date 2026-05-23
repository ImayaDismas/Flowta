package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowgroup.flowta.data.model.entity.ClientEntity
import com.flowgroup.flowta.data.model.entity.projection.ClientWithBalanceProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query(
        "SELECT c.*, COALESCE(SUM(CASE WHEN d.type = 'CREDIT' THEN d.amount_minor " +
            "WHEN d.type = 'PAYMENT' THEN -d.amount_minor ELSE 0 END), 0) AS outstanding_minor " +
            "FROM customers c LEFT JOIN deni_entries d ON d.customer_id = c.customer_id " +
            "WHERE c.business_id = :businessId " +
            "GROUP BY c.customer_id " +
            "ORDER BY outstanding_minor DESC, c.name ASC"
    )
    fun observeWithBalanceForBusiness(businessId: String): Flow<List<ClientWithBalanceProjection>>

    @Query(
        "SELECT c.*, COALESCE(SUM(CASE WHEN d.type = 'CREDIT' THEN d.amount_minor " +
            "WHEN d.type = 'PAYMENT' THEN -d.amount_minor ELSE 0 END), 0) AS outstanding_minor " +
            "FROM customers c LEFT JOIN deni_entries d ON d.customer_id = c.customer_id " +
            "WHERE c.customer_id = :id " +
            "GROUP BY c.customer_id"
    )
    fun observeWithBalanceById(id: String): Flow<ClientWithBalanceProjection?>

    @Query(
        "SELECT customer_id, business_id, name, phone, currency_code, created_at, updated_at " +
            "FROM customers WHERE customer_id = :id LIMIT 1"
    )
    suspend fun getById(id: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ClientEntity)

    @Query("DELETE FROM customers WHERE customer_id = :id")
    suspend fun deleteById(id: String)
}
