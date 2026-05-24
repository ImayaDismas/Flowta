package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flowgroup.flowta.data.model.entity.ReceivedPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivedPaymentDao {
    @Query(
        "SELECT received_payment_id, business_id, provider, amount_minor, currency_code, " +
            "reference, sender_name, sender_phone, direction, status, matched_transaction_id, " +
            "source, occurred_at, created_at, updated_at " +
            "FROM received_payments WHERE business_id = :businessId " +
            "ORDER BY occurred_at DESC, created_at DESC"
    )
    fun observeForBusiness(businessId: String): Flow<List<ReceivedPaymentEntity>>

    @Query(
        "SELECT received_payment_id, business_id, provider, amount_minor, currency_code, " +
            "reference, sender_name, sender_phone, direction, status, matched_transaction_id, " +
            "source, occurred_at, created_at, updated_at " +
            "FROM received_payments WHERE received_payment_id = :id LIMIT 1"
    )
    suspend fun getById(id: String): ReceivedPaymentEntity?

    /** Idempotent insert: a row whose (business_id, provider, reference) already exists is ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringDuplicates(entities: List<ReceivedPaymentEntity>): List<Long>

    @Update
    suspend fun update(entity: ReceivedPaymentEntity)

    @Query("DELETE FROM received_payments WHERE received_payment_id = :id")
    suspend fun deleteById(id: String)
}
