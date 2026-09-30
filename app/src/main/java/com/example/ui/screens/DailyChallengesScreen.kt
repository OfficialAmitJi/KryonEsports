package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyChallengeEntity
import com.example.data.model.StreakRewardTier
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.*

@Composable
fun DailyChallengesScreen(
    challenges: List<DailyChallengeEntity>,
    user: UserProfileEntity?,
    streakTiers: List<StreakRewardTier>,
    claimedRewardCelebration: StreakRewardTier?,
    onClaimStreakReward: () -> Unit,
    onBuyStreakShield: () -> Unit,
    onSimulateAdvanceDay: () -> Unit,
    onDismissCelebration: () -> Unit,
    onClaimChallenge: (DailyChallengeEntity) -> Unit
) {
    val communityGoal = challenges.firstOrNull { it.isCommunityGoal }
    val dailyQuests = challenges.filter { !it.isCommunityGoal }

    val streakDays = user?.streakDays ?: 5
    val streakClaimedToday = user?.streakClaimedToday ?: false
    val streakMultiplier = user?.streakMultiplier ?: 1.5f
    val streakFreezeCores = user?.streakFreezeCores ?: 2

    // Flame pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    var inspectTierModal by remember { mutableStateOf<StreakRewardTier?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Daily Streak Hero Tracking Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CosmicAmber.copy(alpha = 0.7f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔥",
                                fontSize = 32.sp,
                                modifier = Modifier.scale(if (!streakClaimedToday) flameScale else 1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$streakDays DAY STREAK",
                                        color = TextWhite,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Text(
                                    text = if (streakClaimedToday) "Today's reward claimed ✓" else "Consecutive login reward ready!",
                                    color = if (streakClaimedToday) SuccessGreen else CosmicAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Streak Freeze Shields Count
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoidDark)
                                .border(1.dp, SpaceBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛡️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streakFreezeCores Shields",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Community Multiplier Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicAmber.copy(alpha = 0.15f))
                            .border(1.dp, CosmicAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = CosmicAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STREAK BUFF: ${streakMultiplier}x Community Quest & Shard Multiplier Active!",
                            color = CosmicAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Main Claim Streak Reward Button or Claimed Status
                    if (!streakClaimedToday) {
                        val currentTierIndex = ((streakDays - 1) % 7).coerceIn(0, 6)
                        val currentReward = streakTiers.getOrNull(currentTierIndex)
                        Button(
                            onClick = onClaimStreakReward,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_claim_daily_streak"),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, tint = VoidDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLAIM DAY $streakDays REWARD (+${currentReward?.shards ?: 500} 💎, +${currentReward?.cores ?: 50} ⚡)",
                                color = VoidDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoidDark)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Streak Preserved for Today!",
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Next reward in 08h 35m",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action tools: Buy Shield & Advance Day (Simulation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBuyStreakShield,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_buy_streak_shield"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(CyberCyan, CyberCyanDark))
                            )
                        ) {
                            Text("🛡️ +1 Shield (300 💎)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSimulateAdvanceDay,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("btn_simulate_advance_day"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceCardLight)
                        ) {
                            Icon(imageVector = Icons.Default.FastForward, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Next Day", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // 7-Day Visual Calendar / Progressive Streak Track
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "7-DAY PROGRESSIVE STREAK REWARDS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tap for reward details",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(streakTiers) { tier ->
                        val isClaimed = tier.day < streakDays || (tier.day == streakDays && streakClaimedToday)
                        val isToday = tier.day == streakDays && !streakClaimedToday
                        val isGrandDay = tier.day == 7

                        Card(
                            modifier = Modifier
                                .width(94.dp)
                                .height(125.dp)
                                .then(
                                    if (isToday) Modifier.border(1.5.dp, CosmicAmber, RoundedCornerShape(12.dp))
                                    else if (isGrandDay) Modifier.border(1.dp, NeonMagenta.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                                .clickable { inspectTierModal = tier }
                                .testTag("streak_calendar_day_${tier.day}"),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isToday -> SpaceCardLight
                                    isClaimed -> SpaceCard.copy(alpha = 0.6f)
                                    else -> SpaceCard
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Day Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "DAY ${tier.day}",
                                        color = if (isToday) CosmicAmber else TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    if (isClaimed) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Claimed",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // Icon
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isClaimed -> SuccessGreen.copy(alpha = 0.15f)
                                                isToday -> CosmicAmber.copy(alpha = 0.2f)
                                                isGrandDay -> NeonMagenta.copy(alpha = 0.2f)
                                                else -> VoidDark
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = tier.iconEmoji, fontSize = 20.sp)
                                }

                                // Reward Label
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "+${tier.shards}",
                                        color = CyberCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${tier.multiplier}x Buff",
                                        color = if (isGrandDay) NeonMagentaLight else TextMuted,
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

        // Global Community Challenge Card (Integrated with streak progress)
        if (communityGoal != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
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
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GLOBAL COMMUNITY EVENT",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldMatrix.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "STREAK BOOST +25K",
                                    color = EmeraldMatrix,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = communityGoal.title,
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = communityGoal.description,
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val progress = (communityGoal.currentCount.toFloat() / communityGoal.targetCount).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${communityGoal.currentCount.toFormattedString()} / ${communityGoal.targetCount.toFormattedString()}",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% COMPLETED",
                                color = CosmicAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyberCyan,
                            trackColor = SpaceBorder
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(VoidDark)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Community Reward: ${communityGoal.rewardTitle}",
                                    color = CosmicAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "+${communityGoal.rewardShards} Shards",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Daily Quests with Streak Multiplier Indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DAILY COMMUNITY QUESTS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CosmicAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${streakMultiplier}x STREAK MULTIPLIER",
                            color = CosmicAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Text(
                    text = "Resets in 08h 35m",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Daily Quest Cards (showing boosted rewards!)
        items(dailyQuests) { quest ->
            DailyQuestCard(
                quest = quest,
                streakMultiplier = streakMultiplier,
                onClaim = { onClaimChallenge(quest) }
            )
        }
    }

    // Modal: Inspect Day Streak Reward
    inspectTierModal?.let { tier ->
        AlertDialog(
            onDismissRequest = { inspectTierModal = null },
            containerColor = SpaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Day ${tier.day} Streak Reward",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RewardDetailRow("Quantum Shards", "+${tier.shards} 💎", CyberCyan)
                    RewardDetailRow("Energy Cores", "+${tier.cores} ⚡", CosmicAmber)
                    RewardDetailRow("Community Multiplier", "${tier.multiplier}x Boost", EmeraldMatrix)
                    if (tier.bonusItem != null) {
                        RewardDetailRow("Exclusive Bonus", tier.bonusItem, NeonMagentaLight)
                    }
                    RewardDetailRow("Community Contribution", "+25,000 Cosmic Orbs", CyberCyan)
                }
            },
            confirmButton = {
                Button(
                    onClick = { inspectTierModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_close_inspect_tier")
                ) {
                    Text("OK", color = VoidDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal: Streak Reward Celebration Modal
    claimedRewardCelebration?.let { reward ->
        AlertDialog(
            onDismissRequest = onDismissCelebration,
            containerColor = SpaceCard,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 STREAK REWARD UNLOCKED! 🔥",
                        color = CosmicAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "DAY ${reward.day} CONSECUTIVE LOGIN",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(reward.iconEmoji, fontSize = 48.sp)
                    RewardDetailRow("Quantum Shards", "+${reward.shards} 💎", CyberCyan)
                    RewardDetailRow("Energy Cores", "+${reward.cores} ⚡", CosmicAmber)
                    RewardDetailRow("Active Community Buff", "${reward.multiplier}x Multiplier", EmeraldMatrix)
                    if (reward.bonusItem != null) {
                        RewardDetailRow("Special Item", reward.bonusItem, NeonMagentaLight)
                    }
                    RewardDetailRow("Community Progress", "+25,000 Orbs Added", SuccessGreen)
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissCelebration,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("btn_dismiss_streak_celebration")
                ) {
                    Text("CLAIM & CONTINUE", color = VoidDark, fontWeight = FontWeight.Black)
                }
            }
        )
    }
}

@Composable
private fun RewardDetailRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(VoidDark)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DailyQuestCard(
    quest: DailyChallengeEntity,
    streakMultiplier: Float,
    onClaim: () -> Unit
) {
    val boostedShards = (quest.rewardShards * streakMultiplier).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.title,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+$boostedShards",
                        color = CosmicAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (streakMultiplier > 1.0f) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(Base ${quest.rewardShards})",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quest.description,
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            val progress = (quest.currentCount.toFloat() / quest.targetCount).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${quest.currentCount} / ${quest.targetCount}",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (quest.isClaimed) {
                    Text(
                        text = "CLAIMED ✓",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (quest.isCompleted) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("btn_claim_quest_${quest.challengeId}")
                    ) {
                        Text("CLAIM REWARD", color = VoidDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else {
                    Text(
                        text = "IN PROGRESS",
                        color = CosmicAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (quest.isCompleted) SuccessGreen else CyberCyan,
                trackColor = SpaceBorder
            )
        }
    }
}

private fun Int.toFormattedString(): String {
    return String.format("%,d", this)
}
