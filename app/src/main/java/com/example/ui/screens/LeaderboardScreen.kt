package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardPlayer
import com.example.ui.components.CrossPlayPlatformBadge
import com.example.ui.theme.*

@Composable
fun LeaderboardScreen(
    players: List<LeaderboardPlayer>,
    userRank: Int = 6
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Global, 1: Seasonal, 2: Friends

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Season Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CosmicAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SEASON 4: CYBER RIFT",
                                color = TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Ends in 12 days • Tier Apex Grandmaster active",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CosmicAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "GLOBAL LADDER",
                            color = CosmicAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Leaderboard Tabs
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
                    text = { Text("GLOBAL TOP 100", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_lead_global")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("SEASONAL MMR", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_lead_seasonal")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("FRIENDS & CLAN", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_lead_friends")
                )
            }
        }

        // Top 3 Podium
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val top3 = players.take(3)
                if (top3.size >= 3) {
                    // 2nd Place
                    PodiumCard(
                        player = top3[1],
                        place = 2,
                        podiumColor = Color(0xFFB0BEC5),
                        height = 110.dp,
                        modifier = Modifier.weight(1f)
                    )
                    // 1st Place
                    PodiumCard(
                        player = top3[0],
                        place = 1,
                        podiumColor = CosmicAmber,
                        height = 135.dp,
                        modifier = Modifier.weight(1f)
                    )
                    // 3rd Place
                    PodiumCard(
                        player = top3[2],
                        place = 3,
                        podiumColor = Color(0xFFCD7F32),
                        height = 95.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "ALL RANKED PLAYERS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Player Rankings List
        items(players) { player ->
            PlayerRankItem(player = player)
        }
    }
}

@Composable
private fun PodiumCard(
    player: LeaderboardPlayer,
    place: Int,
    podiumColor: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(height)
            .border(1.dp, podiumColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SpaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(podiumColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$place",
                    color = VoidDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = player.displayName,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1
            )
            Text(
                text = "${player.score}",
                color = podiumColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            CrossPlayPlatformBadge(player.platform)
        }
    }
}

@Composable
private fun PlayerRankItem(player: LeaderboardPlayer) {
    val isUser = player.isUser
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isUser) Modifier.border(1.5.dp, CyberCyan, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) SpaceCardLight else SpaceCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isUser) CyberCyan else VoidDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${player.rank}",
                    color = if (isUser) VoidDark else TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.displayName,
                        color = if (isUser) CyberCyan else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CrossPlayPlatformBadge(player.platform)
                }
                Text(
                    text = "${player.title} • ${player.mmr} MMR",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // High Score
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${player.score}",
                    color = CosmicAmber,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = player.tier,
                    color = NeonMagentaLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
