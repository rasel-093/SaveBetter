package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

enum class BottomNavDestination(
    val label: String,
    val labelResId: Int,
    val icon: ImageVector
) {
    Home("Home", R.string.nav_home, Icons.Outlined.Home),
    Weekly("Weekly", R.string.nav_weekly, Icons.Outlined.DateRange),
    Monthly("Monthly", R.string.nav_monthly, Icons.Outlined.CalendarMonth),
    Debts("Debts", R.string.nav_debts, Icons.Outlined.AccountBalanceWallet),
    Settings("Settings", R.string.nav_settings, Icons.Outlined.Settings)
}

/**
 * Bottom navigation bar matching `.bottom-nav` and `.nav-item`.
 *
 * Uses the dark cover background (`#121A15`) with gold active indicators.
 */
@Composable
fun BottomNavBar(
    selectedDestination: BottomNavDestination,
    onDestinationSelected: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val unselectedColor = Color(0xFF8B9C8F)
    val selectedColor = SaveBetterTheme.colors.gold

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SaveBetterTheme.colors.cover)
            .navigationBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavDestination.entries.forEach { destination ->
            val isSelected = destination == selectedDestination
            val tint = if (isSelected) selectedColor else unselectedColor
            val localizedLabel = stringResource(destination.labelResId)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = { onDestinationSelected(destination) },
                        role = Role.Tab
                    )
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = localizedLabel,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )

                Text(
                    text = localizedLabel,
                    style = SaveBetterTheme.typography.caption,
                    color = tint,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
