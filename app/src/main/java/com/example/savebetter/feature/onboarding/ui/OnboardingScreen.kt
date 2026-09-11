package com.example.savebetter.feature.onboarding.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.AlertBanner
import com.example.savebetter.core.designsystem.component.AlertBannerType
import com.example.savebetter.core.designsystem.component.AppTopBar
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.i18n.NumeralConverter
import com.example.savebetter.feature.onboarding.OnboardingUiState
import com.example.savebetter.feature.onboarding.OnboardingViewModel

/**
 * Onboarding Screen (Screen 02 from mockup).
 * Collects monthly salary, monthly expense target, savings goal, and weekly target.
 */
@Composable
fun OnboardingScreen(
    userId: String,
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    userName: String? = null,
    userEmail: String? = null,
    preferredLanguage: String = "en",
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onOnboardingCompleted()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.onboarding_title),
                subtitle = stringResource(R.string.onboarding_subtitle)
            )
        },
        containerColor = SaveBetterTheme.colors.paper
    ) { innerPadding ->
        OnboardingContent(
            uiState = uiState,
            innerPadding = innerPadding,
            onSalaryChange = viewModel::onSalaryChange,
            onMonthlyTargetChange = viewModel::onMonthlyTargetChange,
            onSavingGoalChange = viewModel::onSavingGoalChange,
            onWeeklyTargetChange = viewModel::onWeeklyTargetChange,
            onSubmit = {
                viewModel.submit(
                    userId = userId,
                    name = userName,
                    email = userEmail,
                    preferredLanguage = preferredLanguage
                )
            }
        )
    }
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    innerPadding: PaddingValues,
    onSalaryChange: (String) -> Unit,
    onMonthlyTargetChange: (String) -> Unit,
    onSavingGoalChange: (String) -> Unit,
    onWeeklyTargetChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // General error banner if submission failed
        AnimatedVisibility(visible = !uiState.generalError.isNullOrBlank()) {
            if (!uiState.generalError.isNullOrBlank()) {
                AlertBanner(
                    message = uiState.generalError,
                    type = AlertBannerType.Warning,
                    icon = Icons.Outlined.ErrorOutline
                )
            }
        }

        // 1. Monthly Salary Input
        CurrencyInputField(
            label = stringResource(R.string.onboarding_salary_label),
            placeholder = stringResource(R.string.onboarding_salary_hint),
            value = uiState.salaryInput,
            onValueChange = onSalaryChange,
            errorResId = uiState.salaryErrorResId,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        // 2. Monthly Expense Target Input
        CurrencyInputField(
            label = stringResource(R.string.onboarding_monthly_target_label),
            placeholder = stringResource(R.string.onboarding_monthly_target_hint),
            value = uiState.monthlyTargetInput,
            onValueChange = onMonthlyTargetChange,
            errorResId = uiState.monthlyTargetErrorResId,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        // 3. Monthly Savings Goal Input
        CurrencyInputField(
            label = stringResource(R.string.onboarding_saving_goal_label),
            placeholder = stringResource(R.string.onboarding_saving_goal_hint),
            value = uiState.savingGoalInput,
            onValueChange = onSavingGoalChange,
            errorResId = uiState.savingGoalErrorResId,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        // 4. Weekly Expense Target Input
        CurrencyInputField(
            label = stringResource(R.string.onboarding_weekly_target_label),
            placeholder = stringResource(R.string.onboarding_weekly_target_hint),
            value = uiState.weeklyTargetInput,
            onValueChange = onWeeklyTargetChange,
            errorResId = uiState.weeklyTargetErrorResId,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                onSubmit()
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Advice Banner matching .banner.advice in HTML mockup
        AlertBanner(
            message = stringResource(R.string.onboarding_advice),
            type = AlertBannerType.Info,
            icon = Icons.Outlined.Edit
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Continue Button
        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SaveBetterTheme.colors.gold,
                contentColor = SaveBetterTheme.colors.cover,
                disabledContainerColor = SaveBetterTheme.colors.paperLineStrong,
                disabledContentColor = SaveBetterTheme.colors.textMuted
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SaveBetterTheme.colors.cover,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.onboarding_submit_button),
                    style = SaveBetterTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

/**
 * Paper-styled numeric amount input field with ৳ currency prefix and IBM Plex Mono typography.
 */
@Composable
private fun CurrencyInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorResId: Int?,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = errorResId != null
    val borderColor = if (isError) SaveBetterTheme.colors.brick else SaveBetterTheme.colors.paperLineStrong
    val shape = RoundedCornerShape(10.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = SaveBetterTheme.typography.caption,
            fontWeight = FontWeight.SemiBold,
            color = SaveBetterTheme.colors.inkSoft,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(SaveBetterTheme.colors.card)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Taka Symbol (৳)
            Text(
                text = "৳",
                style = SaveBetterTheme.typography.amountMedium,
                color = SaveBetterTheme.colors.gold,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Amount Input
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = SaveBetterTheme.typography.amountMedium,
                        color = SaveBetterTheme.colors.textMuted.copy(alpha = 0.5f),
                        fontSize = 18.sp
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = SaveBetterTheme.typography.amountMedium.copy(
                        color = SaveBetterTheme.colors.ink,
                        fontSize = 18.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                    cursorBrush = SolidColor(SaveBetterTheme.colors.gold)
                )
            }
        }

        if (errorResId != null) {
            Text(
                text = stringResource(errorResId),
                style = SaveBetterTheme.typography.caption,
                color = SaveBetterTheme.colors.brick,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
