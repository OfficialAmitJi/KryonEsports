package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.game.GameAudioComms
import com.example.ui.components.CrossPlayPlatformBadge
import com.example.ui.theme.*
import com.example.util.ShareHelper

@Composable
fun MultiplayerLobbyScreen(
    user: UserProfileEntity?,
    isMatchmaking: Boolean,
    matchFound: Boolean,
    roomCode: String,
    audioComms: GameAudioComms,
    onStartMatchmaking: () -> Unit,
    onCancelMatchmaking: () -> Unit,
    onRegenerateRoomCode: () -> Unit,
    onLaunchRoomMatch: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ranked Matchmaking, 1: Custom Room & Party
    var showInviteFriendModal by remember { mutableStateOf(false) }
    var slot4Joined by remember { mutableStateOf(false) }

    val isMuted by audioComms.isMuted.collectAsState()
    val noiseSuppression by audioComms.noiseSuppression.collectAsState()
    val waveform by audioComms.liveWaveform.collectAsState()
    val teammates by audioComms.teammates.collectAsState()

    // Radar rotation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Mode Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpaceCard,
                contentColor = CyberCyan,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "RANKED MATCHMAKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.testTag("tab_matchmaking")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "CUSTOM ROOM & PARTY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.testTag("tab_custom_room")
                )
            }
        }

        // TAB 0: Matchmaking Queue
        if (selectedTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Animated Radar or Match Found
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(VoidDark)
                                .border(2.dp, if (matchFound) SuccessGreen else CyberCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isMatchmaking) {
                                // Radar line
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .rotate(radarAngle)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .width(2.dp)
                                            .height(65.dp)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(CyberCyan, Color.Transparent)
                                                )
                                            )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else if (matchFound) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(54.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when {
                                matchFound -> "MATCH FOUND! ENTERING ARENA..."
                                isMatchmaking -> "SEARCHING CROSS-PLATFORM PLAYERS..."
                                else -> "GLOBAL COMPETITIVE QUEUE"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = if (matchFound) SuccessGreen else TextWhite
                        )

                        Text(
                            text = "Matching skill-based MMR (${user?.rankMmr ?: 2140}) across PC, Console & Mobile",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Matchmaking Action Button
                        if (isMatchmaking) {
                            Button(
                                onClick = onCancelMatchmaking,
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_cancel_matchmaking")
                            ) {
                                Text("CANCEL QUEUE", fontWeight = FontWeight.Bold)
                            }
                        } else if (!matchFound) {
                            Button(
                                onClick = onStartMatchmaking,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_start_matchmaking")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = VoidDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FIND MATCH (QUICK QUEUE)", color = VoidDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Server Cluster Latency Stats
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GLOBAL EDGE SERVERS",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LOW LATENCY", color = EmeraldMatrix, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        ServerPingRow("US-East (Virginia)", "14 ms", "Optimal")
                        ServerPingRow("EU-Central (Frankfurt)", "18 ms", "Optimal")
                        ServerPingRow("AP-East (Tokyo)", "24 ms", "Good")
                    }
                }
            }
        }

        // TAB 1: Custom Room & Party
        if (selectedTab == 1) {
            // Room Code & Invite Share Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ROOM CODE (CROSS-PLAY INVITE)",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(VoidDark)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = roomCode,
                                color = CyberCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Row {
                                IconButton(
                                    onClick = onRegenerateRoomCode,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .minimumInteractiveComponentSize()
                                        .testTag("btn_regen_room_code")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "New Code",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        val context = LocalContext.current
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { ShareHelper.copyToClipboard(context, roomCode, "Lobby Code") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_copy_room_code"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(CyberCyan, CyberCyanDark)))
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { ShareHelper.shareLobby(context, roomCode) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_share_lobby_link"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Invite", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Party Members 4 Slots
            item {
                Text(
                    text = "PARTY SLOTS (4 PLAYERS)",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Slot 1: You (Host)
            item {
                PlayerSlotCard(
                    avatar = user?.avatarEmoji ?: "🚀",
                    name = "${user?.displayName ?: "NovaPilot"} (You)",
                    role = "HOST",
                    platform = "Android",
                    status = "READY",
                    statusColor = SuccessGreen
                )
            }

            // Slot 2: Teammate 1
            item {
                PlayerSlotCard(
                    avatar = "🛸",
                    name = "ValkyrieX",
                    role = "PILOT",
                    platform = "PC",
                    status = "READY",
                    statusColor = SuccessGreen
                )
            }

            // Slot 3: Teammate 2
            item {
                PlayerSlotCard(
                    avatar = "⚡",
                    name = "Kira_Kinetix",
                    role = "PILOT",
                    platform = "iOS",
                    status = "READY",
                    statusColor = SuccessGreen
                )
            }

            // Slot 4: Friend Invite or Filled
            item {
                if (slot4Joined) {
                    PlayerSlotCard(
                        avatar = "👑",
                        name = "TitanStrike",
                        role = "PILOT",
                        platform = "Xbox",
                        status = "READY",
                        statusColor = SuccessGreen
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { showInviteFriendModal = true }
                            .testTag("slot_invite_friend_card"),
                        colors = CardDefaults.cardColors(containerColor = SpaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Invite",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INVITE FRIEND DIRECTLY INTO MATCH",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Launch Match Button
            item {
                Button(
                    onClick = onLaunchRoomMatch,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("btn_launch_party_match")
                ) {
                    Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LAUNCH ARENA MATCH",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Voice Chat Coordination & Test Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IN-GAME VOICE COMMS",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Mic Mute Switch
                        IconButton(
                            onClick = { audioComms.toggleMute() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) DangerRed.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f))
                                .minimumInteractiveComponentSize()
                                .testTag("lobby_mic_toggle")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = if (isMuted) DangerRed else CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Waveform Visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(VoidDark)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMuted) "Mic Muted" else "Microphone Live",
                            color = if (isMuted) TextMuted else EmeraldMatrix,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            waveform.forEach { amp ->
                                val barH = (20.dp * amp).coerceIn(4.dp, 20.dp)
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(barH)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(if (isMuted) TextMuted else CyberCyan)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // AI Noise Suppression Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Low-Latency AI Noise Gate",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Switch(
                            checked = noiseSuppression,
                            onCheckedChange = { audioComms.toggleNoiseSuppression() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberCyanDark
                            ),
                            modifier = Modifier.testTag("switch_noise_suppression")
                        )
                    }
                }
            }
        }
    }

    // Direct Friend Invite Modal
    if (showInviteFriendModal) {
        AlertDialog(
            onDismissRequest = { showInviteFriendModal = false },
            containerColor = SpaceCard,
            title = {
                Text(
                    text = "INVITE FRIENDS DIRECTLY",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val friends = listOf(
                        Triple("TitanStrike", "Xbox", "Online - In Hangar"),
                        Triple("ShadowRacer", "Android", "Online - In Lobby"),
                        Triple("AuraGlide", "iOS", "Online - Exploring")
                    )

                    friends.forEach { (name, platform, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SpaceCardLight)
                                .clickable {
                                    slot4Joined = true
                                    showInviteFriendModal = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = status, color = TextMuted, fontSize = 11.sp)
                            }
                            CrossPlayPlatformBadge(platform)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showInviteFriendModal = false },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("btn_close_invite_dialog")
                ) {
                    Text("Close", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun PlayerSlotCard(
    avatar: String,
    name: String,
    role: String,
    platform: String,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VoidDark),
                contentAlignment = Alignment.Center
            ) {
                Text(text = avatar, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    CrossPlayPlatformBadge(platform)
                }
                Text(text = role, color = TextMuted, fontSize = 11.sp)
            }
            Text(
                text = status,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ServerPingRow(region: String, ping: String, quality: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(VoidDark)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = region, color = TextWhite, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = ping, color = EmeraldMatrix, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = quality, color = TextMuted, fontSize = 11.sp)
        }
    }
}
