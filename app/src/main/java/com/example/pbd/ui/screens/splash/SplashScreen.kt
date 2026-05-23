package com.example.pbd.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbd.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

// ── Colour palette (mirrors HomeScreen) ───────────────────────────────────────
private val BgDark       = Color(0xFF0D0F1A)
private val AccentPurple = Color(0xFF7B61FF)
private val AccentBlue   = Color(0xFF4FC3F7)
private val TextPrimary  = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0B8D0)

private val LogoGradient = Brush.linearGradient(
    colors = listOf(AccentPurple, AccentBlue)
)

/**
 * Splash screen shown once on app start.
 *
 * Flow:
 *  1. Fade-in animation plays (~600 ms).
 *  2. Checks FirebaseAuth.currentUser — no network call, uses cached session.
 *  3. Navigates to Home (logged-in) or Login (guest), clearing Splash from
 *     the back stack so the user can never navigate back to it.
 */
@Composable
fun SplashScreen(navController: NavHostController) {

    // Fade-in alpha animation for the logo block
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate logo in
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        // Brief pause so the splash doesn't flash by too quickly
        delay(700)

        // Determine destination based on cached Firebase Auth state
        val currentUser = FirebaseAuth.getInstance().currentUser
        val destination = if (currentUser != null) Screen.Home.route else Screen.Login.route

        navController.navigate(destination) {
            // Remove Splash from the back stack entirely
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha.value)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo icon box
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(LogoGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App name
            Text(
                text = "FinanceTrack",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your personal budget, simplified",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            CircularProgressIndicator(
                color = AccentPurple,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
