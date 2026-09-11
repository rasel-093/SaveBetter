package com.example.savebetter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savebetter.feature.settings.LanguageViewModel
import com.example.savebetter.feature.auth.AuthGateState
import com.example.savebetter.feature.auth.AuthGateViewModel
import com.example.savebetter.feature.auth.AuthViewModel
import com.example.savebetter.feature.auth.ui.AuthenticatedPlaceholderScreen
import com.example.savebetter.feature.auth.ui.SaveBetterColors
import com.example.savebetter.navigation.AuthNavHost
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity host for the SaveBetter application.
 *
 * Hosts the root [AuthGateViewModel] to manage session restoration
 * and conditionally routes between the Auth flow and authenticated screens.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val languageViewModel: LanguageViewModel = hiltViewModel()
            val currentLanguage by languageViewModel.currentLanguage.collectAsStateWithLifecycle()

            SaveBetterTheme(language = currentLanguage) {
                val authGateViewModel: AuthGateViewModel = hiltViewModel()
                val authViewModel: AuthViewModel = hiltViewModel()
                val gateState by authGateViewModel.gateState.collectAsStateWithLifecycle()
                var showDesignShowcase by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                if (showDesignShowcase) {
                    com.example.savebetter.core.designsystem.component.DesignSystemShowcase(
                        onBackClick = { showDesignShowcase = false },
                        selectedLanguage = currentLanguage,
                        onLanguageSelected = languageViewModel::onLanguageSelected
                    )
                } else {
                    when (val state = gateState) {
                        is AuthGateState.Loading -> {
                            AuthLoadingScreen()
                        }
                        is AuthGateState.Unauthenticated -> {
                            AuthNavHost(viewModel = authViewModel)
                        }
                        is AuthGateState.Authenticated -> {
                            AuthenticatedPlaceholderScreen(
                                user = state.user,
                                onSignOut = authViewModel::signOut,
                                selectedLanguage = currentLanguage,
                                onLanguageSelected = languageViewModel::onLanguageSelected,
                                onOpenShowcase = { showDesignShowcase = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Paper-themed loading splash screen displayed while restoring the auth session.
 */
@Composable
private fun AuthLoadingScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaveBetterColors.Paper),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SaveBetterColors.GoldTint)
                    .border(1.5.dp, SaveBetterColors.Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SB",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = SaveBetterColors.Ink
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = SaveBetterColors.Gold,
                strokeWidth = 3.dp
            )
        }
    }
}
