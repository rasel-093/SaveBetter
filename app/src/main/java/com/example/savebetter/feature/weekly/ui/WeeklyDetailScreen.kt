package com.example.savebetter.feature.weekly.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.example.savebetter.core.designsystem.component.DailyTrendBarChart
import com.example.savebetter.core.designsystem.component.ExpenseListRow
import com.example.savebetter.core.designsystem.component.LedgerCard
import com.example.savebetter.core.designsystem.component.LedgerLabelValueRow
import com.example.savebetter.core.designsystem.component.ProgressTrack
import com.example.savebetter.core.designsystem.component.SectionLabel
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.feature.weekly.WeeklyDetailUiState
import com.example.savebetter.feature.weekly.WeeklyDetailViewModel

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType

/**
 * Screen 05: Weekly Detail (সাপ্তাহিক বিস্তারিত)
 *
 * Replicates Screen 05 from expense-tracker-ui-design-savebetter.html:
 * - Budget progress with target, spent, remaining
 * - In-screen weekly budget setup and editing dialog
 * - Pace warning banner
 * - Deterministic rule-based advice cards (dominant category, frequent small purchases, overshoot)
 * - 7-day trend bar chart with peak highlight
 * - Week's expenses list with navigation to edit expense
 */
@Composable
fun WeeklyDetailScreen(
    userId: String,
    modifier: Modifier = Modifier,
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    initialOpenBudgetDialog: Boolean = false,
    onExpenseClick: (String) -> Unit = {},
    onDestinationSelected: ((BottomNavDestination) -> Unit)? = null,
    viewModel: WeeklyDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(userId, selectedLanguage) {
        viewModel.initForUser(userId, selectedLanguage)
        if (initialOpenBudgetDialog) {
            viewModel.showBudgetDialog(true)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.weekly_detail_title),
                subtitle = uiState.weekDateRangeText,
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = viewModel::navigatePreviousWeek
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.weekly_prev_week),
                                tint = SaveBetterTheme.colors.inkSoft,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = viewModel::navigateNextWeek,
                            enabled = uiState.hasNextWeek
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.weekly_next_week),
                                tint = if (uiState.hasNextWeek) SaveBetterTheme.colors.inkSoft else SaveBetterTheme.colors.textMuted.copy(alpha = 0.38f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (onDestinationSelected != null) {
                BottomNavBar(
                    selectedDestination = BottomNavDestination.Weekly,
                    onDestinationSelected = onDestinationSelected
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
            WeeklyDetailContent(
                uiState = uiState,
                selectedLanguage = selectedLanguage,
                innerPadding = innerPadding,
                onExpenseClick = onExpenseClick,
                onEditBudgetClick = { viewModel.showBudgetDialog(true) }
            )
        }
    }

    if (uiState.showBudgetDialog) {
        WeeklyBudgetDialog(
            currentAmountMinor = uiState.weeklySummary.targetAmountMinor,
            weekRangeText = uiState.weekDateRangeText,
            onDismiss = { viewModel.showBudgetDialog(false) },
            onSave = { viewModel.saveWeeklyBudget(it) }
        )
    }
}

@Composable
private fun WeeklyDetailContent(
    uiState: WeeklyDetailUiState,
    selectedLanguage: AppLanguage,
    innerPadding: PaddingValues,
    onExpenseClick: (String) -> Unit,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val weekly = uiState.weeklySummary

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Weekly Target & Budget Card
        LedgerCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weekly_target_label),
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted
                )
                TextButton(
                    onClick = onEditBudgetClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = SaveBetterTheme.colors.gold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (weekly.targetAmountMinor > 0L) {
                            stringResource(R.string.weekly_edit_budget_action)
                        } else {
                            stringResource(R.string.weekly_set_budget_action)
                        },
                        style = SaveBetterTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = SaveBetterTheme.colors.gold
                    )
                }
            }

            if (weekly.targetAmountMinor == 0L) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(SaveBetterTheme.shapes.radiusCard))
                        .background(SaveBetterTheme.colors.goldTint)
                        .border(1.dp, SaveBetterTheme.colors.gold.copy(alpha = 0.4f), RoundedCornerShape(SaveBetterTheme.shapes.radiusCard))
                        .clickable { onEditBudgetClick() }
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.weekly_no_target_set_prompt),
                        style = SaveBetterTheme.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = SaveBetterTheme.colors.ink
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = CurrencyFormatter.formatMinor(weekly.targetAmountMinor, selectedLanguage),
                    style = SaveBetterTheme.typography.screenSubtitle.copy(fontSize = 18.sp),
                    color = SaveBetterTheme.colors.ink
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val spentColor = when {
                weekly.isOverBudget -> SaveBetterTheme.colors.brick
                weekly.isWarning -> SaveBetterTheme.colors.gold
                else -> SaveBetterTheme.colors.ink
            }
            LedgerLabelValueRow(
                label = stringResource(R.string.weekly_spent_label),
                value = CurrencyFormatter.formatMinor(weekly.spentAmountMinor, selectedLanguage),
                valueColor = spentColor,
                valueStyle = SaveBetterTheme.typography.amountLarge.copy(fontSize = 24.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProgressTrack(
                progress = weekly.percentage,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            val remainingColor = if (weekly.isOverBudget) SaveBetterTheme.colors.brick else SaveBetterTheme.colors.moss
            LedgerLabelValueRow(
                label = stringResource(R.string.weekly_remaining_label),
                value = CurrencyFormatter.formatMinor(weekly.remainingAmountMinor, selectedLanguage),
                valueColor = remainingColor
            )
        }

        // 2. Pace Warning Banner
        AnimatedVisibility(visible = weekly.isWarning || weekly.isOverBudget) {
            val isOver = weekly.isOverBudget
            val bannerType = if (isOver) AlertBannerType.Warning else AlertBannerType.Info
            val desc = stringResource(
                R.string.weekly_overshoot_warning_desc,
                CurrencyFormatter.formatMinor(weekly.dailyLimitMinor, selectedLanguage),
                2
            )
            AlertBanner(
                title = stringResource(R.string.weekly_overshoot_warning_title),
                message = desc,
                type = bannerType
            )
        }

        // 3. Advice Section
        if (uiState.adviceList.isNotEmpty()) {
            SectionLabel(text = stringResource(R.string.weekly_advice_section_title))

            uiState.adviceList.forEach { advice ->
                val title = if (advice.titleArgs.isNotEmpty()) {
                    stringResource(advice.titleResId, *advice.titleArgs.toTypedArray())
                } else {
                    stringResource(advice.titleResId)
                }

                val message = if (advice.messageArgs.isNotEmpty()) {
                    stringResource(advice.messageResId, *advice.messageArgs.toTypedArray())
                } else {
                    stringResource(advice.messageResId)
                }

                AlertBanner(
                    title = title,
                    message = message,
                    type = if (advice.isWarning) AlertBannerType.Warning else AlertBannerType.Info
                )
            }
        }

        // 4. Daily Spending Trend Chart Card
        SectionLabel(text = stringResource(R.string.weekly_trend_section_title))

        LedgerCard {
            DailyTrendBarChart(
                bars = uiState.dailyTrendBars,
                chartHeight = 84.dp,
                highlightColor = SaveBetterTheme.colors.gold
            )
        }

        // 5. This Week's Expenses List
        SectionLabel(text = stringResource(R.string.weekly_expenses_section_title))

        LedgerCard(isFlush = true) {
            if (uiState.weeklyExpenses.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SaveBetterTheme.colors.paper)
                            .border(1.dp, SaveBetterTheme.colors.paperLineStrong, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = SaveBetterTheme.colors.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.weekly_no_expenses_desc),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                uiState.weeklyExpenses.forEachIndexed { index, expense ->
                    ExpenseListRow(
                        title = expense.title,
                        subtitle = "${expense.categoryName} • ${expense.date}",
                        amount = CurrencyFormatter.formatMinor(expense.amountMinor, selectedLanguage),
                        badgeColor = expense.categoryColor,
                        showDivider = index < uiState.weeklyExpenses.lastIndex,
                        onClick = { onExpenseClick(expense.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WeeklyBudgetDialog(
    currentAmountMinor: Long,
    weekRangeText: String,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val initialValue = if (currentAmountMinor > 0L) {
        (currentAmountMinor / 100.0).toBigDecimal().stripTrailingZeros().toPlainString()
    } else ""
    var input by remember { mutableStateOf(initialValue) }
    val isValid = input.isNotBlank() && input.toDoubleOrNull()?.let { it > 0.0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaveBetterTheme.colors.paper,
        title = {
            Text(
                text = stringResource(R.string.weekly_budget_dialog_title),
                style = SaveBetterTheme.typography.screenSubtitle,
                color = SaveBetterTheme.colors.ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.weekly_budget_dialog_sub, weekRangeText),
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(R.string.weekly_target_label)) },
                    placeholder = { Text("e.g. 8,500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaveBetterTheme.colors.gold,
                        unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                        focusedLabelColor = SaveBetterTheme.colors.gold,
                        focusedTextColor = SaveBetterTheme.colors.ink,
                        unfocusedTextColor = SaveBetterTheme.colors.ink
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val major = input.toDoubleOrNull() ?: 0.0
                    onSave((major * 100).toLong())
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaveBetterTheme.colors.gold,
                    contentColor = SaveBetterTheme.colors.cover,
                    disabledContainerColor = SaveBetterTheme.colors.paperLine,
                    disabledContentColor = SaveBetterTheme.colors.textMuted
                )
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SaveBetterTheme.colors.ink)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

