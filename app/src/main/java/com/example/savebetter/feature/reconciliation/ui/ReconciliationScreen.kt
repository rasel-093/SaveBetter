package com.example.savebetter.feature.reconciliation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.AlertBanner
import com.example.savebetter.core.designsystem.component.AlertBannerType
import com.example.savebetter.core.designsystem.component.AppTopBar
import com.example.savebetter.core.designsystem.component.LedgerCard
import com.example.savebetter.core.designsystem.component.LedgerLabelValueRow
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.ReconciliationStatus
import com.example.savebetter.feature.reconciliation.ReconciliationViewModel

/**
 * Screen 07: Out-of-Note Reconciliation (মাস শেষের মিলান).
 *
 * Reconciles starting cash / income against recorded expenses and current cash in hand
 * to discover and resolve unrecorded spending for the month.
 */
@Composable
fun ReconciliationScreen(
    onNavigateBack: () -> Unit,
    onOpenAddExpenseWithAmount: ((Long) -> Unit)? = null,
    viewModel: ReconciliationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.userMessageResId?.let { messageResId ->
        val message = stringResource(messageResId)
        LaunchedEffect(messageResId) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }


    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.reconciliation_title),
                subtitle = uiState.monthDisplay.ifBlank { null },
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.navigatePreviousMonth() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.monthly_prev_month),
                            tint = SaveBetterTheme.colors.ink,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateNextMonth() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.monthly_next_month),
                            tint = SaveBetterTheme.colors.ink,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SaveBetterTheme.colors.paper
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Guidance / Advice Banner
            AlertBanner(
                type = AlertBannerType.Info,
                title = null,
                message = stringResource(R.string.reconciliation_advice_desc)
            )

            // 2. Input Fields: Salary & Cash in Hand
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReconciliationInputField(
                    label = stringResource(R.string.reconciliation_salary_label),
                    value = uiState.salaryInput,
                    onValueChange = viewModel::onSalaryChanged,
                    placeholder = "৫৫,০০০"
                )

                ReconciliationInputField(
                    label = stringResource(R.string.reconciliation_hand_remaining_label),
                    value = uiState.handRemainingInput,
                    onValueChange = viewModel::onHandRemainingChanged,
                    placeholder = "৮,৫০০"
                )
            }

            // 3. Breakdown Card (Screen 07 core calculation)
            LedgerCard {
                LedgerLabelValueRow(
                    label = stringResource(R.string.reconciliation_net_outflow_label),
                    value = uiState.formattedNetOutflow
                )

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(
                    color = SaveBetterTheme.colors.paperLine,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LedgerLabelValueRow(
                    label = stringResource(R.string.reconciliation_app_logged_label),
                    value = uiState.formattedAppLogged,
                    valueColor = SaveBetterTheme.colors.moss
                )

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(
                    color = SaveBetterTheme.colors.paperLineStrong,
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LedgerLabelValueRow(
                    label = stringResource(R.string.reconciliation_unrecorded_label),
                    value = uiState.formattedUnrecorded,
                    valueColor = when (uiState.summary.status) {
                        ReconciliationStatus.UNRECORDED_EXPENSE -> SaveBetterTheme.colors.brick
                        ReconciliationStatus.BALANCED -> SaveBetterTheme.colors.moss
                        ReconciliationStatus.DATA_MISMATCH -> SaveBetterTheme.colors.amber
                    },
                    valueStyle = SaveBetterTheme.typography.amountLarge
                )
            }

            // 4. Status Banner
            when (uiState.summary.status) {
                ReconciliationStatus.UNRECORDED_EXPENSE -> {
                    AlertBanner(
                        type = AlertBannerType.Warning,
                        title = stringResource(
                            R.string.reconciliation_discrepancy_warning_title,
                            uiState.formattedUnrecorded
                        ),
                        message = stringResource(R.string.reconciliation_discrepancy_warning_desc)
                    )
                }
                ReconciliationStatus.DATA_MISMATCH -> {
                    AlertBanner(
                        type = AlertBannerType.Warning,
                        title = stringResource(R.string.reconciliation_mismatch_title),
                        message = stringResource(R.string.reconciliation_mismatch_desc)
                    )
                }
                ReconciliationStatus.BALANCED -> {
                    AlertBanner(
                        type = AlertBannerType.Success,
                        title = stringResource(R.string.reconciliation_balanced_title),
                        message = stringResource(R.string.reconciliation_balanced_desc)
                    )
                }
            }

            // 5. Action Buttons
            if (uiState.summary.status == ReconciliationStatus.UNRECORDED_EXPENSE) {
                Button(
                    onClick = { viewModel.showQuickLogConfirmation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaveBetterTheme.colors.ink,
                        contentColor = SaveBetterTheme.colors.paper
                    ),
                    shape = RoundedCornerShape(SaveBetterTheme.shapes.radiusCard)
                ) {
                    Text(
                        text = stringResource(R.string.reconciliation_add_expense_button),
                        style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.saveRecord() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(SaveBetterTheme.shapes.radiusCard),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SaveBetterTheme.colors.ink
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SaveBetterTheme.colors.paperLineStrong)
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = SaveBetterTheme.colors.ink
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.reconciliation_save_record_button),
                            style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 6. Quick Log Dialog
    if (uiState.showQuickLogDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissQuickLogConfirmation() },
            title = {
                Text(
                    text = stringResource(R.string.reconciliation_quick_log_title),
                    style = SaveBetterTheme.typography.screenTitle,
                    color = SaveBetterTheme.colors.ink
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.reconciliation_quick_log_desc,
                        uiState.formattedUnrecorded
                    ),
                    style = SaveBetterTheme.typography.body,
                    color = SaveBetterTheme.colors.inkSoft
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.quickLogDiscrepancy() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaveBetterTheme.colors.moss,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.reconciliation_quick_log_action),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                if (onOpenAddExpenseWithAmount != null) {
                    TextButton(
                        onClick = {
                            viewModel.dismissQuickLogConfirmation()
                            onOpenAddExpenseWithAmount(uiState.summary.outOfNoteExpenseMinor)
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.reconciliation_open_add_expense),
                            color = SaveBetterTheme.colors.slate
                        )
                    }
                } else {
                    TextButton(onClick = { viewModel.dismissQuickLogConfirmation() }) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = SaveBetterTheme.colors.textMuted
                        )
                    }
                }
            },
            containerColor = SaveBetterTheme.colors.card,
            shape = RoundedCornerShape(SaveBetterTheme.shapes.radiusCard)
        )
    }
}

@Composable
private fun ReconciliationInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(
            text = label,
            style = SaveBetterTheme.typography.caption,
            color = SaveBetterTheme.colors.textMuted,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Text(
                    text = "৳",
                    style = SaveBetterTheme.typography.amountMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SaveBetterTheme.colors.ink
                    )
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = SaveBetterTheme.typography.amountMedium,
                    color = SaveBetterTheme.colors.textMuted.copy(alpha = 0.5f)
                )
            },
            textStyle = SaveBetterTheme.typography.amountMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SaveBetterTheme.colors.ink
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SaveBetterTheme.colors.card,
                unfocusedContainerColor = SaveBetterTheme.colors.card,
                focusedBorderColor = SaveBetterTheme.colors.gold,
                unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                focusedTextColor = SaveBetterTheme.colors.ink,
                unfocusedTextColor = SaveBetterTheme.colors.ink
            ),
            shape = RoundedCornerShape(SaveBetterTheme.shapes.radiusCard)
        )
    }
}
