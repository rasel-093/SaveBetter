package com.example.savebetter.feature.monthly.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.AlertBanner
import com.example.savebetter.core.designsystem.component.AlertBannerType
import com.example.savebetter.core.designsystem.component.AppTopBar
import com.example.savebetter.core.designsystem.component.ComparisonBars
import com.example.savebetter.core.designsystem.component.DonutChart
import com.example.savebetter.core.designsystem.component.LedgerCard
import com.example.savebetter.core.designsystem.component.ProgressTrack
import com.example.savebetter.core.designsystem.component.SectionLabel
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.MonthlySuggestionType
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.feature.monthly.MonthlyAnalysisUiState
import com.example.savebetter.feature.monthly.MonthlyAnalysisViewModel

/**
 * Screen 06: Monthly Analysis & Detail (মাসিক বিস্তারিত ও গ্রাফ).
 *
 * Replicates Screen 06 from expense-tracker-ui-design-savebetter.html:
 * - Budget and savings goal progress
 * - Category spending distribution DonutChart with percentage legend
 * - Outlier-resistant reduction suggestion engine
 * - Month-over-month comparison bars
 */
@Composable
fun MonthlyAnalysisScreen(
    userId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    viewModel: MonthlyAnalysisViewModel = hiltViewModel()
) {
    LaunchedEffect(userId, selectedLanguage) {
        viewModel.initForUser(userId, selectedLanguage)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = uiState.monthTitleText.ifBlank { stringResource(R.string.monthly_analysis_title) },
                subtitle = stringResource(R.string.monthly_tag_subtitle),
                onBackClick = onBackClick,
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = viewModel::navigatePreviousMonth,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.monthly_prev_month),
                                tint = SaveBetterTheme.colors.inkSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = viewModel::navigateNextMonth,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.monthly_next_month),
                                tint = SaveBetterTheme.colors.inkSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
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
            MonthlyAnalysisContent(
                uiState = uiState,
                selectedLanguage = selectedLanguage,
                innerPadding = innerPadding
            )
        }
    }
}

@Composable
private fun MonthlyAnalysisContent(
    uiState: MonthlyAnalysisUiState,
    selectedLanguage: AppLanguage,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Monthly Target & Savings Card
        LedgerCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.label_budget)}: ${CurrencyFormatter.formatMinor(uiState.targetAmountMinor, selectedLanguage)}",
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted
                )
                val spentColor = when {
                    uiState.isOverBudget -> SaveBetterTheme.colors.brick
                    uiState.isWarning -> SaveBetterTheme.colors.gold
                    else -> SaveBetterTheme.colors.ink
                }
                Text(
                    text = CurrencyFormatter.formatMinor(uiState.spentAmountMinor, selectedLanguage),
                    style = SaveBetterTheme.typography.amountMedium,
                    color = spentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            ProgressTrack(
                progress = uiState.budgetProgress,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.dashboard_savings_goal)}: ${CurrencyFormatter.formatMinor(uiState.savingGoalMinor, selectedLanguage)}",
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted
                )
                val isGoalPossible = uiState.spentAmountMinor <= uiState.targetAmountMinor
                Text(
                    text = if (isGoalPossible) {
                        CurrencyFormatter.formatMinor(uiState.savingGoalMinor, selectedLanguage)
                    } else {
                        CurrencyFormatter.formatMinor(0L, selectedLanguage)
                    },
                    style = SaveBetterTheme.typography.amountMedium,
                    color = if (isGoalPossible) SaveBetterTheme.colors.moss else SaveBetterTheme.colors.brick,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Category Spending Donut Chart Section
        SectionLabel(text = stringResource(R.string.monthly_spending_by_category))

        LedgerCard {
            if (uiState.categorySlices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.monthly_no_data),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted
                    )
                }
            } else {
                DonutChart(
                    slices = uiState.categorySlices,
                    chartSize = 114.dp,
                    strokeWidth = 18.dp,
                    showLegend = true
                )
            }
        }

        // 3. Intelligent Reduction Suggestions & Outlier Explanations
        if (uiState.suggestions.isNotEmpty()) {
            uiState.suggestions.forEach { suggestion ->
                val title = if (suggestion.titleArgs.isNotEmpty()) {
                    stringResource(suggestion.titleResId, *suggestion.titleArgs.toTypedArray())
                } else {
                    stringResource(suggestion.titleResId)
                }

                val message = if (suggestion.messageArgs.isNotEmpty()) {
                    stringResource(suggestion.messageResId, *suggestion.messageArgs.toTypedArray())
                } else {
                    stringResource(suggestion.messageResId)
                }

                val bannerType = when (suggestion.type) {
                    MonthlySuggestionType.OUTLIER_EXCLUDED -> AlertBannerType.Info
                    MonthlySuggestionType.REDUCTION_SUGGESTION -> AlertBannerType.Warning
                    MonthlySuggestionType.SAVINGS_FEASIBLE -> AlertBannerType.Success
                }

                AlertBanner(
                    title = title,
                    message = message,
                    type = bannerType
                )
            }
        }

        // 4. Month-over-Month Comparison
        if (uiState.comparisonCurrent != null && uiState.comparisonPrevious != null) {
            SectionLabel(text = stringResource(R.string.monthly_compared_to_last_month))

            LedgerCard {
                ComparisonBars(
                    primaryItem = uiState.comparisonPrevious,
                    secondaryItem = uiState.comparisonCurrent
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
