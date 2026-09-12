package com.example.savebetter.feature.debts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.AlertBanner
import com.example.savebetter.core.designsystem.component.AlertBannerType
import com.example.savebetter.core.designsystem.component.AppTopBar
import com.example.savebetter.core.designsystem.component.BottomNavBar
import com.example.savebetter.core.designsystem.component.BottomNavDestination
import com.example.savebetter.core.designsystem.component.CategoryChip
import com.example.savebetter.core.designsystem.component.LedgerCard
import com.example.savebetter.core.designsystem.component.SectionLabel
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.core.i18n.NumeralConverter
import com.example.savebetter.feature.debts.DebtTab
import com.example.savebetter.feature.debts.DebtsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/**
 * Screen 08 — Debts & Credits / দেনা-পাওনা.
 *
 * Implements:
 * - Dual summaries: Total Receivable (moss) & Total Payable (brick)
 * - Net position advice banner explaining exclusion from monthly savings
 * - Separate sections for active receivables and payables
 * - Settled records retained in history
 * - Add/Edit/Settle dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    userId: String,
    selectedLanguage: AppLanguage,
    onBackClick: (() -> Unit)? = null,
    onDestinationSelected: ((BottomNavDestination) -> Unit)? = null,
    viewModel: DebtsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(userId) {
        viewModel.initUser(userId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var selectedItemForAction by remember { mutableStateOf<DebtCredit?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaveBetterTheme.colors.paper,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.debts_screen_title),
                onBackClick = onBackClick,
                actions = {
                    TextButton(onClick = { viewModel.openAddDialog() }) {
                        Text(
                            text = stringResource(R.string.debts_add_new_action),
                            style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                            color = SaveBetterTheme.colors.gold
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (onDestinationSelected != null) {
                BottomNavBar(
                    selectedDestination = BottomNavDestination.Debts,
                    onDestinationSelected = onDestinationSelected
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SaveBetterTheme.colors.gold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dual summaries: Total Receivable & Total Payable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LedgerCard(modifier = Modifier.weight(1f)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.debts_total_receivable_label),
                                style = SaveBetterTheme.typography.sectionLabel,
                                color = SaveBetterTheme.colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = CurrencyFormatter.formatMinor(
                                    uiState.summary.totalReceivableMinor,
                                    selectedLanguage
                                ),
                                style = SaveBetterTheme.typography.amountMedium.copy(fontWeight = FontWeight.Bold),
                                color = SaveBetterTheme.colors.moss
                            )
                        }
                    }

                    LedgerCard(modifier = Modifier.weight(1f)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.debts_total_payable_label),
                                style = SaveBetterTheme.typography.sectionLabel,
                                color = SaveBetterTheme.colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = CurrencyFormatter.formatMinor(
                                    uiState.summary.totalPayableMinor,
                                    selectedLanguage
                                ),
                                style = SaveBetterTheme.typography.amountMedium.copy(fontWeight = FontWeight.Bold),
                                color = SaveBetterTheme.colors.brick
                            )
                        }
                    }
                }

                // Advice Banner: Net calculation + notice that debt/credit is excluded from savings
                val netAmountFormatted = CurrencyFormatter.formatMinor(
                    abs(uiState.summary.netPositionMinor),
                    selectedLanguage
                )
                val netTitle = when {
                    uiState.summary.netPositionMinor > 0L ->
                        stringResource(R.string.debts_net_position_receivable, netAmountFormatted)
                    uiState.summary.netPositionMinor < 0L ->
                        stringResource(R.string.debts_net_position_payable, netAmountFormatted)
                    else ->
                        stringResource(R.string.debts_net_position_balanced, netAmountFormatted)
                }

                AlertBanner(
                    title = netTitle,
                    message = stringResource(R.string.debts_savings_exclusion_notice),
                    type = AlertBannerType.Info,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tabs: Active vs History
                TabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = SaveBetterTheme.colors.paper,
                    contentColor = SaveBetterTheme.colors.gold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                            color = SaveBetterTheme.colors.gold
                        )
                    },
                    divider = {
                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == DebtTab.ACTIVE,
                        onClick = { viewModel.selectTab(DebtTab.ACTIVE) },
                        text = {
                            Text(
                                text = stringResource(R.string.debts_tab_active),
                                style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                                color = if (uiState.selectedTab == DebtTab.ACTIVE) SaveBetterTheme.colors.ink else SaveBetterTheme.colors.textMuted
                            )
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == DebtTab.HISTORY,
                        onClick = { viewModel.selectTab(DebtTab.HISTORY) },
                        text = {
                            Text(
                                text = stringResource(R.string.debts_tab_history),
                                style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                                color = if (uiState.selectedTab == DebtTab.HISTORY) SaveBetterTheme.colors.ink else SaveBetterTheme.colors.textMuted
                            )
                        }
                    )
                }

                if (uiState.selectedTab == DebtTab.ACTIVE) {
                    // Section 1: Receivables (Lent / তারা আপনাকে দেবে)
                    SectionLabel(text = stringResource(R.string.debts_receivables_section_title))
                    if (uiState.summary.activeReceivables.isEmpty()) {
                        LedgerCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.debts_empty_receivables),
                                style = SaveBetterTheme.typography.caption,
                                color = SaveBetterTheme.colors.textMuted,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        LedgerCard(isFlush = true) {
                            uiState.summary.activeReceivables.forEachIndexed { index, item ->
                                DebtListRow(
                                    item = item,
                                    selectedLanguage = selectedLanguage,
                                    onRowClick = { selectedItemForAction = item },
                                    onSettleClick = { viewModel.toggleSettle(item) }
                                )
                                if (index < uiState.summary.activeReceivables.lastIndex) {
                                    HorizontalDivider(color = SaveBetterTheme.colors.paperLine)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Section 2: Payables (Borrowed / আপনি দেবেন)
                    SectionLabel(text = stringResource(R.string.debts_payables_section_title))
                    if (uiState.summary.activePayables.isEmpty()) {
                        LedgerCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.debts_empty_payables),
                                style = SaveBetterTheme.typography.caption,
                                color = SaveBetterTheme.colors.textMuted,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        LedgerCard(isFlush = true) {
                            uiState.summary.activePayables.forEachIndexed { index, item ->
                                DebtListRow(
                                    item = item,
                                    selectedLanguage = selectedLanguage,
                                    onRowClick = { selectedItemForAction = item },
                                    onSettleClick = { viewModel.toggleSettle(item) }
                                )
                                if (index < uiState.summary.activePayables.lastIndex) {
                                    HorizontalDivider(color = SaveBetterTheme.colors.paperLine)
                                }
                            }
                        }
                    }
                } else {
                    // History & Settled Section
                    SectionLabel(text = stringResource(R.string.debts_history_section_title))
                    if (uiState.summary.settledList.isEmpty()) {
                        LedgerCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.debts_empty_history),
                                style = SaveBetterTheme.typography.caption,
                                color = SaveBetterTheme.colors.textMuted,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        LedgerCard(isFlush = true) {
                            uiState.summary.settledList.forEachIndexed { index, item ->
                                DebtListRow(
                                    item = item,
                                    selectedLanguage = selectedLanguage,
                                    onRowClick = { selectedItemForAction = item },
                                    onSettleClick = { viewModel.toggleSettle(item) }
                                )
                                if (index < uiState.summary.settledList.lastIndex) {
                                    HorizontalDivider(color = SaveBetterTheme.colors.paperLine)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (uiState.isAddEditSheetOpen) {
        DebtAddEditDialog(
            editingItem = uiState.editingItem,
            errorMessage = uiState.errorMessage,
            selectedLanguage = selectedLanguage,
            onDismiss = { viewModel.dismissDialog() },
            onSave = { direction, personName, amountMinor, note, dueDate ->
                viewModel.saveDebtCredit(direction, personName, amountMinor, note, dueDate)
            }
        )
    }

    // Item Action Dialog (Settle, Edit, Delete)
    selectedItemForAction?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItemForAction = null },
            containerColor = SaveBetterTheme.colors.paper,
            title = {
                Text(
                    text = item.personName,
                    style = SaveBetterTheme.typography.screenSubtitle,
                    color = SaveBetterTheme.colors.ink
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = CurrencyFormatter.formatMinor(item.amountMinor, selectedLanguage),
                        style = SaveBetterTheme.typography.amountMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (item.direction == DebtDirection.RECEIVABLE) SaveBetterTheme.colors.moss else SaveBetterTheme.colors.brick
                    )
                    item.note?.let {
                        Text(
                            text = it,
                            style = SaveBetterTheme.typography.body,
                            color = SaveBetterTheme.colors.inkSoft
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toUpdate = selectedItemForAction
                        selectedItemForAction = null
                        if (toUpdate != null) {
                            viewModel.toggleSettle(toUpdate)
                        }
                    }
                ) {
                    Text(
                        text = if (item.isSettled) {
                            stringResource(R.string.debts_action_unsettle)
                        } else {
                            stringResource(R.string.debts_action_settle)
                        },
                        color = SaveBetterTheme.colors.moss,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val toDelete = selectedItemForAction
                            selectedItemForAction = null
                            if (toDelete != null) {
                                viewModel.deleteItem(toDelete)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.debts_action_delete),
                            color = SaveBetterTheme.colors.brick
                        )
                    }
                    TextButton(
                        onClick = {
                            val toEdit = selectedItemForAction
                            selectedItemForAction = null
                            if (toEdit != null) {
                                viewModel.openEditDialog(toEdit)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.debts_action_edit),
                            color = SaveBetterTheme.colors.gold
                        )
                    }
                }
            }
        )
    }
}

/**
 * Single row item matching Screen 08 list row:
 * - Icon: Moss circle with ↓ for receivable, Brick circle with ↑ for payable, Gold check for settled
 * - Left: Person name, subtitle (date or note)
 * - Right: Formatted amount + action button
 */
@Composable
private fun DebtListRow(
    item: DebtCredit,
    selectedLanguage: AppLanguage,
    onRowClick: () -> Unit,
    onSettleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReceivable = item.direction == DebtDirection.RECEIVABLE
    val iconBg = when {
        item.isSettled -> SaveBetterTheme.colors.paperLineStrong
        isReceivable -> SaveBetterTheme.colors.moss
        else -> SaveBetterTheme.colors.brick
    }
    val amountColor = when {
        item.isSettled -> SaveBetterTheme.colors.textMuted
        isReceivable -> SaveBetterTheme.colors.moss
        else -> SaveBetterTheme.colors.brick
    }
    val prefix = if (isReceivable) "+" else "-"

    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    val dateText = item.date.atZone(ZoneId.systemDefault()).format(dateFormatter)
    val localizedDate = if (selectedLanguage == AppLanguage.BANGLA) NumeralConverter.toBanglaDigits(dateText) else dateText

    val subtitle = buildString {
        if (!item.note.isNullOrBlank()) {
            append(item.note)
            append(" · ")
        }
        append(localizedDate)
        if (item.dueDate != null) {
            val dueFormatted = item.dueDate.atZone(ZoneId.systemDefault()).format(dateFormatter)
            val localizedDue = if (selectedLanguage == AppLanguage.BANGLA) NumeralConverter.toBanglaDigits(dueFormatted) else dueFormatted
            append(" · ")
            append(stringResource(R.string.debts_due_date_prefix, localizedDue))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                if (item.isSettled) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.debts_settled_badge),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isReceivable) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = stringResource(R.string.debts_direction_receivable),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = stringResource(R.string.debts_direction_payable),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.personName,
                        style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                        color = SaveBetterTheme.colors.ink,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isSettled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${stringResource(R.string.debts_settled_badge)}]",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(max = 140.dp)
        ) {
            Text(
                text = "$prefix${CurrencyFormatter.formatMinor(item.amountMinor, selectedLanguage)}",
                style = SaveBetterTheme.typography.amountSmall.copy(fontWeight = FontWeight.SemiBold),
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .widthIn(max = 100.dp)
            )

            IconButton(
                onClick = onSettleClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (item.isSettled) Icons.Default.Undo else Icons.Default.Check,
                    contentDescription = if (item.isSettled) {
                        stringResource(R.string.debts_action_unsettle)
                    } else {
                        stringResource(R.string.debts_action_settle)
                    },
                    tint = if (item.isSettled) SaveBetterTheme.colors.textMuted else SaveBetterTheme.colors.moss,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Dialog for creating or editing a debt/credit record.
 */
@Composable
private fun DebtAddEditDialog(
    editingItem: DebtCredit?,
    errorMessage: String?,
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (DebtDirection, String, Long, String?, Instant?) -> Unit
) {
    var direction by remember(editingItem) {
        mutableStateOf(editingItem?.direction ?: DebtDirection.RECEIVABLE)
    }
    var personName by remember(editingItem) {
        mutableStateOf(editingItem?.personName ?: "")
    }
    var amountInput by remember(editingItem) {
        val initialAmount = editingItem?.let { (it.amountMinor / 100.0).toString().removeSuffix(".0") } ?: ""
        mutableStateOf(initialAmount)
    }
    var note by remember(editingItem) {
        mutableStateOf(editingItem?.note ?: "")
    }
    var localError by remember { mutableStateOf<String?>(null) }

    val errorEmptyPerson = stringResource(R.string.debts_error_empty_person)
    val errorInvalidAmount = stringResource(R.string.debts_error_invalid_amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaveBetterTheme.colors.paper,
        title = {
            Text(
                text = if (editingItem == null) {
                    stringResource(R.string.debts_dialog_add_title)
                } else {
                    stringResource(R.string.debts_dialog_edit_title)
                },
                style = SaveBetterTheme.typography.screenSubtitle,
                color = SaveBetterTheme.colors.ink
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Direction Selector
                Text(
                    text = stringResource(R.string.debts_direction_label),
                    style = SaveBetterTheme.typography.sectionLabel,
                    color = SaveBetterTheme.colors.textMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        label = stringResource(R.string.debts_direction_receivable),
                        dotColor = SaveBetterTheme.colors.moss,
                        isSelected = direction == DebtDirection.RECEIVABLE,
                        onClick = { direction = DebtDirection.RECEIVABLE }
                    )
                    CategoryChip(
                        label = stringResource(R.string.debts_direction_payable),
                        dotColor = SaveBetterTheme.colors.brick,
                        isSelected = direction == DebtDirection.PAYABLE,
                        onClick = { direction = DebtDirection.PAYABLE }
                    )
                }

                // Person Name
                OutlinedTextField(
                    value = personName,
                    onValueChange = {
                        personName = it
                        localError = null
                    },
                    label = { Text(stringResource(R.string.debts_person_name_label)) },
                    placeholder = { Text(stringResource(R.string.debts_person_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaveBetterTheme.colors.gold,
                        unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                        focusedTextColor = SaveBetterTheme.colors.ink,
                        unfocusedTextColor = SaveBetterTheme.colors.ink
                    )
                )

                // Amount
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        localError = null
                    },
                    label = { Text(stringResource(R.string.debts_amount_label)) },
                    placeholder = { Text(stringResource(R.string.debts_amount_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaveBetterTheme.colors.gold,
                        unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                        focusedTextColor = SaveBetterTheme.colors.ink,
                        unfocusedTextColor = SaveBetterTheme.colors.ink
                    )
                )

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.debts_note_label)) },
                    placeholder = { Text(stringResource(R.string.debts_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaveBetterTheme.colors.gold,
                        unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                        focusedTextColor = SaveBetterTheme.colors.ink,
                        unfocusedTextColor = SaveBetterTheme.colors.ink
                    )
                )

                val displayError = localError ?: errorMessage
                if (displayError != null) {
                    Text(
                        text = displayError,
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.brick
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedPerson = personName.trim()
                    if (trimmedPerson.isBlank()) {
                        localError = errorEmptyPerson
                        return@Button
                    }
                    val normalizedAmount = NumeralConverter.toEnglishDigits(amountInput).trim().replace(",", "")
                    val amountDouble = normalizedAmount.toDoubleOrNull()
                    if (amountDouble == null || amountDouble <= 0.0) {
                        localError = errorInvalidAmount
                        return@Button
                    }
                    val amountMinor = (amountDouble * 100).toLong()

                    onSave(
                        direction,
                        trimmedPerson,
                        amountMinor,
                        note.trim().takeIf { it.isNotEmpty() },
                        editingItem?.dueDate
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaveBetterTheme.colors.ink,
                    contentColor = SaveBetterTheme.colors.goldSoft
                )
            ) {
                Text(stringResource(R.string.debts_save_button))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SaveBetterTheme.colors.ink
                )
            ) {
                Text(stringResource(R.string.debts_cancel_button))
            }
        }
    )
}
