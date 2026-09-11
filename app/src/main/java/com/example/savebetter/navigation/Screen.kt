package com.example.savebetter.navigation

/**
 * Navigation destinations for the authentication flow.
 */
sealed class AuthScreen(val route: String) {
    data object Login : AuthScreen("auth/login")
    data object SignUp : AuthScreen("auth/signup")
    data object ForgotPassword : AuthScreen("auth/forgot_password")
}
