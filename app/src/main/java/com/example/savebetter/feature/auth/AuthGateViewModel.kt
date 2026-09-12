package com.example.savebetter.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.savebetter.core.domain.usecase.profile.GetUserProfileUseCase
import com.example.savebetter.core.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Root gate state representing session and onboarding status.
 */
sealed interface AuthGateState {
    /**
     * Initial startup state while verifying persistent credentials.
     */
    data object Loading : AuthGateState

    /**
     * No active authenticated session; show authentication flow.
     */
    data object Unauthenticated : AuthGateState

    /**
     * User is authenticated but has not yet completed initial financial onboarding.
     */
    data class NeedsOnboarding(val user: AuthUser) : AuthGateState

    /**
     * User is authenticated and onboarding is completed; proceed to home dashboard.
     */
    data class Authenticated(val user: AuthUser, val profile: UserProfile) : AuthGateState
}

/**
 * Root ViewModel that acts as the application's authentication and onboarding gate.
 *
 * Flow:
 * Startup -> Loading -> Check Firebase Session
 * If Unauthenticated -> Login / Signup
 * If Authenticated -> Observe local UserProfile
 *   If profile == null || !profile.onboardingCompleted -> NeedsOnboarding (Screen 02)
 *   If profile != null && profile.onboardingCompleted -> Authenticated (Home)
 */
@HiltViewModel
class AuthGateViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    getUserProfileUseCase: GetUserProfileUseCase,
    private val syncRepository: SyncRepository? = null
) : ViewModel() {

    init {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { user ->
                if (user != null && syncRepository != null) {
                    val localProfile = getUserProfileUseCase(user.id).first()
                    if (localProfile == null) {
                        // Multi-device session restoration: hydrate Room from remote
                        runCatching { syncRepository.pullRemote(user.id) }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val gateState: StateFlow<AuthGateState> = observeAuthStateUseCase()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(AuthGateState.Unauthenticated)
            } else {
                getUserProfileUseCase(user.id).map { profile ->
                    if (profile == null || !profile.onboardingCompleted) {
                        AuthGateState.NeedsOnboarding(user)
                    } else {
                        AuthGateState.Authenticated(user, profile)
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthGateState.Loading
        )
}
