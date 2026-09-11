package com.example.savebetter.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme

enum class SyncStatus {
    Synced,
    Syncing,
    Offline,
    Error
}

/**
 * Sync status badge matching `.sync-pill`.
 *
 * Uses IBM Plex Mono for the status label.
 */
@Composable
fun SyncStatusPill(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status) {
        SyncStatus.Synced -> Pair(SaveBetterTheme.colors.moss, "SYNCED")
        SyncStatus.Syncing -> Pair(SaveBetterTheme.colors.gold, "SYNCING")
        SyncStatus.Offline -> Pair(SaveBetterTheme.colors.textMuted, "OFFLINE")
        SyncStatus.Error -> Pair(SaveBetterTheme.colors.brick, "SYNC ERR")
    }

    val shape = SaveBetterTheme.shapes.pillShape

    Row(
        modifier = modifier
            .clip(shape)
            .border(1.dp, color, shape)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            style = SaveBetterTheme.typography.amountSmall,
            color = color,
            fontSize = 9.5.sp
        )
    }
}
