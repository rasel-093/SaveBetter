package com.example.savebetter.feature.addexpense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.component.AppTopBar
import com.example.savebetter.core.designsystem.component.CategoryChip
import com.example.savebetter.core.designsystem.component.SectionLabel
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.NumeralConverter
import com.example.savebetter.feature.addexpense.AddExpenseUiState
import com.example.savebetter.feature.addexpense.AddExpenseViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    userId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    editExpenseId: String? = null,
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    LaunchedEffect(userId, editExpenseId) {
        viewModel.initForUser(userId, editExpenseId)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) {
            onDismiss()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.action_save), color = SaveBetterTheme.colors.gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel), color = SaveBetterTheme.colors.textMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.isNewCategoryDialogOpen) {
        NewCategoryDialog(
            name = uiState.newCategoryName,
            colorToken = uiState.newCategoryColorToken,
            errorResId = uiState.newCategoryErrorResId,
            onNameChange = viewModel::onNewCategoryNameChange,
            onColorChange = viewModel::onNewCategoryColorChange,
            onDismiss = viewModel::closeNewCategoryDialog,
            onConfirm = viewModel::createCustomCategory
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = if (uiState.isEditing) {
                    stringResource(R.string.edit_expense_title)
                } else {
                    stringResource(R.string.add_expense_title)
                },
                onBackClick = onDismiss
            )
        },
        containerColor = SaveBetterTheme.colors.paper
    ) { innerPadding ->
        AddExpenseContent(
            uiState = uiState,
            selectedLanguage = selectedLanguage,
            innerPadding = innerPadding,
            onDigitClick = viewModel::onDigitClick,
            onDecimalClick = viewModel::onDecimalClick,
            onBackspaceClick = viewModel::onBackspaceClick,
            onClearClick = viewModel::onClearClick,
            onCategorySelected = viewModel::onCategorySelected,
            onOpenNewCategoryDialog = viewModel::openNewCategoryDialog,
            onOpenDatePicker = { showDatePicker = true },
            onNoteChange = viewModel::onNoteChange,
            onSave = { viewModel.save(userId) },
            onDelete = viewModel::delete
        )
    }
}

@Composable
private fun AddExpenseContent(
    uiState: AddExpenseUiState,
    selectedLanguage: AppLanguage,
    innerPadding: PaddingValues,
    onDigitClick: (Char) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onOpenNewCategoryDialog: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
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
        // 1. Amount Display Card
        AmountDisplayCard(
            amountString = uiState.amountString,
            selectedLanguage = selectedLanguage,
            errorResId = uiState.amountErrorResId
        )

        // 2. Category Selector Chips
        CategorySelectorSection(
            categories = uiState.categories,
            selectedCategoryId = uiState.selectedCategoryId,
            errorResId = uiState.categoryErrorResId,
            onCategorySelected = onCategorySelected,
            onOpenNewCategoryDialog = onOpenNewCategoryDialog
        )

        // 3. Date & Note Row
        DateAndNoteSection(
            selectedDate = uiState.selectedDate,
            note = uiState.note,
            onOpenDatePicker = onOpenDatePicker,
            onNoteChange = onNoteChange
        )

        // 4. Custom Numeric Keypad
        NumericKeypad(
            onDigitClick = onDigitClick,
            onDecimalClick = onDecimalClick,
            onBackspaceClick = onBackspaceClick
        )

        // 5. Action Buttons
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SaveBetterTheme.colors.gold,
                contentColor = SaveBetterTheme.colors.cover
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
                    text = if (uiState.isEditing) {
                        stringResource(R.string.action_update_expense)
                    } else {
                        stringResource(R.string.action_save_expense)
                    },
                    style = SaveBetterTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        if (uiState.isEditing) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SaveBetterTheme.colors.brick),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaveBetterTheme.colors.brick)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_delete_expense),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AmountDisplayCard(
    amountString: String,
    selectedLanguage: AppLanguage,
    errorResId: Int?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val isError = errorResId != null
    val borderColor = if (isError) SaveBetterTheme.colors.brick else SaveBetterTheme.colors.paperLineStrong

    val displayDigits = if (selectedLanguage == AppLanguage.BANGLA) {
        NumeralConverter.toBanglaDigits(amountString)
    } else {
        amountString
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(SaveBetterTheme.colors.card)
                .border(1.5.dp, borderColor, shape)
                .padding(vertical = 18.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "৳",
                    style = SaveBetterTheme.typography.amountLarge,
                    color = SaveBetterTheme.colors.gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = displayDigits,
                    style = SaveBetterTheme.typography.amountLarge,
                    color = SaveBetterTheme.colors.ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )
            }
        }

        if (errorResId != null) {
            Text(
                text = stringResource(errorResId),
                style = SaveBetterTheme.typography.caption,
                color = SaveBetterTheme.colors.brick,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelectorSection(
    categories: List<Category>,
    selectedCategoryId: String?,
    errorResId: Int?,
    onCategorySelected: (String) -> Unit,
    onOpenNewCategoryDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(text = stringResource(R.string.expense_category_label))

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val label = if (category.nameKey != null) {
                    val resId = context.resources.getIdentifier(category.nameKey, "string", context.packageName)
                    if (resId != 0) context.getString(resId) else category.nameKey
                } else {
                    category.customName ?: "Category"
                }

                val dotColor = getCategoryColor(category.colorToken)

                CategoryChip(
                    label = label,
                    dotColor = dotColor,
                    isSelected = category.id == selectedCategoryId,
                    onClick = { onCategorySelected(category.id) }
                )
            }

            // + New Category Chip
            CategoryChip(
                label = stringResource(R.string.expense_new_category),
                dotColor = SaveBetterTheme.colors.gold,
                isSelected = false,
                onClick = onOpenNewCategoryDialog
            )
        }

        if (errorResId != null) {
            Text(
                text = stringResource(errorResId),
                style = SaveBetterTheme.typography.caption,
                color = SaveBetterTheme.colors.brick,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun DateAndNoteSection(
    selectedDate: LocalDate,
    note: String,
    onOpenDatePicker: () -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Date Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SaveBetterTheme.colors.card)
                .border(1.dp, SaveBetterTheme.colors.paperLineStrong, RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenDatePicker)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = SaveBetterTheme.colors.gold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.expense_date_label),
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted
                )
            }

            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                style = SaveBetterTheme.typography.body,
                fontWeight = FontWeight.SemiBold,
                color = SaveBetterTheme.colors.ink
            )
        }

        // Note Input
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.expense_note_hint),
                    style = SaveBetterTheme.typography.caption,
                    color = SaveBetterTheme.colors.textMuted.copy(alpha = 0.6f)
                )
            },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SaveBetterTheme.colors.card,
                unfocusedContainerColor = SaveBetterTheme.colors.card,
                focusedBorderColor = SaveBetterTheme.colors.gold,
                unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong,
                focusedTextColor = SaveBetterTheme.colors.ink,
                unfocusedTextColor = SaveBetterTheme.colors.ink
            )
        )
    }
}

@Composable
private fun NumericKeypad(
    onDigitClick: (Char) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('.', '0', '⌫')
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SaveBetterTheme.colors.card)
            .border(1.dp, SaveBetterTheme.colors.paperLineStrong, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                '.' -> onDecimalClick()
                                '⌫' -> onBackspaceClick()
                                else -> onDigitClick(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SaveBetterTheme.colors.paper)
            .border(1.dp, SaveBetterTheme.colors.paperLine, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == '⌫') {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = SaveBetterTheme.colors.inkSoft,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = key.toString(),
                style = SaveBetterTheme.typography.amountMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = SaveBetterTheme.colors.ink
            )
        }
    }
}

@Composable
private fun NewCategoryDialog(
    name: String,
    colorToken: String,
    errorResId: Int?,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = listOf("cat1", "cat2", "cat3", "cat4", "cat5", "cat6")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_new_category_title),
                style = SaveBetterTheme.typography.screenTitle,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.dialog_category_name_label)) },
                    placeholder = { Text(stringResource(R.string.dialog_category_name_hint)) },
                    isError = errorResId != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaveBetterTheme.colors.gold,
                        unfocusedBorderColor = SaveBetterTheme.colors.paperLineStrong
                    )
                )

                if (errorResId != null) {
                    Text(
                        text = stringResource(errorResId),
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.brick
                    )
                }

                Text(
                    text = stringResource(R.string.dialog_category_color_label),
                    style = SaveBetterTheme.typography.caption,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { token ->
                        val isSelected = token == colorToken
                        val c = getCategoryColor(token)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) SaveBetterTheme.colors.ink else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onColorChange(token) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SaveBetterTheme.colors.gold)
            ) {
                Text(stringResource(R.string.dialog_create), color = SaveBetterTheme.colors.cover)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel), color = SaveBetterTheme.colors.textMuted)
            }
        },
        containerColor = SaveBetterTheme.colors.card
    )
}

private fun getCategoryColor(colorToken: String?): Color {
    return when (colorToken) {
        "cat1" -> Color(0xFFC9A227)
        "cat2" -> Color(0xFFA23E32)
        "cat3" -> Color(0xFF2F6F62)
        "cat4" -> Color(0xFF4C6785)
        "cat5" -> Color(0xFF7C8C3E)
        "cat6" -> Color(0xFF9C8F73)
        else -> Color(0xFFC9A227)
    }
}
