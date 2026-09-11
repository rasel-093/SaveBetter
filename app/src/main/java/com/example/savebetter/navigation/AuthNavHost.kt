package com.example.savebetter.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.savebetter.feature.auth.AuthViewModel
import com.example.savebetter.feature.auth.ui.ForgotPasswordScreen
import com.example.savebetter.feature.auth.ui.LoginScreen
import com.example.savebetter.feature.auth.ui.SignUpScreen

/**
 * Authentication navigation graph managing transitions between
 * Login, Sign Up, and Forgot Password screens.
 */
@Composable
fun AuthNavHost(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route,
        modifier = modifier
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = {
                    viewModel.clearLoginError()
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    viewModel.clearLoginError()
                    navController.navigate(AuthScreen.ForgotPassword.route)
                }
            )
        }

        composable(AuthScreen.SignUp.route) {
            SignUpScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    viewModel.clearSignUpError()
                    navController.popBackStack()
                }
            )
        }

        composable(AuthScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBackToLogin = {
                    viewModel.resetForgotPasswordStatus()
                    navController.popBackStack()
                }
            )
        }
    }
}
