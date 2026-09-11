package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

/**
 * Interactive Design System Showcase screen displaying every token and component.
 *
 * Includes an interactive theme toggle to preview components under both
 * Light and Dark themes dynamically.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignSystemShowcase(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var selectedBottomNav by remember { mutableStateOf(BottomNavDestination.Home) }
    var selectedChipIndex by remember { mutableStateOf(0) }
    var toggleState1 by remember { mutableStateOf(true) }
    var toggleState2 by remember { mutableStateOf(false) }

    SaveBetterTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    title = "Design System",
                    subtitle = if (isDarkTheme) "Dark Mode • Paper Aesthetic" else "Light Mode • Paper Aesthetic",
                    onBackClick = onBackClick,
                    actions = {
                        IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = SaveBetterTheme.colors.gold
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavBar(
                    selectedDestination = selectedBottomNav,
                    onDestinationSelected = { selectedBottomNav = it }
                )
            },
            containerColor = SaveBetterTheme.colors.paper
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Section: Colors ──────────────────────────────────────────
                SectionLabel(text = "Color Tokens")

                LedgerCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Core Surfaces & Inks",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorSwatch("Paper", SaveBetterTheme.colors.paper)
                            ColorSwatch("Card", SaveBetterTheme.colors.card)
                            ColorSwatch("Line", SaveBetterTheme.colors.paperLine)
                            ColorSwatch("Ink", SaveBetterTheme.colors.ink)
                            ColorSwatch("Muted", SaveBetterTheme.colors.textMuted)
                        }

                        Text(
                            text = "Accents",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorSwatch("Gold", SaveBetterTheme.colors.gold)
                            ColorSwatch("Moss", SaveBetterTheme.colors.moss)
                            ColorSwatch("Brick", SaveBetterTheme.colors.brick)
                            ColorSwatch("Amber", SaveBetterTheme.colors.amber)
                            ColorSwatch("Slate", SaveBetterTheme.colors.slate)
                        }

                        Text(
                            text = "Category Palette (cat1 – cat6)",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorSwatch("Cat 1", SaveBetterTheme.colors.cat1)
                            ColorSwatch("Cat 2", SaveBetterTheme.colors.cat2)
                            ColorSwatch("Cat 3", SaveBetterTheme.colors.cat3)
                            ColorSwatch("Cat 4", SaveBetterTheme.colors.cat4)
                            ColorSwatch("Cat 5", SaveBetterTheme.colors.cat5)
                            ColorSwatch("Cat 6", SaveBetterTheme.colors.cat6)
                        }
                    }
                }

                // ── Section: Typography ──────────────────────────────────────
                SectionLabel(text = "Typography Scale")

                LedgerCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Screen Title (Tiro Bangla)",
                            style = SaveBetterTheme.typography.screenTitle,
                            color = SaveBetterTheme.colors.ink
                        )
                        Text(
                            text = "Screen Subtitle — Track simply, save intentionally (Hind Siliguri)",
                            style = SaveBetterTheme.typography.screenSubtitle,
                            color = SaveBetterTheme.colors.textMuted
                        )
                        Text(
                            text = "SECTION LABEL (UPPERCASE TRACKING)",
                            style = SaveBetterTheme.typography.sectionLabel,
                            color = SaveBetterTheme.colors.textMuted
                        )
                        Text(
                            text = "Body text in Hind Siliguri for readable personal finance notes.",
                            style = SaveBetterTheme.typography.body,
                            color = SaveBetterTheme.colors.ink
                        )
                        Text(
                            text = "Caption text for timestamps, categories, and helpers.",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monetary Amounts (IBM Plex Mono):",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        Text(
                            text = "Large: $14,250.00",
                            style = SaveBetterTheme.typography.amountLarge,
                            color = SaveBetterTheme.colors.ink
                        )
                        Text(
                            text = "Medium: $2,840.50",
                            style = SaveBetterTheme.typography.amountMedium,
                            color = SaveBetterTheme.colors.moss
                        )
                        Text(
                            text = "Small: $125.00",
                            style = SaveBetterTheme.typography.amountSmall,
                            color = SaveBetterTheme.colors.brick
                        )
                    }
                }

                // ── Section: Metrics & Progress ──────────────────────────────
                SectionLabel(text = "Statistics & Progress Tracks")

                LedgerCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        StatRow(
                            items = listOf(
                                StatItem("BUDGET", "$3,500"),
                                StatItem("SPENT", "$1,420", SaveBetterTheme.colors.moss),
                                StatItem("LEFT", "$2,080", SaveBetterTheme.colors.ink)
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Progress Track — Normal (< 70%)",
                                style = SaveBetterTheme.typography.caption,
                                color = SaveBetterTheme.colors.textMuted
                            )
                            ProgressTrack(progress = 0.40f)

                            Text(
                                text = "Progress Track — Warning (70% – 90%)",
                                style = SaveBetterTheme.typography.caption,
                                color = SaveBetterTheme.colors.textMuted
                            )
                            ProgressTrack(progress = 0.78f)

                            Text(
                                text = "Progress Track — Overbudget (> 90%)",
                                style = SaveBetterTheme.typography.caption,
                                color = SaveBetterTheme.colors.textMuted
                            )
                            ProgressTrack(progress = 0.94f)
                        }
                    }
                }

                // ── Section: Category Chips & Sync Status ────────────────────
                SectionLabel(text = "Chips & Status Pills")

                LedgerCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val chips = listOf(
                                "Food & Dining" to SaveBetterTheme.colors.cat1,
                                "Rent & Living" to SaveBetterTheme.colors.cat2,
                                "Utilities" to SaveBetterTheme.colors.cat3,
                                "Entertainment" to SaveBetterTheme.colors.cat4,
                                "Health" to SaveBetterTheme.colors.cat5,
                                "Travel" to SaveBetterTheme.colors.cat6
                            )
                            chips.forEachIndexed { index, (name, color) ->
                                CategoryChip(
                                    label = name,
                                    dotColor = color,
                                    isSelected = selectedChipIndex == index,
                                    onClick = { selectedChipIndex = index }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SyncStatusPill(status = SyncStatus.Synced)
                            SyncStatusPill(status = SyncStatus.Syncing)
                            SyncStatusPill(status = SyncStatus.Offline)
                            SyncStatusPill(status = SyncStatus.Error)
                        }
                    }
                }

                // ── Section: Alert Banners ───────────────────────────────────
                SectionLabel(text = "Alert Banners")

                AlertBanner(
                    title = "Monthly Budget Alert",
                    message = "You have used 88% of your entertainment budget this month.",
                    type = AlertBannerType.Warning
                )

                AlertBanner(
                    title = "Savings Milestone",
                    message = "Great job! You saved $240 more than last month.",
                    type = AlertBannerType.Success
                )

                AlertBanner(
                    title = "Sync Notice",
                    message = "Working offline. Transactions will sync when connected.",
                    type = AlertBannerType.Info
                )

                // ── Section: Transactions ────────────────────────────────────
                SectionLabel(text = "Transaction Rows (ExpenseListRow)")

                LedgerCard {
                    Column {
                        ExpenseListRow(
                            title = "Whole Foods Market",
                            subtitle = "Today, 5:40 PM • Groceries",
                            amount = "-$68.40",
                            badgeColor = SaveBetterTheme.colors.cat1,
                            amountColor = SaveBetterTheme.colors.brick,
                            icon = Icons.Outlined.Fastfood
                        )
                        ExpenseListRow(
                            title = "Apartment Rent",
                            subtitle = "Yesterday • Housing",
                            amount = "-$1,200.00",
                            badgeColor = SaveBetterTheme.colors.cat2,
                            amountColor = SaveBetterTheme.colors.brick,
                            icon = Icons.Outlined.Home
                        )
                        ExpenseListRow(
                            title = "Freelance Consulting",
                            subtitle = "Sep 10 • Income",
                            amount = "+$850.00",
                            badgeColor = SaveBetterTheme.colors.moss,
                            amountColor = SaveBetterTheme.colors.moss,
                            icon = Icons.Outlined.Receipt,
                            showDivider = false
                        )
                    }
                }

                // ── Section: Charts ──────────────────────────────────────────
                SectionLabel(text = "Visual Charts")

                LedgerCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Category Distribution (DonutChart)",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        DonutChart(
                            slices = listOf(
                                DonutSlice("Food", 47f, SaveBetterTheme.colors.cat1),
                                DonutSlice("Rent", 21f, SaveBetterTheme.colors.cat2),
                                DonutSlice("Utilities", 15f, SaveBetterTheme.colors.cat3),
                                DonutSlice("Leisure", 8f, SaveBetterTheme.colors.cat4),
                                DonutSlice("Health", 7f, SaveBetterTheme.colors.cat5),
                                DonutSlice("Other", 2f, SaveBetterTheme.colors.cat6)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Daily Trend (DailyTrendBarChart)",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        DailyTrendBarChart(
                            bars = listOf(
                                DailyBarData("M", 45f),
                                DailyBarData("T", 60f),
                                DailyBarData("W", 30f),
                                DailyBarData("T", 85f),
                                DailyBarData("F", 120f, isHighlighted = true),
                                DailyBarData("S", 75f),
                                DailyBarData("S", 40f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Comparative Spending (ComparisonBars)",
                            style = SaveBetterTheme.typography.caption,
                            color = SaveBetterTheme.colors.textMuted
                        )

                        ComparisonBars(
                            primaryItem = ComparisonBarItem("This Week", "$455.00", 455f, SaveBetterTheme.colors.moss),
                            secondaryItem = ComparisonBarItem("Last Week", "$580.00", 580f, SaveBetterTheme.colors.paperLineStrong)
                        )
                    }
                }

                // ── Section: Settings Toggles ────────────────────────────────
                SectionLabel(text = "Settings Controls")

                LedgerCard {
                    Column {
                        SettingsToggleRow(
                            title = "Biometric Authentication",
                            subtitle = "Require fingerprint or face recognition on launch",
                            checked = toggleState1,
                            onCheckedChange = { toggleState1 = it }
                        )
                        SettingsToggleRow(
                            title = "Daily Budget Notifications",
                            subtitle = "Receive an alert when nearing daily spending limit",
                            checked = toggleState2,
                            onCheckedChange = { toggleState2 = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(1.dp, SaveBetterTheme.colors.paperLineStrong, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = SaveBetterTheme.typography.caption,
            fontSize = 10.sp,
            color = SaveBetterTheme.colors.inkSoft
        )
    }
}
