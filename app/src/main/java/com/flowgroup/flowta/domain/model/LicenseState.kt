package com.flowgroup.flowta.domain.model

sealed class LicenseState {
    data class Trial(val daysRemaining: Int) : LicenseState()
    data object Active : LicenseState()
    data object Expired : LicenseState()
}
