package com.flowgroup.flowta.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flowgroup.flowta.data.model.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query(
        "SELECT business_id, name, currency_code, created_at, updated_at " +
            "FROM businesses ORDER BY created_at ASC"
    )
    fun observeAll(): Flow<List<BusinessEntity>>

    @Query(
        "SELECT business_id, name, currency_code, created_at, updated_at " +
            "FROM businesses WHERE business_id = :id LIMIT 1"
    )
    fun observeById(id: String): Flow<BusinessEntity?>

    @Query(
        "SELECT business_id, name, currency_code, created_at, updated_at " +
            "FROM businesses WHERE business_id = :id LIMIT 1"
    )
    suspend fun getById(id: String): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BusinessEntity)

    @Query("DELETE FROM businesses WHERE business_id = :id")
    suspend fun deleteById(id: String)
}