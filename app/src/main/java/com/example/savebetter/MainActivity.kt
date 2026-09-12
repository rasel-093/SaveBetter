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

        // Schedule WorkManager month-end reconciliation reminder
        com.example.savebetter.core.notification.ReconciliationReminderScheduler.scheduleMonthEndReminder(applicationContext)

        setContent {
            val languageViewModel: LanguageViewModel = hiltViewModel()
            val settingsViewModel: com.example.savebetter.feature.settings.SettingsViewModel = hiltViewModel()
            val currentLanguage by languageViewModel.currentLanguage.collectAsStateWithLifecycle()
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val isDarkTheme = when (settingsUiState.themeMode) {
                com.example.savebetter.core.domain.model.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                com.example.savebetter.core.domain.model.ThemeMode.LIGHT -> false
                com.example.savebetter.core.domain.model.ThemeMode.DARK -> true
            }

            SaveBetterTheme(
                darkTheme = isDarkTheme,
                language = currentLanguage
            ) {
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
                        is AuthGateState.NeedsOnboarding -> {
                            com.example.savebetter.feature.onboarding.ui.OnboardingScreen(
                                userId = state.user.id,
                                userName = state.user.displayName,
                                userEmail = state.user.email,
                                preferredLanguage = currentLanguage.code,
                                onOnboardingCompleted = {
                                    // Room write automatically updates AuthGateViewModel to Authenticated
                                }
                            )
                        }
                        is AuthGateState.Authenticated -> {
                            var isAddExpenseOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var activeExpenseIdForEdit by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
                            var showWeeklyDetail by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var showMonthlyAnalysis by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var showReconciliation by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var showDebts by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var showSettings by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                if (intent?.getStringExtra("navigate_to") == "reconciliation") {
                                    showReconciliation = true
                                }
                            }

                            if (isAddExpenseOpen) {
                                com.example.savebetter.feature.addexpense.ui.AddExpenseScreen(
                                    userId = state.user.id,
                                    editExpenseId = activeExpenseIdForEdit,
                                    selectedLanguage = currentLanguage,
                                    onDismiss = {
                                        isAddExpenseOpen = false
                                        activeExpenseIdForEdit = null
                                    }
                                )
                            } else if (showWeeklyDetail) {
                                com.example.savebetter.feature.weekly.ui.WeeklyDetailScreen(
                                    userId = state.user.id,
                                    selectedLanguage = currentLanguage,
                                    onBackClick = { showWeeklyDetail = false },
                                    onExpenseClick = { expenseId ->
                                        activeExpenseIdForEdit = expenseId
                                        isAddExpenseOpen = true
                                    }
                                )
                            } else if (showMonthlyAnalysis) {
                                com.example.savebetter.feature.monthly.ui.MonthlyAnalysisScreen(
                                    userId = state.user.id,
                                    selectedLanguage = currentLanguage,
                                    onBackClick = { showMonthlyAnalysis = false },
                                    onReconcileClick = {
                                        showMonthlyAnalysis = false
                                        showReconciliation = true
                                    }
                                )
                            } else if (showReconciliation) {
                                com.example.savebetter.feature.reconciliation.ui.ReconciliationScreen(
                                    onNavigateBack = { showReconciliation = false },
                                    onOpenAddExpenseWithAmount = {
                                        showReconciliation = false
                                        activeExpenseIdForEdit = null
                                        isAddExpenseOpen = true
                                    }
                                )
                            } else if (showDebts) {
                                com.example.savebetter.feature.debts.ui.DebtsScreen(
                                    userId = state.user.id,
                                    selectedLanguage = currentLanguage,
                                    onBackClick = { showDebts = false },
                                    onDestinationSelected = { dest ->
                                        when (dest) {
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Home -> {
                                                showDebts = false
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Weekly -> {
                                                showDebts = false
                                                showWeeklyDetail = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Monthly -> {
                                                showDebts = false
                                                showMonthlyAnalysis = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Debts -> {
                                                // Already here
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Settings -> {
                                                showDebts = false
                                                showSettings = true
                                            }
                                        }
                                    }
                                )
                            } else if (showSettings) {
                                com.example.savebetter.feature.settings.ui.SettingsScreen(
                                    userId = state.user.id,
                                    selectedLanguage = currentLanguage,
                                    onBackClick = { showSettings = false },
                                    onDestinationSelected = { dest ->
                                        when (dest) {
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Home -> {
                                                showSettings = false
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Weekly -> {
                                                showSettings = false
                                                showWeeklyDetail = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Monthly -> {
                                                showSettings = false
                                                showMonthlyAnalysis = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Debts -> {
                                                showSettings = false
                                                showDebts = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Settings -> {
                                                // Already here
                                            }
                                        }
                                    },
                                    onDeleteAccountClick = {
                                        settingsViewModel.showDeleteAccountConfirm(true)
                                    }
                                )

                            } else {
                                com.example.savebetter.feature.home.ui.HomeScreen(
                                    userId = state.user.id,
                                    userName = state.user.displayName,
                                    selectedLanguage = currentLanguage,
                                    onAddExpenseClick = {
                                        activeExpenseIdForEdit = null
                                        isAddExpenseOpen = true
                                    },
                                    onExpenseClick = { expenseId ->
                                        activeExpenseIdForEdit = expenseId
                                        isAddExpenseOpen = true
                                    },
                                    onWeeklyDetailClick = {
                                        showWeeklyDetail = true
                                    },
                                    onMonthlyAnalysisClick = {
                                        showMonthlyAnalysis = true
                                    },
                                    onReconciliationClick = {
                                        showReconciliation = true
                                    },
                                    onDestinationSelected = { dest ->
                                        when (dest) {
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Home -> {
                                                // Already here
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Weekly -> {
                                                showWeeklyDetail = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Monthly -> {
                                                showMonthlyAnalysis = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Debts -> {
                                                showDebts = true
                                            }
                                            com.example.savebetter.core.designsystem.component.BottomNavDestination.Settings -> {
                                                showSettings = true
                                            }
                                        }
                                    }
                                )
                            }
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
