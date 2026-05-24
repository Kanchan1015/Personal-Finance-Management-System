package com.example.pbd.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbd.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.compose.koinViewModel

// ── Colour palette ────────────────────────────────────────────────────────────
private val DarkBackground = Color(0xFF0D0F1A)
private val DarkCard       = Color(0xFF161929)
private val DarkBorder     = Color(0xFF212437)
private val LabelGray      = Color(0xFF9E9E9E)
private val WhiteText      = Color(0xFFFFFFFF)
private val GradientStart  = Color(0xFF7B61FF)   // purple
private val GradientEnd    = Color(0xFF00B4D8)   // cyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    
    // Observe the authentication state from the ViewModel
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val webClientId = remember(context) {
        val resourceId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (resourceId == 0) "" else context.getString(resourceId)
    }
    val googleSignInClient = remember(context, webClientId) {
        if (webClientId.isBlank()) {
            null
        } else {
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, googleSignInOptions)
        }
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(
                    context,
                    "Google Sign-In did not return an ID token",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                viewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            Toast.makeText(
                context,
                "Google Sign-In failed: ${e.localizedMessage ?: e.statusCode}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // React to state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.resetState() // Prevent re-triggering
                // Navigate to Home and clear the entire auth flow (Splash → Login) from the back stack
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is AuthState.PasswordResetSent -> {
                Toast.makeText(
                    context,
                    "Password reset email sent to ${(authState as AuthState.PasswordResetSent).email}",
                    Toast.LENGTH_LONG
                ).show()
                showForgotPasswordDialog = false
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(statusBarHeightDp + 16.dp))

            // ── App Name Title with Gradient ──────────────────────────────
            Text(
                text = "FinanceTrack",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Titles ────────────────────────────────────────────────────
            Text(
                text = "Welcome Back",
                color = WhiteText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Sign in to track your personal finances",
                color = LabelGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Email Input Field ─────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = LabelGray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedLabelColor = GradientStart,
                    unfocusedLabelColor = LabelGray,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ── Password Input Field ──────────────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = LabelGray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = icon, 
                            contentDescription = null, 
                            tint = LabelGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientStart,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedLabelColor = GradientStart,
                    unfocusedLabelColor = LabelGray,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Forgot password?",
                color = GradientEnd,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        resetEmail = email
                        showForgotPasswordDialog = true
                    }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Login Action Button ───────────────────────────────────────
            val isLoading = authState is AuthState.Loading
            val isEnabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isEnabled) {
                            Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                        } else {
                            Brush.horizontalGradient(listOf(DarkCard, DarkCard))
                        }
                    )
                    .clickable(enabled = isEnabled) {
                        viewModel.login(email, password)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = WhiteText,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Login",
                        color = if (isEnabled) WhiteText else LabelGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DarkBorder
                )
                Text(
                    text = "  or  ",
                    color = LabelGray,
                    fontSize = 12.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DarkBorder
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    val client = googleSignInClient
                    if (client == null) {
                        Toast.makeText(
                            context,
                            "Google Sign-In needs Firebase OAuth client setup first",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        googleSignInLauncher.launch(client.signInIntent)
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkCard,
                    contentColor = WhiteText,
                    disabledContainerColor = DarkCard,
                    disabledContentColor = LabelGray
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "G",
                    color = GradientEnd,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Navigation Link ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = LabelGray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Register",
                    color = GradientEnd,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Fixed Top Gradient Overlay to fade content going under status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeightDp + 16.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkBackground,
                            DarkBackground.copy(alpha = 0.95f),
                            DarkBackground.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = DarkCard,
            title = {
                Text(
                    text = "Reset Password",
                    color = WhiteText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter the email address linked to your account.",
                        color = LabelGray,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = LabelGray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GradientStart,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedLabelColor = GradientStart,
                            unfocusedLabelColor = LabelGray,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.sendPasswordResetEmail(resetEmail) },
                    enabled = authState !is AuthState.Loading && resetEmail.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                ) {
                    Text("Send Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = LabelGray)
                }
            }
        )
    }
}
