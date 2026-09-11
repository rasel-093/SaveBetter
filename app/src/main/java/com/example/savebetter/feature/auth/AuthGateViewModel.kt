package com.example.savebetter.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.domain.usecase.auth.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Root gate state representing whether an active authenticated session exists.
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
     * User is authenticated with a valid session.
     */
    data class Authenticated(val user: AuthUser) : AuthGateState
}

/**
 * Root ViewModel that acts as the application's authentication gate.
 *
 * Restores the persistent Firebase session automatically on startup.
 */
@HiltViewModel
class AuthGateViewModel @Inject constructor(
    observeAuthStateUseCase: ObserveAuthStateUseCase
) : ViewModel() {

    val gateState: StateFlow<AuthGateState> = observeAuthStateUseCase()
        .map { user ->
            if (user != null) {
                AuthGateState.Authenticated(user)
            } else {
                AuthGateState.Unauthenticated
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthGateState.Loading
        )
}
