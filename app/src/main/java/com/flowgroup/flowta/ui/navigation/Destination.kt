package com.flowgroup.flowta.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes. Each route is a @Serializable object (no args) or data class (args).
 * Stage 1 will extend this with: GetStarted, Registration, OtpVerification, Login,
 * the onboarding flow, and Home (Wallets/History/Insights tabs).
 */
sealed interface Destination {
    @Serializable
    data object Splash : Destination
}
