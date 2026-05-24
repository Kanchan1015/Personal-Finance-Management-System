package com.example.pbd.ui.screens.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbd.data.model.NotificationEntity
import com.example.pbd.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Colour palette (matches app-wide dark theme) ──────────────────────────────
private val BgDark        = Color(0xFF0D0F1A)
private val CardDark      = Color(0xFF161929)
private val DarkSurface   = Color(0xFF1E2235)
private val AccentPurple  = Color(0xFF7B61FF)
private val AccentBlue    = Color(0xFF4FC3F7)
private val AccentGreen   = Color(0xFF4CAF50)
private val AccentOrange  = Color(0xFFFF9800)
private val AccentRed     = Color(0xFFEF5350)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFB0B8D0)

// ── Notification type → UI metadata ──────────────────────────────────────────
private data class NotifStyle(
    val icon: ImageVector,
    val accentColor: Color,
    val label: String
)

private fun styleFor(type: String): NotifStyle = when (type) {
    "BUDGET_ALERT"     -> NotifStyle(Icons.Default.Warning,        AccentRed,    "Budget Alert")
    "LARGE_TRANSACTION"-> NotifStyle(Icons.Default.MonetizationOn, AccentOrange, "Large Transaction")
    "RECURRING_AUTO"   -> NotifStyle(Icons.Default.Repeat,         AccentBlue,   "Auto-Logged")
    "GOAL_MILESTONE"   -> NotifStyle(Icons.Default.EmojiEvents,    AccentGreen,  "Goal Milestone")
    "GOAL_DEADLINE"    -> NotifStyle(Icons.Default.Timer,          AccentOrange, "Goal Deadline")
    "DAILY_SUMMARY"    -> NotifStyle(Icons.Default.BarChart,       AccentPurple, "Daily Summary")
    "WEEKLY_REPORT"    -> NotifStyle(Icons.Default.CalendarMonth,  AccentBlue,   "Weekly Report")
    else               -> NotifStyle(Icons.Default.Notifications,  TextSecondary,"Notification")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    navController: NavHostController,
    viewModel: NotificationViewModel = koinViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    // Group by date label
    val grouped = remember(notifications) {
        notifications.groupBy { notif ->
            val cal = Calendar.getInstance().apply { timeInMillis = notif.timestamp }
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            when {
                cal.get(Calendar.YEAR)       == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR)== today.get(Calendar.DAY_OF_YEAR) -> "Today"

                cal.get(Calendar.YEAR)       == yesterday.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR)== yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

                else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(notif.timestamp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notification Center",
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
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Notification Settings",
                            tint = TextPrimary
                        )
                    }
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text(
                                "Mark all read",
                                color = AccentPurple,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CardDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "No Notifications Yet",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "You're all caught up! Notifications\nwill appear here as they arrive.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                grouped.forEach { (dateLabel, notifList) ->
                    item {
                        Text(
                            text = dateLabel,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                    }
                    items(notifList, key = { it.id }) { notif ->
                        NotificationCard(
                            notif = notif,
                            onTap = { viewModel.markAsRead(notif.id) },
                            onDismiss = { viewModel.deleteNotification(notif.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notif: NotificationEntity,
    onTap: () -> Unit,
    onDismiss: () -> Unit
) {
    val style = styleFor(notif.type)
    val timeStr = remember(notif.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(notif.timestamp))
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentRed.copy(alpha = 0.7f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTap() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (notif.isRead) CardDark else CardDark.copy(alpha = 0.95f)
            ),
            border = if (!notif.isRead)
                androidx.compose.foundation.BorderStroke(1.dp, style.accentColor.copy(alpha = 0.4f))
            else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(style.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = style.label,
                            color = style.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = timeStr,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = notif.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = notif.message,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                // Unread indicator dot
                if (!notif.isRead) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AccentPurple, AccentBlue))
                            )
                    )
                }
            }
        }
    }
}
