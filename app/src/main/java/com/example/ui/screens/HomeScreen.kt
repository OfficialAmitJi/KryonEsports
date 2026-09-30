package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProfileEntity
import com.example.ui.AppScreen
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    user: UserProfileEntity?,
    onStartGame: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCard)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.arena_hero_banner),
                        contentDescription = "Cosmo Arena Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        VoidDark.copy(alpha = 0.75f),
                                        VoidDark.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Banner Content
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CROSS-PLAY LIVE • 142k ONLINE",
                                color = EmeraldMatrix,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "SEASON 4: CYBER RIFT",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = TextWhite
                        )
                        Text(
                            text = "Global competitive arena across PC, Console & Mobile",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Profile Quick Stats Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppScreen.PROFILE_HANGAR) }
                    .testTag("home_profile_card"),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(CyberCyan, NeonMagenta)))
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(VoidDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.avatarEmoji ?: "🚀",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.displayName ?: "NovaPilot",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LVL ${user?.level ?: 14}",
                                color = CosmicAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = user?.title ?: "Apex Pilot",
                            color = CyberCyan,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // XP Bar
                        val xpProgress = ((user?.currentXp ?: 3450).toFloat() / (user?.maxXp ?: 5000)).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonMagenta,
                            trackColor = SpaceBorder
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = user?.rankTier ?: "Diamond II",
                            color = NeonMagentaLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${user?.rankMmr ?: 2140} MMR",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Daily Streak Quick Tracker Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CosmicAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onNavigate(AppScreen.DAILY_CHALLENGES) }
                    .testTag("home_streak_banner"),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${user?.streakDays ?: 5} DAY STREAK",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CosmicAmber.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${user?.streakMultiplier ?: 1.5f}X COMMUNITY BOOST",
                                        color = CosmicAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = if (user?.streakClaimedToday == true) "Today's reward claimed ✓" else "Tap to claim today's reward!",
                                color = if (user?.streakClaimedToday == true) SuccessGreen else CyberCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Streak",
                        tint = CosmicAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Section Title: Game Modes
        item {
            Text(
                text = "SELECT ARENA MODE",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = TextMuted
            )
        }

        // Mode 1: Multiplayer Arena
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NeonMagenta.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .clickable { onStartGame("Multiplayer Arena") }
                    .testTag("mode_multiplayer_card"),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(16.dp)
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
                            Text("⚔️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Multiplayer Arena",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonMagenta.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "4P CROSS-PLAY",
                                color = NeonMagentaLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Face off against live cross-platform opponents. Real-time ranking, tactical pings, and built-in voice chat.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onStartGame("Multiplayer Arena") },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_play_multiplayer"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLAY ARENA", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.MULTIPLAYER_LOBBY) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("btn_invite_friends"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(CyberCyan, CyberCyanDark))),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.GroupAdd, contentDescription = "Party")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PARTY")
                        }
                    }
                }
            }
        }

        // Mode 2: Endless Odyssey
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { onStartGame("Endless Odyssey") }
                    .testTag("mode_endless_card"),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(16.dp)
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
                            Text("🌌", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Endless Odyssey",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "SOLO SURVIVAL",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Infinite asteroid waves, cyber drones, and powerups. Push your personal best and climb the global ladder.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onStartGame("Endless Odyssey") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("btn_play_endless"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = VoidDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LAUNCH ODYSSEY", color = VoidDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Mode 3: Daily Community Challenge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CosmicAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onStartGame("Daily Challenge") }
                    .testTag("mode_daily_challenge_card"),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Challenge: Asteroid Blitz",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Earn 2X Quantum Shards towards the 10M Community Goal.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { onStartGame("Daily Challenge") },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_play_challenge")
                    ) {
                        Text("START", color = VoidDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
