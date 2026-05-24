package com.flowgroup.flowta.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.model.LicenseState
import com.flowgroup.flowta.domain.usecase.business.IsOnboardingCompleteUseCase
import com.flowgroup.flowta.domain.usecase.license.GetLicenseStateUseCase
import com.flowgroup.flowta.domain.usecase.license.InitLicenseTrialUseCase
import com.flowgroup.flowta.domain.usecase.pin.IsPinSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isOnboardingComplete: IsOnboardingCompleteUseCase,
    private val isPinSet: IsPinSetUseCase,
    private val getLicenseState: GetLicenseStateUseCase,
    private val initLicenseTrial: InitLicenseTrialUseCase,
) : ViewModel() {

    private val _destination = Channel<SplashDestination>(Channel.BUFFERED)
    val destination: Flow<SplashDestination> = _destination.receiveAsFlow()

    init {
        viewModelScope.launch {
            val onboarded = (isOnboardingComplete() as? Result.Success)?.data ?: false
            if (!onboarded) {
                _destination.send(SplashDestination.Onboarding)
                return@launch
            }

            initLicenseTrial()

            val licenseState = getLicenseState().first()
            if (licenseState is LicenseState.Expired) {
                _destination.send(SplashDestination.Paywall)
                return@launch
            }

            val pinSet = (isPinSet() as? Result.Success)?.data ?: false
            _destination.send(if (pinSet) SplashDestination.Unlock else SplashDestination.SetPin)
        }
    }

    enum class SplashDestination { Onboarding, SetPin, Unlock, Paywall }
}
