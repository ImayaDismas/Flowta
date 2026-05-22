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
}
