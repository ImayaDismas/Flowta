package com.flowgroup.flowta.domain.model

/**
 * Which way money moved in a mobile-money message, relative to the user's wallet.
 *
 * [IN] — received (a sale / income). [OUT] — sent, paid to a till/paybill, withdrawn or
 * transferred to a bank (an expense). Reconciliation matches IN against [TransactionType.SALE]
 * and OUT against [TransactionType.EXPENSE].
 */
enum class PaymentDirection {
    IN,
    OUT,
}
