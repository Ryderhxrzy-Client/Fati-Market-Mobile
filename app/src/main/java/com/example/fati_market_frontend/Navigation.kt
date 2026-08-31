package com.fati_market

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fati_market.auth.ForgotPasswordScreen
import com.fati_market.auth.LoginScreen
import com.fati_market.auth.SignUpScreen

/**
 * How long a login stays valid when the server did not say. The API issues
 * seven-day tokens (`SANCTUM_TOKEN_EXPIRATION`), so this only covers a login
 * that predates the server sending `expires_in`.
 */
private const val SESSION_DURATION_MS = 7L * 24 * 60 * 60 * 1000  // 7 days

@Composable
fun AppNavigation(isDarkMode: Boolean, onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("splash") {
            // Resume straight into the app when the saved session is still live
            val destination = remember {
                val prefs     = context.getSharedPreferences("fatimarket_prefs", 0)
                val token     = prefs.getString("auth_token", null)
                val loginTime = prefs.getLong("login_timestamp", 0L)
                // The server tells us when its token dies; fall back to the
                // default window for sessions saved before it did.
                val expiresAt = prefs.getLong("session_expires_at", 0L)
                    .takeIf { it > 0L } ?: (loginTime + SESSION_DURATION_MS)
                val role      = prefs.getString("user_role", "admin") ?: "admin"
                if (token != null && System.currentTimeMillis() < expiresAt) {
                    if (role == "admin") "admin_home" else "student_home"
                } else {
                    "login"
                }
            }
            SplashScreen(navController, destination = destination)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignUpScreen(navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }
        composable("admin_home") {
            AdminDashboard(
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                onLogout = {
                    val prefs = context.getSharedPreferences("fatimarket_prefs", 0)
                    val token = prefs.getString("auth_token", "") ?: ""

                    // Call logout API in background
                    if (token.isNotBlank()) {
                        Thread {
                            performLogout(token)
                        }.start()
                    }

                    // Clear preferences and navigate to login
                    prefs.edit().clear().apply()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("student_home") {
            StudentDashboard(
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                onLogout = {
                    val prefs = context.getSharedPreferences("fatimarket_prefs", 0)
                    val token = prefs.getString("auth_token", "") ?: ""

                    // Call logout API in background
                    if (token.isNotBlank()) {
                        Thread {
                            performLogout(token)
                        }.start()
                    }

                    // Clear preferences and navigate to login
                    prefs.edit().clear().apply()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
