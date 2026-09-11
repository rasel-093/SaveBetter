package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

/**
 * Transaction row component matching `.list-row`.
 *
 * Monetary amount is strictly rendered using IBM Plex Mono.
 */
@Composable
fun ExpenseListRow(
    title: String,
    subtitle: String,
    amount: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    amountColor: Color? = null,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(clickModifier)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = title.take(1).uppercase(),
                            color = Color.White,
                            style = SaveBetterTheme.typography.caption,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = SaveBetterTheme.typography.body,
                        color = SaveBetterTheme.colors.ink,
                        fontSize = 13.sp
                    )
                    Text(
                        text = subtitle,
                        style = SaveBetterTheme.typography.caption,
                        color = SaveBetterTheme.colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Amount in IBM Plex Mono
            Text(
                text = amount,
                style = SaveBetterTheme.typography.amountSmall,
                color = amountColor ?: SaveBetterTheme.colors.ink
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = SaveBetterTheme.colors.paperLine,
                thickness = 1.dp
            )
        }
    }
}
