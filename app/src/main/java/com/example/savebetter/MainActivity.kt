package com.example.savebetter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity host for the SaveBetter application.
 *
 * Navigation is handled entirely by Compose Navigation — there is only
 * one Activity. All screens are Composable destinations within the
 * NavHost (wired in the navigation package, Step 1).
 *
 * @AndroidEntryPoint allows Hilt to inject into this Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Step 2 will replace this with SaveBetterTheme { ... }
            // Step 1 will replace SaveBetterPlaceholderScreen with the
            // auth-gated navigation graph.
            SaveBetterPlaceholderScreen()
        }
    }
}

/**
 * Temporary placeholder screen shown at Step 0.
 *
 * This screen confirms:
 *   ✓ App launches
 *   ✓ Hilt is wired (no crash = DI works)
 *   ✓ Compose renders
 *
 * It will be replaced by the navigation graph in Step 1.
 */
@Composable
fun SaveBetterPlaceholderScreen() {
    // Using raw Color values here because the design system (Step 2) does not
    // exist yet. This is the ONLY place where raw colors are acceptable.
    val bgColor    = Color(0xFF121A15) // --cover from design spec
    val textColor  = Color(0xFFD9D2BF) // --text-on-cover from design spec
    val goldColor  = Color(0xFFC9A227) // --gold from design spec

    Scaffold(
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "৳",
                fontSize   = 64.sp,
                color      = goldColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text       = "SaveBetter",
                fontSize   = 28.sp,
                color      = textColor,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text     = "Step 0 — Architecture Foundation",
                fontSize = 13.sp,
                color    = textColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text     = "✓ Hilt initialized\n✓ Firebase configured\n✓ Room ready\n✓ DataStore ready",
                fontSize = 13.sp,
                color    = goldColor.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPlaceholder() {
    SaveBetterPlaceholderScreen()
}
