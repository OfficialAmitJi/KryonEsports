package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InGameNotificationEntity
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    notifications: List<InGameNotificationEntity>,
    onMarkAllRead: () -> Unit
) {
    var streakAlertsEnabled by remember { mutableStateOf(true) }
    var partyInvitesEnabled by remember { mutableStateOf(true) }
    var communityMilestonesEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Notification Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "NOTIFICATION & PUSH ALERT PREFERENCES",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NotificationSettingRow(
                        title = "Daily Streak Protection & Reminders",
                        description = "Alert before 24h streak reset",
                        checked = streakAlertsEnabled,
                        onCheckedChange = { streakAlertsEnabled = it },
                        tag = "switch_streak_alerts"
                    )

                    NotificationSettingRow(
                        title = "Cross-Play Friend Match Invites",
                        description = "Notify when friends invite you to arena",
                        checked = partyInvitesEnabled,
                        onCheckedChange = { partyInvitesEnabled = it },
                        tag = "switch_party_alerts"
                    )

                    NotificationSettingRow(
                        title = "Community Challenge Milestones",
                        description = "Notify when collective rewards unlock",
                        checked = communityMilestonesEnabled,
                        onCheckedChange = { communityMilestonesEnabled = it },
                        tag = "switch_community_alerts"
                    )
                }
            }
        }

        // Header with Mark All as Read
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMUNITY ALERTS & INBOX",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = onMarkAllRead,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_mark_all_read")
                ) {
                    Text("MARK ALL READ", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Notification Items
        items(notifications) { notif ->
            NotificationCard(notif = notif)
        }
    }
}

@Composable
private fun NotificationCard(notif: InGameNotificationEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.isRead) SpaceCard else SpaceCardLight
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (notif.category) {
                            "EVENT" -> CosmicAmber.copy(alpha = 0.2f)
                            "CHALLENGE" -> CyberCyan.copy(alpha = 0.2f)
                            else -> NeonMagenta.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notif.category) {
                        "EVENT" -> Icons.Default.Campaign
                        "CHALLENGE" -> Icons.Default.Stars
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (notif.category) {
                        "EVENT" -> CosmicAmber
                        "CHALLENGE" -> CyberCyan
                        else -> NeonMagentaLight
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notif.title,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (!notif.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notif.message,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = description, color = TextMuted, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberCyan,
                checkedTrackColor = CyberCyanDark
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
