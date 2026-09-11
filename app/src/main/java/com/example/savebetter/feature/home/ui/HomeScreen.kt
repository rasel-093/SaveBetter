package com.example.savebetter.feature.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.AlertBanner
import com.example.savebetter.core.designsystem.component.AlertBannerType
import com.example.savebetter.core.designsystem.component.AppTopBar
import com.example.savebetter.core.designsystem.component.BottomNavBar
import com.example.savebetter.core.designsystem.component.BottomNavDestination
import com.example.savebetter.core.designsystem.component.ExpenseListRow
import com.example.savebetter.core.designsystem.component.LedgerCard
import com.example.savebetter.core.designsystem.component.ProgressTrack
import com.example.savebetter.core.designsystem.component.SectionLabel
import com.example.savebetter.core.designsystem.component.StatItem
import com.example.savebetter.core.designsystem.component.StatRow
import com.example.savebetter.core.designsystem.component.SyncStatusPill
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.feature.home.HomeUiState
import com.example.savebetter.feature.home.HomeViewModel

/**
 * Screen 03: Home Dashboard matching expense-tracker-ui-design-savebetter.html.
 */
@Composable
fun HomeScreen(
    userId: String,
    modifier: Modifier = Modifier,
    userName: String? = null,
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    onAddExpenseClick: () -> Unit = {},
    onExpenseClick: (String) -> Unit = {},
    onWeeklyDetailClick: () -> Unit = {},
    onMonthlyAnalysisClick: () -> Unit = {},
    onDestinationSelected: (BottomNavDestination) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.initForUser(userId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var selectedNavDestination by remember { mutableStateOf(BottomNavDestination.Home) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val greeting = stringResource(uiState.greetingResId)
            val displayName = userName ?: uiState.userProfile?.name
            val title = if (!displayName.isNullOrBlank()) "$greeting, $displayName" else greeting

            AppTopBar(
                title = title,
                subtitle = uiState.monthYearText,
                actions = {
                    SyncStatusPill(status = uiState.syncStatus)
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedDestination = selectedNavDestination,
                onDestinationSelected = { dest ->
                    selectedNavDestination = dest
                    when (dest) {
                        BottomNavDestination.Weekly -> onWeeklyDetailClick()
                        BottomNavDestination.Monthly -> onMonthlyAnalysisClick()
                        else -> onDestinationSelected(dest)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = SaveBetterTheme.colors.gold,
                contentColor = SaveBetterTheme.colors.cover,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.dashboard_fab_add_expense),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = SaveBetterTheme.colors.paper
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SaveBetterTheme.colors.gold)
            }
        } else {
            HomeContent(
                uiState = uiState,
                selectedLanguage = selectedLanguage,
                innerPadding = innerPadding,
                onExpenseClick = onExpenseClick,
                onWeeklyDetailClick = onWeeklyDetailClick,
                onMonthlyAnalysisClick = onMonthlyAnalysisClick
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    selectedLanguage: AppLanguage,
    innerPadding: PaddingValues,
    onExpenseClick: (String) -> Unit = {},
    onWeeklyDetailClick: () -> Unit = {},
    onMonthlyAnalysisClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Warning Banner (Rule: >= 80% warning, >= 100% over budget)
        AnimatedVisibility(visible = uiState.weeklySummary.isWarning || uiState.monthlySummary.isWarning) {
            val weekly = uiState.weeklySummary
            val monthly = uiState.monthlySummary
            val isOver = weekly.isOverBudget || monthly.isOverBudget
            val bannerType = if (isOver) AlertBannerType.Warning else AlertBannerType.Info
            val percentageStr = "${(weekly.percentage * 100).toInt()}%"

            val message = when {
                weekly.isOverBudget -> stringResource(R.string.dashboard_warning_weekly_over)
                weekly.isWarning -> stringResource(R.string.dashboard_warning_weekly_near, percentageStr)
                else -> stringResource(R.string.alert_budget_warning_msg)
            }

            AlertBanner(
                message = message,
                type = bannerType
            )
        }

        // 2. Weekly Summary Card
        LedgerCard {
            SectionLabel(
                text = stringResource(R.string.dashboard_weekly_title),
                actionText = stringResource(R.string.dashboard_see_all),
                onActionClick = onWeeklyDetailClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            // StatRow: Target, Spent, Remaining
            val remainingColor = if (uiState.weeklySummary.isOverBudget) {
                SaveBetterTheme.colors.brick
            } else {
                SaveBetterTheme.colors.moss
            }

            StatRow(
                items = listOf(
                    StatItem(
                        label = stringResource(R.string.label_budget),
                        value = CurrencyFormatter.formatMinor(uiState.weeklySummary.targetAmountMinor, selectedLanguage)
                    ),
                    StatItem(
                        label = stringResource(R.string.label_spent),
                        value = CurrencyFormatter.formatMinor(uiState.weeklySummary.spentAmountMinor, selectedLanguage)
                    ),
                    StatItem(
                        label = stringResource(R.string.label_remaining),
                        value = CurrencyFormatter.formatMinor(uiState.weeklySummary.remainingAmountMinor, selectedLanguage),
                        valueColor = remainingColor
                    )
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Track
            ProgressTrack(
                progress = uiState.weeklySummary.percentage,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = SaveBetterTheme.colors.paperLine, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-metrics: Daily Allowance and Today's Spend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_daily_limit),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted
                    )
                    Text(
                        text = CurrencyFormatter.formatMinor(uiState.weeklySummary.dailyLimitMinor, selectedLanguage),
                        style = SaveBetterTheme.typography.amountSmall,
                        color = SaveBetterTheme.colors.ink
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.dashboard_today_spent),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted
                    )
                    Text(
                        text = CurrencyFormatter.formatMinor(uiState.weeklySummary.dailySpentMinor, selectedLanguage),
                        style = SaveBetterTheme.typography.amountSmall,
                        color = SaveBetterTheme.colors.ink
                    )
                }
            }
        }

        // 3. Monthly Overview Card
        LedgerCard {
            SectionLabel(
                text = stringResource(R.string.dashboard_monthly_title),
                actionText = stringResource(R.string.dashboard_see_all),
                onActionClick = onMonthlyAnalysisClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Monthly StatRow: Budget, Spent, Remaining
            val monthlyRemainingColor = if (uiState.monthlySummary.isOverBudget) {
                SaveBetterTheme.colors.brick
            } else {
                SaveBetterTheme.colors.moss
            }

            StatRow(
                items = listOf(
                    StatItem(
                        label = stringResource(R.string.label_budget),
                        value = CurrencyFormatter.formatMinor(uiState.monthlySummary.targetAmountMinor, selectedLanguage)
                    ),
                    StatItem(
                        label = stringResource(R.string.label_spent),
                        value = CurrencyFormatter.formatMinor(uiState.monthlySummary.spentAmountMinor, selectedLanguage)
                    ),
                    StatItem(
                        label = stringResource(R.string.label_remaining),
                        value = CurrencyFormatter.formatMinor(uiState.monthlySummary.remainingAmountMinor, selectedLanguage),
                        valueColor = monthlyRemainingColor
                    )
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProgressTrack(
                progress = uiState.monthlySummary.percentage,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = SaveBetterTheme.colors.paperLine, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-metrics: Savings Goal, Cash Remaining in Hand, Projected Spend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_savings_goal),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted
                    )
                    Text(
                        text = CurrencyFormatter.formatMinor(uiState.monthlySummary.savingGoalMinor, selectedLanguage),
                        style = SaveBetterTheme.typography.amountSmall,
                        color = SaveBetterTheme.colors.moss,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.dashboard_cash_in_hand),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted
                    )
                    Text(
                        text = CurrencyFormatter.formatMinor(uiState.monthlySummary.handRemainingMinor, selectedLanguage),
                        style = SaveBetterTheme.typography.amountSmall,
                        color = SaveBetterTheme.colors.ink
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.dashboard_projected_spent),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted
                    )
                    Text(
                        text = CurrencyFormatter.formatMinor(uiState.monthlySummary.projectedSpentMinor, selectedLanguage),
                        style = SaveBetterTheme.typography.amountSmall,
                        color = SaveBetterTheme.colors.inkSoft
                    )
                }
            }
        }

        // 4. Recent Expenses Card
        LedgerCard {
            SectionLabel(
                text = stringResource(R.string.dashboard_recent_expenses),
                actionText = if (uiState.recentExpenses.isNotEmpty()) stringResource(R.string.dashboard_see_all) else null
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (uiState.recentExpenses.isEmpty()) {
                // Empty state card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SaveBetterTheme.colors.paper)
                            .border(1.dp, SaveBetterTheme.colors.paperLineStrong, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = SaveBetterTheme.colors.textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.dashboard_no_expenses),
                        style = SaveBetterTheme.typography.body,
                        color = SaveBetterTheme.colors.ink,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.dashboard_add_first_expense),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                uiState.recentExpenses.forEachIndexed { index, expense ->
                    ExpenseListRow(
                        title = expense.title,
                        subtitle = "${expense.categoryName} • ${expense.date}",
                        amount = CurrencyFormatter.formatMinor(expense.amountMinor, selectedLanguage),
                        badgeColor = expense.categoryColor,
                        showDivider = index < uiState.recentExpenses.lastIndex,
                        onClick = { onExpenseClick(expense.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
