package com.example.savebetter.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens extracted from expense-tracker-ui-design-savebetter.html
 */
object SaveBetterColors {
    val Paper = Color(0xFFFBF6EA)
    val PaperLine = Color(0xFFE6DCC2)
    val PaperLineStrong = Color(0xFFD8CBA8)
    val Ink = Color(0xFF1E2A22)
    val InkSoft = Color(0xFF3A4A3E)
    val TextMuted = Color(0xFF7A7060)
    val Gold = Color(0xFFC9A227)
    val GoldSoft = Color(0xFFEFDFA3)
    val GoldTint = Color(0xFFF7EFD2)
    val Moss = Color(0xFF3F7856)
    val MossTint = Color(0xFFDCEADF)
    val Brick = Color(0xFFA23E32)
    val BrickTint = Color(0xFFF1DAD3)
    val Cover = Color(0xFF121A15)
}

/**
 * Branded header displayed atop auth screens.
 */
@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App emblem
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SaveBetterColors.GoldTint)
                .border(1.5.dp, SaveBetterColors.Gold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SB",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = SaveBetterColors.Ink
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SaveBetter",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SaveBetterColors.Ink,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SaveBetterColors.InkSoft
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = SaveBetterColors.TextMuted,
            lineHeight = 18.sp
        )
    }
}

/**
 * Paper-styled input text field.
 */
@Composable
fun SaveBetterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = SaveBetterColors.InkSoft,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = SaveBetterColors.TextMuted.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            },
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            enabled = enabled,
            singleLine = true,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = SaveBetterColors.TextMuted
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = SaveBetterColors.Gold,
                unfocusedBorderColor = SaveBetterColors.PaperLineStrong,
                focusedTextColor = SaveBetterColors.Ink,
                unfocusedTextColor = SaveBetterColors.Ink,
                cursorColor = SaveBetterColors.Gold
            )
        )
    }
}

/**
 * Primary action button in warm gold/ink.
 */
@Composable
fun SaveBetterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SaveBetterColors.Gold,
            contentColor = SaveBetterColors.Cover,
            disabledContainerColor = SaveBetterColors.PaperLineStrong,
            disabledContentColor = SaveBetterColors.TextMuted
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = SaveBetterColors.Cover,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Google Sign-In button with paper aesthetic.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = SaveBetterColors.Ink
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, SaveBetterColors.PaperLineStrong)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Google G badge text/icon
            Text(
                text = "G",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = SaveBetterColors.Moss
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Continue with Google",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SaveBetterColors.Ink
            )
        }
    }
}

/**
 * Error card displaying a localized, user-friendly message.
 */
@Composable
fun AuthErrorCard(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !errorMessage.isNullOrBlank(),
        modifier = modifier
    ) {
        if (!errorMessage.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SaveBetterColors.BrickTint)
                    .border(1.dp, SaveBetterColors.Brick.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = SaveBetterColors.Brick,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = errorMessage,
                    color = SaveBetterColors.Brick,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
