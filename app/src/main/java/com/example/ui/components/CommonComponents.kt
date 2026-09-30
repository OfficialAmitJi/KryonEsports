package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.AppScreen
import com.example.ui.theme.*

@Composable
fun CosmoTopAppBar(
    title: String,
    quantumShards: Int,
    energyCores: Int,
    unreadNotifications: Int,
    canNavigateBack: Boolean = false,
    onBackClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Surface(
        color = VoidDark,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canNavigateBack) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("nav_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = TextWhite
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quantum Shards pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceCard)
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💎", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$quantumShards",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyberCyan
                    )
                }

                // Energy Cores pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpaceCard)
                        .border(1.dp, CosmicAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$energyCores",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CosmicAmber
                    )
                }

                // Public Share Button
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SpaceCard)
                        .minimumInteractiveComponentSize()
                        .testTag("app_bar_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Game",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Notification Bell
                Box {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SpaceCard)
                            .minimumInteractiveComponentSize()
                            .testTag("notification_bell_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadNotifications > 0) CosmicAmber else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (unreadNotifications > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(DangerRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$unreadNotifications",
                                color = TextWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CosmoBottomNavigation(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = VoidDark,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.fillMaxWidth()
    ) {
        val navItems = listOf(
            Triple(AppScreen.HOME, "Arena", Icons.Default.RocketLaunch),
            Triple(AppScreen.MULTIPLAYER_LOBBY, "Match", Icons.Default.SportsEsports),
            Triple(AppScreen.LEADERBOARD, "Ranks", Icons.Default.Leaderboard),
            Triple(AppScreen.DAILY_CHALLENGES, "Quests", Icons.Default.Stars),
            Triple(AppScreen.COMMS_CHAT, "Comms", Icons.Default.Forum),
            Triple(AppScreen.PROFILE_HANGAR, "Hangar", Icons.Default.Person)
        )

        navItems.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VoidDark,
                    selectedTextColor = CyberCyan,
                    indicatorColor = CyberCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}

@Composable
fun CrossPlayPlatformBadge(platform: String) {
    val (bgColor, textColor, icon) = when (platform.uppercase()) {
        "PC" -> Triple(Color(0xFF1E88E5), TextWhite, "💻")
        "IOS" -> Triple(Color(0xFF546E7A), TextWhite, "🍏")
        "ANDROID" -> Triple(Color(0xFF43A047), TextWhite, "🤖")
        "XBOX" -> Triple(Color(0xFF2E7D32), TextWhite, "🎮")
        "PLAYSTATION" -> Triple(Color(0xFF1565C0), TextWhite, "⚡")
        "SWITCH" -> Triple(Color(0xFFE53935), TextWhite, "🕹️")
        else -> Triple(SpaceBorder, TextMuted, "🌐")
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor.copy(alpha = 0.25f))
            .border(1.dp, bgColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = platform,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModerationReportDialog(
    targetMessage: ChatMessageEntity,
    onDismiss: () -> Unit,
    onSubmitReport: (String) -> Unit
) {
    val reasons = listOf(
        "Harassment / Hate Speech",
        "Spam / Phishing Links",
        "Offensive / Inappropriate Name",
        "Cheating / Exploits Mention"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpaceCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = "Report",
                    tint = DangerRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Player & Message",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Flagged User: ${targetMessage.senderName} (${targetMessage.senderPlatform})",
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${targetMessage.text}\"",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VoidDark, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select violation category:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                reasons.forEach { reason ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSubmitReport(reason) }
                            .testTag("report_reason_${reason.take(5).lowercase()}"),
                        colors = CardDefaults.cardColors(containerColor = SpaceCardLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = reason, color = TextWhite, fontSize = 12.sp)
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("dismiss_report_button")
            ) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
