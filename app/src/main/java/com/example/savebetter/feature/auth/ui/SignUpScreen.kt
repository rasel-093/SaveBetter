package com.example.savebetter.feature.auth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebetter.R
import com.example.savebetter.feature.auth.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.signUpState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SaveBetterColors.Paper
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader(
                title = stringResource(R.string.signup_title),
                subtitle = stringResource(R.string.signup_subtitle)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthErrorCard(errorMessage = state.errorMessage)

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            SaveBetterTextField(
                value = state.email,
                onValueChange = viewModel::onSignUpEmailChange,
                label = stringResource(R.string.email_label),
                placeholder = stringResource(R.string.email_placeholder),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            SaveBetterTextField(
                value = state.password,
                onValueChange = viewModel::onSignUpPasswordChange,
                label = stringResource(R.string.password_label),
                placeholder = stringResource(R.string.password_placeholder),
                isPassword = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm Password Input
            SaveBetterTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onSignUpConfirmPasswordChange,
                label = stringResource(R.string.confirm_password_label),
                placeholder = stringResource(R.string.password_placeholder),
                isPassword = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.signUp()
                    }
                ),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            SaveBetterButton(
                text = stringResource(R.string.signup_button),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.signUp()
                },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Back to Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    color = SaveBetterColors.TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = stringResource(R.string.sign_in_link),
                    color = SaveBetterColors.Moss,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }
        }
    }
}
