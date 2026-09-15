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
import com.example.savebetter.core.sync.SyncManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single Activity host for the SaveBetter application.
 *
 * Hosts the root [AuthGateViewModel] to manage session restoration
 * and conditionally routes between the Auth flow and authenticated screens.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule periodic background sync (15 min, network connected)
        syncManager.schedulePeriodicSync()

        // Schedule WorkManager month-end reconciliation reminder
        com.example.savebetter.core.notification.ReconciliationReminderScheduler.scheduleMonthEndReminder(applicationContext)
        // Schedule WorkManager weekly and monthly budget setup reminders
        com.example.savebetter.core.notification.BudgetReminderScheduler.scheduleWeeklyBudgetReminder(applicationContext)
        com.example.savebetter.core.notification.BudgetReminderScheduler.scheduleMonthlyBudgetReminder(applicationContext)

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
                            var showReconciliation by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var openBudgetDialogForWeekly by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var openBudgetDialogForMonthly by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                            var mainDestination by androidx.compose.runtime.remember {
                                androidx.compose.runtime.mutableStateOf(
                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Home
                                )
                            }

                            val onMainTabSelected: (com.example.savebetter.core.designsystem.component.BottomNavDestination) -> Unit =
                                { mainDestination = it }

                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                when (intent?.getStringExtra("navigate_to")) {
                                    "reconciliation" -> showReconciliation = true
                                    "weekly" -> {
                                        mainDestination = com.example.savebetter.core.designsystem.component.BottomNavDestination.Weekly
                                        if (intent?.getBooleanExtra("open_budget_dialog", false) == true) {
                                            openBudgetDialogForWeekly = true
                                        }
                                    }
                                    "monthly" -> {
                                        mainDestination = com.example.savebetter.core.designsystem.component.BottomNavDestination.Monthly
                                        if (intent?.getBooleanExtra("open_budget_dialog", false) == true) {
                                            openBudgetDialogForMonthly = true
                                        }
                                    }
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
                            } else if (showReconciliation) {
                                com.example.savebetter.feature.reconciliation.ui.ReconciliationScreen(
                                    onNavigateBack = { showReconciliation = false },
                                    onOpenAddExpenseWithAmount = {
                                        showReconciliation = false
                                        activeExpenseIdForEdit = null
                                        isAddExpenseOpen = true
                                    }
                                )
                            } else {
                                when (mainDestination) {
                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Home -> {
                                        com.example.savebetter.feature.home.ui.HomeScreen(
                                            userId = state.user.id,
                                            userName = state.user.displayName,
                                            selectedLanguage = currentLanguage,
                                            selectedBottomNavDestination = mainDestination,
                                            onAddExpenseClick = {
                                                activeExpenseIdForEdit = null
                                                isAddExpenseOpen = true
                                            },
                                            onExpenseClick = { expenseId ->
                                                activeExpenseIdForEdit = expenseId
                                                isAddExpenseOpen = true
                                            },
                                            onWeeklyDetailClick = {
                                                mainDestination =
                                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Weekly
                                            },
                                            onMonthlyAnalysisClick = {
                                                mainDestination =
                                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Monthly
                                            },
                                            onReconciliationClick = {
                                                showReconciliation = true
                                            },
                                            onDestinationSelected = onMainTabSelected
                                        )
                                    }
                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Weekly -> {
                                        com.example.savebetter.feature.weekly.ui.WeeklyDetailScreen(
                                            userId = state.user.id,
                                            selectedLanguage = currentLanguage,
                                            initialOpenBudgetDialog = openBudgetDialogForWeekly,
                                            onExpenseClick = { expenseId ->
                                                activeExpenseIdForEdit = expenseId
                                                isAddExpenseOpen = true
                                            },
                                            onDestinationSelected = onMainTabSelected
                                        )
                                    }
                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Monthly -> {
                                        com.example.savebetter.feature.monthly.ui.MonthlyAnalysisScreen(
                                            userId = state.user.id,
                                            selectedLanguage = currentLanguage,
                                            initialOpenBudgetDialog = openBudgetDialogForMonthly,
                                            onReconcileClick = { showReconciliation = true },
                                            onDestinationSelected = onMainTabSelected
                                        )
                                    }
                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Debts -> {
                                        com.example.savebetter.feature.debts.ui.DebtsScreen(
                                            userId = state.user.id,
                                            selectedLanguage = currentLanguage,
                                            onDestinationSelected = onMainTabSelected
                                        )
                                    }
                                    com.example.savebetter.core.designsystem.component.BottomNavDestination.Settings -> {
                                        com.example.savebetter.feature.settings.ui.SettingsScreen(
                                            userId = state.user.id,
                                            selectedLanguage = currentLanguage,
                                            onDestinationSelected = onMainTabSelected,
                                            onDeleteAccountClick = {
                                                settingsViewModel.showDeleteAccountConfirm(true)
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
    }

    override fun onStart() {
        super.onStart()
        // Trigger immediate background sync on app return to foreground
        syncManager.requestImmediateSync()
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
