package com.example.pbd.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbd.navigation.Screen
import com.example.pbd.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = profileState) {
                is ProfileState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
                is ProfileState.Success -> {
                    var isEditMode by remember { mutableStateOf(false) }
                    var editedName by remember { mutableStateOf(state.user.name) }
                    var editedCurrency by remember { mutableStateOf(state.user.baseCurrency) }
                    var editedSavingsPercentage by remember { mutableStateOf(state.user.savingsPercentage) }
                    var editedNotifications by remember { mutableStateOf(viewModel.notificationsEnabled) }
                    var editedBudgetThreshold by remember { mutableStateOf(viewModel.budgetThreshold.toInt().toString()) }
                    var editedLargeTransactionThreshold by remember { mutableStateOf(viewModel.largeTransactionThreshold.toInt().toString()) }
                    var editedDailySummaryHour by remember { mutableStateOf(viewModel.dailySummaryHour.toString()) }
                    var editedDailySummaryMinute by remember { mutableStateOf(viewModel.dailySummaryMinute.toString()) }

                    // Reset values when entering or leaving edit mode
                    LaunchedEffect(state.user, isEditMode) {
                        if (!isEditMode) {
                            editedName = state.user.name
                            editedCurrency = state.user.baseCurrency
                            editedSavingsPercentage = state.user.savingsPercentage
                            editedNotifications = viewModel.notificationsEnabled
                            editedBudgetThreshold = viewModel.budgetThreshold.toInt().toString()
                            editedLargeTransactionThreshold = viewModel.largeTransactionThreshold.toInt().toString()
                            editedDailySummaryHour = viewModel.dailySummaryHour.toString()
                            editedDailySummaryMinute = viewModel.dailySummaryMinute.toString()
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Circular Avatar with Gradient & initials
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(AccentPurple, AccentOrange)
                                    )
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = state.user.name.split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercase() }
                                .take(2)
                                .joinToString("")
                            
                            Text(
                                text = initials.ifEmpty { "U" },
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // User Name & Email display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.user.name,
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.user.email,
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        if (!isEditMode) {
                            // ─── VIEW PROFILE MODE ───

                            // Account Details Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Account Details",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    HorizontalDivider(color = DarkSurface, thickness = 1.dp)
                                    
                                    DetailItem(
                                        icon = Icons.Default.Person,
                                        label = "Display Name",
                                        value = state.user.name
                                    )
                                    DetailItem(
                                        icon = Icons.Default.Email,
                                        label = "Email Address",
                                        value = state.user.email
                                    )
                                    DetailItem(
                                        icon = Icons.Default.AttachMoney,
                                        label = "Preferred Currency",
                                        value = state.user.baseCurrency
                                    )
                                }
                            }

                            // Smart Savings & Alerts Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Engine Preferences",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    HorizontalDivider(color = DarkSurface, thickness = 1.dp)

                                    DetailItem(
                                        icon = Icons.Default.Savings,
                                        label = "Auto-Savings Intercept Rate",
                                        value = "${state.user.savingsPercentage}% of Income"
                                    )
                                    
                                    DetailItem(
                                        icon = Icons.Default.Notifications,
                                        label = "Smart Reminders",
                                        value = if (viewModel.notificationsEnabled) "Enabled" else "Disabled"
                                    )

                                    if (viewModel.notificationsEnabled) {
                                        DetailItem(
                                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                                            label = "Monthly Budget Limit",
                                            value = "LKR %,.0f".format(viewModel.budgetThreshold)
                                        )
                                        DetailItem(
                                            icon = Icons.Default.AttachMoney,
                                            label = "Large Transaction Warning Limit",
                                            value = "LKR %,.0f".format(viewModel.largeTransactionThreshold)
                                        )
                                        DetailItem(
                                            icon = Icons.Default.Schedule,
                                            label = "Daily Spending Summary Time",
                                            value = "%02d:%02d".format(viewModel.dailySummaryHour, viewModel.dailySummaryMinute)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Edit Button
                            Button(
                                onClick = { isEditMode = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile & Preferences", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            // ─── EDIT PROFILE MODE ───
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    Text(
                                        text = "Edit Preferences",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    HorizontalDivider(color = DarkSurface, thickness = 1.dp)

                                    // Name Field
                                    OutlinedTextField(
                                        value = editedName,
                                        onValueChange = { editedName = it },
                                        label = { Text("Display Name", color = TextSecondary) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentPurple,
                                            unfocusedBorderColor = TextSecondary,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    // Currency Selector Chips
                                    Text(
                                        text = "Base Currency",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val currencies = listOf("LKR", "USD", "EUR", "GBP")
                                        currencies.forEach { currency ->
                                            val isSelected = editedCurrency == currency
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isSelected) AccentPurple.copy(alpha = 0.2f)
                                                        else DarkSurface
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) AccentPurple else Color.Transparent,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { editedCurrency = currency },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = currency,
                                                    color = if (isSelected) AccentPurple else TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Smart Savings Rate Selector Chips
                                    Text(
                                        text = "Auto-Savings Intercept Rate",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val percentages = listOf(10, 20, 30, 40, 50)
                                        percentages.forEach { pct ->
                                            val isSelected = editedSavingsPercentage == pct
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isSelected) AccentOrange.copy(alpha = 0.2f)
                                                        else DarkSurface
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) AccentOrange else Color.Transparent,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { editedSavingsPercentage = pct },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$pct%",
                                                    color = if (isSelected) AccentOrange else TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Push notification switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Smart Savings Alerts",
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Prompt routing actions when income arrives",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Switch(
                                            checked = editedNotifications,
                                            onCheckedChange = { editedNotifications = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = AccentPurple,
                                                checkedTrackColor = AccentPurple.copy(alpha = 0.4f),
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DarkSurface
                                            )
                                        )
                                    }

                                    if (editedNotifications) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Notification Settings",
                                            color = TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        // Monthly Budget Limit Field
                                        OutlinedTextField(
                                            value = editedBudgetThreshold,
                                            onValueChange = { editedBudgetThreshold = it.filter { char -> char.isDigit() } },
                                            label = { Text("Monthly Budget Limit (LKR)", color = TextSecondary) },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentPurple,
                                                unfocusedBorderColor = TextSecondary,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        // Large Transaction Warning Limit Field
                                        OutlinedTextField(
                                            value = editedLargeTransactionThreshold,
                                            onValueChange = { editedLargeTransactionThreshold = it.filter { char -> char.isDigit() } },
                                            label = { Text("Large Transaction Limit (LKR)", color = TextSecondary) },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentPurple,
                                                unfocusedBorderColor = TextSecondary,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        // Daily Reminder Timing Hour and Minute Input
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editedDailySummaryHour,
                                                onValueChange = { input ->
                                                    val hourVal = input.filter { it.isDigit() }.toIntOrNull()
                                                    if (hourVal == null || hourVal in 0..23) {
                                                        editedDailySummaryHour = input.filter { it.isDigit() }.take(2)
                                                    }
                                                },
                                                label = { Text("Reminder Hour (0-23)", color = TextSecondary) },
                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                                ),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AccentPurple,
                                                    unfocusedBorderColor = TextSecondary,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )

                                            OutlinedTextField(
                                                value = editedDailySummaryMinute,
                                                onValueChange = { input ->
                                                    val minVal = input.filter { it.isDigit() }.toIntOrNull()
                                                    if (minVal == null || minVal in 0..59) {
                                                        editedDailySummaryMinute = input.filter { it.isDigit() }.take(2)
                                                    }
                                                },
                                                label = { Text("Minute (0-59)", color = TextSecondary) },
                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                                ),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = AccentPurple,
                                                    unfocusedBorderColor = TextSecondary,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }

                            // Inline Save / Cancel buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { isEditMode = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)
                                ) {
                                    Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                                
                                Button(
                                    onClick = {
                                        if (editedName.isNotEmpty()) {
                                            val budget = editedBudgetThreshold.toDoubleOrNull() ?: viewModel.budgetThreshold
                                            val largeTx = editedLargeTransactionThreshold.toDoubleOrNull() ?: viewModel.largeTransactionThreshold
                                            val hour = editedDailySummaryHour.toIntOrNull() ?: viewModel.dailySummaryHour
                                            val minute = editedDailySummaryMinute.toIntOrNull() ?: viewModel.dailySummaryMinute

                                             viewModel.updateUserProfile(
                                                 name = editedName,
                                                 baseCurrency = editedCurrency,
                                                 savingsPercentage = editedSavingsPercentage,
                                                 notificationsEnabled = editedNotifications,
                                                 budget = budget,
                                                 largeTx = largeTx,
                                                 hour = hour,
                                                 minute = minute
                                             )
                                             isEditMode = false
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save Changes",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Logout Button (placed at the bottom)
                        Button(
                            onClick = {
                                viewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.85f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.size(18.dp)
                                
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
                is ProfileState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = ExpenseRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = TextSecondary,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadUserProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}