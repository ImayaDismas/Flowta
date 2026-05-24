package com.flowgroup.flowta.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowgroup.flowta.domain.model.CurrencyCode
import com.flowgroup.flowta.domain.model.MobileMoneyProvider
import com.flowgroup.flowta.domain.model.PaymentSource
import com.flowgroup.flowta.domain.model.ReconciliationStatus
import kotlinx.datetime.Instant

/**
 * The unique index on (business_id, provider, reference) makes re-pasting or re-scanning the same
 * payment idempotent — inserts use IGNORE so a duplicate transaction code is silently dropped.
 *
 * matched_transaction_id is a soft link (indexed, no FK) like deni's wallet_id: a payment may be
 * matched/unmatched freely and outlives any single sale.
 */
@Entity(
    tableName = "received_payments",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["business_id"],
            childColumns = ["business_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["business_id"], name = "index_received_payments_business_id"),
        Index(
            value = ["matched_transaction_id"],
            name = "index_received_payments_matched_transaction_id",
        ),
        Index(
            value = ["business_id", "provider", "reference"],
            unique = true,
            name = "index_received_payments_business_id_provider_reference",
        ),
    ],
)
data class ReceivedPaymentEntity(
    @PrimaryKey @ColumnInfo(name = "received_payment_id") val receivedPaymentId: String,
    @ColumnInfo(name = "business_id") val businessId: String,
    @ColumnInfo(name = "provider") val provider: MobileMoneyProvider,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: CurrencyCode,
    @ColumnInfo(name = "reference") val reference: String,
    @ColumnInfo(name = "sender_name") val senderName: String?,
    @ColumnInfo(name = "sender_phone") val senderPhone: String?,
    @ColumnInfo(name = "status") val status: ReconciliationStatus,
    @ColumnInfo(name = "matched_transaction_id") val matchedTransactionId: String?,
    @ColumnInfo(name = "source") val source: PaymentSource,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
