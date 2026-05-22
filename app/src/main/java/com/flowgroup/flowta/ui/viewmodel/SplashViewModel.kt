package com.flowgroup.flowta.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.usecase.business.IsOnboardingCompleteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isOnboardingComplete: IsOnboardingCompleteUseCase,
) : ViewModel() {

    private val _destination = Channel<SplashDestination>(Channel.BUFFERED)
    val destination: Flow<SplashDestination> = _destination.receiveAsFlow()

    init {
        viewModelScope.launch {
            val complete = (isOnboardingComplete() as? Result.Success)?.data ?: false
            _destination.send(if (complete) SplashDestination.Home else SplashDestination.Onboarding)
        }
    }

    enum class SplashDestination { Onboarding, Home }
}