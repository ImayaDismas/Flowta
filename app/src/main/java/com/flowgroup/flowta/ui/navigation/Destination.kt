package com.flowgroup.flowta.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable data object Splash : Destination
    @Serializable data object GetStarted : Destination
    @Serializable data object AddBusiness : Destination
    @Serializable data object SetPin : Destination
    @Serializable data object SetupComplete : Destination
    @Serializable data object PinUnlock : Destination
    @Serializable data object Home : Destination
    @Serializable data object AddWallet : Destination
    @Serializable data class WalletDetail(val walletId: String) : Destination
    @Serializable data class EditWallet(val walletId: String) : Destination
    @Serializable data object RecordTransaction : Destination
    @Serializable data class TransactionDetail(val transactionId: String) : Destination
    @Serializable data class EditTransaction(val transactionId: String) : Destination
    @Serializable data object DeniList : Destination
    @Serializable data object AddClient : Destination
    @Serializable data class ClientDeniDetail(val clientId: String) : Destination
    @Serializable data object Reconciliation : Destination
    @Serializable data object PasteSms : Destination
    @Serializable data class MatchReview(val paymentId: String) : Destination
    @Serializable data object ScanReceipt : Destination
    @Serializable data object ImportStatement : Destination
    @Serializable data object ScanInbox : Destination
    @Serializable data object Export : Destination
}
