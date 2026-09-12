package com.example.savebetter.feature.settings.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.AppTopBar
import com.example.savebetter.core.designsystem.component.BottomNavBar
import com.example.savebetter.core.designsystem.component.BottomNavDestination
import com.example.savebetter.core.designsystem.component.LedgerCard
import com.example.savebetter.core.designsystem.component.SectionLabel
import com.example.savebetter.core.designsystem.component.SettingsToggleRow
import com.example.savebetter.core.designsystem.component.SyncStatus
import com.example.savebetter.core.designsystem.component.SyncStatusPill
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.ThemeMode
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import com.example.savebetter.core.i18n.NumeralConverter
import com.example.savebetter.feature.settings.SettingsViewModel

/**
 * Screen 09 — Settings / সেটিংস.
 *
 * Implements:
 * - Profile: Initials avatar, name, edit profile dialog.
 * - Display: Theme selection (System/Light/Dark), vibration toggle, language switcher.
 * - Notifications: Master toggle, weekly warning, monthly warning, month-end reconciliation reminder.
 * - Data & Sync: Category management, on-demand synchronization, logout, and delete account.
 */
@Composable
fun SettingsScreen(
    userId: String,
    selectedLanguage: AppLanguage,
    onBackClick: (() -> Unit)? = null,
    onDestinationSelected: ((BottomNavDestination) -> Unit)? = null,
    onDeleteAccountClick: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(userId) {
        viewModel.initUser(userId)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Notification permission launcher for Android 13+ (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setNotificationsEnabled(true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaveBetterTheme.colors.paper,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_screen_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            if (onDestinationSelected != null) {
                BottomNavBar(
                    selectedDestination = BottomNavDestination.Settings,
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
                // Section: Profile
                LedgerCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showEditProfile(true) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Avatar Circle
                        val initials = resolveAvatarInitials(uiState.userName, uiState.userEmail)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SaveBetterTheme.colors.ink),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = SaveBetterTheme.typography.screenTitle.copy(
                                    fontSize = 17.sp,
                                    color = SaveBetterTheme.colors.goldSoft
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.userName.ifBlank { "User" },
                                style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                                color = SaveBetterTheme.colors.ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_edit_profile),
                                style = SaveBetterTheme.typography.caption,
                                color = SaveBetterTheme.colors.textMuted
                            )
                        }
                    }
                }

                // Section: Display
                SectionLabel(text = stringResource(R.string.settings_section_display))
                LedgerCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Theme Row
                        val themeText = when (uiState.themeMode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                            ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                        }
                        SettingsClickableRow(
                            title = stringResource(R.string.settings_theme_label),
                            subtitle = stringResource(R.string.settings_theme_sub),
                            value = "$themeText ›",
                            onClick = { viewModel.showThemeDialog(true) }
                        )

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Vibration Toggle
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_vibration_label),
                            subtitle = stringResource(R.string.settings_vibration_sub),
                            checked = uiState.vibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) }
                        )

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Language Row
                        val langText = when (uiState.currentLanguage) {
                            AppLanguage.ENGLISH -> "English"
                            AppLanguage.BANGLA -> "বাংলা"
                        }
                        SettingsClickableRow(
                            title = stringResource(R.string.settings_language_label),
                            subtitle = stringResource(R.string.settings_language_sub),
                            value = "$langText ›",
                            onClick = { viewModel.showLanguageDialog(true) }
                        )
                    }
                }

                // Section: Notifications
                SectionLabel(text = stringResource(R.string.settings_section_notifications))
                LedgerCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Master Switch
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_notifications_all),
                            subtitle = stringResource(R.string.settings_notifications_all_sub),
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (!hasPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@SettingsToggleRow
                                    }
                                }
                                viewModel.setNotificationsEnabled(isChecked)
                            }
                        )

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Weekly Warning
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_notify_weekly),
                            subtitle = stringResource(R.string.settings_notify_weekly_sub),
                            checked = uiState.notifyWeeklyWarning && uiState.notificationsEnabled,
                            enabled = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotifyWeeklyWarning(it) }
                        )

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Monthly Warning
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_notify_monthly),
                            subtitle = stringResource(R.string.settings_notify_monthly_sub),
                            checked = uiState.notifyMonthlyWarning && uiState.notificationsEnabled,
                            enabled = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotifyMonthlyWarning(it) }
                        )

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Month-End Reconciliation Reminder
                        SettingsToggleRow(
                            title = stringResource(R.string.settings_notify_reconciliation),
                            subtitle = stringResource(R.string.settings_notify_reconciliation_sub),
                            checked = uiState.notifyReconciliation && uiState.notificationsEnabled,
                            enabled = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotifyReconciliation(it) }
                        )
                    }
                }

                // Section: Data & Sync
                SectionLabel(text = stringResource(R.string.settings_section_data_sync))
                LedgerCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Manage Categories
                        SettingsClickableRow(
                            title = stringResource(R.string.settings_manage_categories),
                            subtitle = stringResource(R.string.settings_manage_categories_sub),
                            value = "›",
                            onClick = { viewModel.showCategoriesDialog(true) }
                        )

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Sync Now
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !uiState.isSyncing) { viewModel.syncNow() }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_sync_now),
                                style = SaveBetterTheme.typography.body,
                                color = SaveBetterTheme.colors.ink
                            )

                            if (uiState.isSyncing) {
                                SyncStatusPill(
                                    status = SyncStatus.Syncing
                                )
                            } else {
                                SyncStatusPill(
                                    status = SyncStatus.Synced
                                )
                            }
                        }

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Log Out
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.showLogoutConfirm(true) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_logout),
                                style = SaveBetterTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                                color = SaveBetterTheme.colors.brick
                            )
                            Text(
                                text = "›",
                                style = SaveBetterTheme.typography.body,
                                color = SaveBetterTheme.colors.brick
                            )
                        }

                        HorizontalDivider(color = SaveBetterTheme.colors.paperLine)

                        // Delete Account / Data
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (onDeleteAccountClick != null) {
                                        onDeleteAccountClick()
                                    } else {
                                        viewModel.showDeleteAccountConfirm(true)
                                    }
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_delete_account),
                                style = SaveBetterTheme.typography.body,
                                color = SaveBetterTheme.colors.brick
                            )
                            Text(
                                text = "›",
                                style = SaveBetterTheme.typography.body,
                                color = SaveBetterTheme.colors.brick
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit Profile
    if (uiState.showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.userName,
            currentSalaryMinor = uiState.monthlySalaryMinor,
            onDismiss = { viewModel.showEditProfile(false) },
            onSave = { name, salary -> viewModel.updateProfile(name, salary) }
        )
    }

    // Dialog: Theme Selection
    if (uiState.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeMode,
            onDismiss = { viewModel.showThemeDialog(false) },
            onSelect = { viewModel.setThemeMode(it) }
        )
    }

    // Dialog: Language Selection
    if (uiState.showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = uiState.currentLanguage,
            onDismiss = { viewModel.showLanguageDialog(false) },
            onSelect = { viewModel.setLanguage(it) }
        )
    }

    // Dialog: Categories
    if (uiState.showCategoriesDialog) {
        CategoriesDialog(
            categories = uiState.categories,
            selectedLanguage = selectedLanguage,
            onDismiss = { viewModel.showCategoriesDialog(false) }
        )
    }

    // Dialog: Logout Confirmation
    if (uiState.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showLogoutConfirm(false) },
            containerColor = SaveBetterTheme.colors.paper,
            title = {
                Text(
                    text = stringResource(R.string.settings_logout_confirm_title),
                    style = SaveBetterTheme.typography.screenSubtitle,
                    color = SaveBetterTheme.colors.ink
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_logout_confirm_desc),
                    style = SaveBetterTheme.typography.body,
                    color = SaveBetterTheme.colors.inkSoft
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaveBetterTheme.colors.brick,
                        contentColor = SaveBetterTheme.colors.paper
                    )
                ) {
                    Text(stringResource(R.string.settings_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showLogoutConfirm(false) }) {
                    Text(stringResource(R.string.settings_cancel_button), color = SaveBetterTheme.colors.ink)
                }
            }
        )
    }

    // Dialog: Delete Account Confirmation (Step 13 Entrypoint)
    if (uiState.showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteAccountConfirm(false) },
            containerColor = SaveBetterTheme.colors.paper,
            title = {
                Text(
                    text = stringResource(R.string.settings_delete_account_confirm_title),
                    style = SaveBetterTheme.typography.screenSubtitle,
                    color = SaveBetterTheme.colors.brick
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_delete_account_confirm_desc),
                    style = SaveBetterTheme.typography.body,
                    color = SaveBetterTheme.colors.inkSoft
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showDeleteAccountConfirm(false)
                        onDeleteAccountClick?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaveBetterTheme.colors.brick,
                        contentColor = SaveBetterTheme.colors.paper
                    )
                ) {
                    Text(stringResource(R.string.settings_delete_account))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteAccountConfirm(false) }) {
                    Text(stringResource(R.string.settings_cancel_button), color = SaveBetterTheme.colors.ink)
                }
            }
        )
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SaveBetterTheme.typography.body,
                color = SaveBetterTheme.colors.ink
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted
                )
            }
        }
        Text(
            text = value,
            style = SaveBetterTheme.typography.caption.copy(fontSize = 12.sp),
            color = SaveBetterTheme.colors.textMuted
        )
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentSalaryMinor: Long,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    val initialSalary = (currentSalaryMinor / 100.0).toString().removeSuffix(".0")
    var salaryInput by remember { mutableStateOf(initialSalary) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaveBetterTheme.colors.paper,
        title = {
            Text(
                text = stringResource(R.string.settings_edit_profile_title),
                style = SaveBetterTheme.typography.screenSubtitle,
                color = SaveBetterTheme.colors.ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.settings_name_label)) },
                    placeholder = { Text(stringResource(R.string.settings_name_hint)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaveBetterTheme.colors.gold,
                        unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                        focusedTextColor = SaveBetterTheme.colors.ink,
                        unfocusedTextColor = SaveBetterTheme.colors.ink
                    )
                )

                OutlinedTextField(
                    value = salaryInput,
                    onValueChange = { salaryInput = it },
                    label = { Text(stringResource(R.string.settings_salary_label)) },
                    placeholder = { Text(stringResource(R.string.settings_salary_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaveBetterTheme.colors.gold,
                        unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                        focusedTextColor = SaveBetterTheme.colors.ink,
                        unfocusedTextColor = SaveBetterTheme.colors.ink
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalizedSalary = NumeralConverter.toEnglishDigits(salaryInput).trim().replace(",", "")
                    val salaryDouble = normalizedSalary.toDoubleOrNull() ?: 0.0
                    val salaryMinor = (salaryDouble * 100).toLong()
                    onSave(nameInput.trim(), salaryMinor)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaveBetterTheme.colors.ink,
                    contentColor = SaveBetterTheme.colors.goldSoft
                )
            ) {
                Text(stringResource(R.string.settings_save_button))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SaveBetterTheme.colors.ink)
            ) {
                Text(stringResource(R.string.settings_cancel_button))
            }
        }
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaveBetterTheme.colors.paper,
        title = {
            Text(
                text = stringResource(R.string.settings_theme_label),
                style = SaveBetterTheme.typography.screenSubtitle,
                color = SaveBetterTheme.colors.ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val label = when (mode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentTheme,
                            onClick = { onSelect(mode) },
                            colors = RadioButtonDefaults.colors(selectedColor = SaveBetterTheme.colors.gold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = SaveBetterTheme.typography.body,
                            color = SaveBetterTheme.colors.ink
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_close_button), color = SaveBetterTheme.colors.gold)
            }
        }
    )
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaveBetterTheme.colors.paper,
        title = {
            Text(
                text = stringResource(R.string.settings_language_label),
                style = SaveBetterTheme.typography.screenSubtitle,
                color = SaveBetterTheme.colors.ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppLanguage.entries.forEach { lang ->
                    val label = when (lang) {
                        AppLanguage.ENGLISH -> "English"
                        AppLanguage.BANGLA -> "বাংলা"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = lang == currentLanguage,
                            onClick = { onSelect(lang) },
                            colors = RadioButtonDefaults.colors(selectedColor = SaveBetterTheme.colors.gold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = SaveBetterTheme.typography.body,
                            color = SaveBetterTheme.colors.ink
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_close_button), color = SaveBetterTheme.colors.gold)
            }
        }
    )
}

@Composable
private fun CategoriesDialog(
    categories: List<com.example.savebetter.core.domain.model.Category>,
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SaveBetterTheme.colors.paper,
        title = {
            Text(
                text = stringResource(R.string.settings_categories_dialog_title),
                style = SaveBetterTheme.typography.screenSubtitle,
                color = SaveBetterTheme.colors.ink
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val name = if (cat.nameKey != null) {
                        val resId = context.resources.getIdentifier(cat.nameKey, "string", context.packageName)
                        if (resId != 0) context.getString(resId) else cat.nameKey
                    } else {
                        cat.customName ?: "Category"
                    }
                    val dotColor = when (cat.colorToken) {
                        "cat1" -> SaveBetterTheme.colors.cat1
                        "cat2" -> SaveBetterTheme.colors.cat2
                        "cat3" -> SaveBetterTheme.colors.cat3
                        "cat4" -> SaveBetterTheme.colors.cat4
                        "cat5" -> SaveBetterTheme.colors.cat5
                        "cat6" -> SaveBetterTheme.colors.cat6
                        else -> SaveBetterTheme.colors.gold
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = name,
                            style = SaveBetterTheme.typography.body,
                            color = SaveBetterTheme.colors.ink
                        )
                    }
                    HorizontalDivider(color = SaveBetterTheme.colors.paperLine)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_close_button), color = SaveBetterTheme.colors.gold)
            }
        }
    )
}

private fun resolveAvatarInitials(name: String, email: String): String {
    val trimmed = name.trim()
    if (trimmed.isNotEmpty()) {
        val parts = trimmed.split(" ").filter { it.isNotBlank() }
        return if (parts.size >= 2) {
            "${parts[0].first()}${parts[1].first()}"
        } else {
            trimmed.take(2)
        }
    }
    return email.firstOrNull()?.uppercase() ?: "S"
}
