package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String = "CR-7829-X",
    val displayName: String = "NovaPilot",
    val title: String = "Apex Pilot",
    val avatarEmoji: String = "🚀",
    val shipSkinId: String = "apex_phoenix",
    val trailFxId: String = "plasma_cyan",
    val level: Int = 14,
    val currentXp: Int = 3450,
    val maxXp: Int = 5000,
    val rankMmr: Int = 2140,
    val rankTier: String = "Diamond II",
    val energyCores: Int = 320,
    val quantumShards: Int = 1850,
    val totalMatches: Int = 86,
    val wins: Int = 54,
    val highScore: Long = 84250L,
    val streakDays: Int = 5,
    val streakClaimedToday: Boolean = false,
    val streakFreezeCores: Int = 2,
    val streakMultiplier: Float = 1.5f,
    val streakShieldActive: Boolean = true,
    val lastClaimDateString: String = "",
    val lastLoginEpoch: Long = System.currentTimeMillis(),
    val crossPlayId: String = "CROSS-PC-9921",
    val isCrossPlayLinked: Boolean = true,
    val voiceChatEnabled: Boolean = true,
    val voiceMuted: Boolean = false,
    val voiceNoiseSuppression: Boolean = true
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: String,
    val gameMode: String, // "Endless Odyssey", "Multiplayer Arena", "Daily Challenge"
    val score: Long,
    val placement: Int, // 1 for 1st place, etc.
    val kills: Int,
    val survivalTimeSec: Int,
    val opponentsSummary: String,
    val shardsEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val messageId: Long = 0,
    val channel: String, // "GLOBAL", "ARENA", "CLAN", "PRIVATE"
    val senderId: String,
    val senderName: String,
    val senderTitle: String,
    val senderPlatform: String, // "Android", "PC", "iOS", "PlayStation", "Xbox"
    val avatarEmoji: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false,
    val isFlagged: Boolean = false
)

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val challengeId: String,
    val title: String,
    val description: String,
    val rewardShards: Int,
    val rewardTitle: String?,
    val targetCount: Int,
    val currentCount: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val isCommunityGoal: Boolean = false
)

@Entity(tableName = "notifications")
data class InGameNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val category: String, // "EVENT", "MATCH", "CHALLENGE", "COMMUNITY"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionPayload: String? = null
)

// Data classes for cosmetics & leaderboards
data class CosmeticItem(
    val id: String,
    val name: String,
    val category: String, // "SHIP", "TRAIL", "TITLE"
    val description: String,
    val rarity: String, // "Rare", "Epic", "Legendary"
    val previewEmoji: String,
    val shardCost: Int,
    val isUnlocked: Boolean = false
)

data class LeaderboardPlayer(
    val rank: Int,
    val playerId: String,
    val displayName: String,
    val title: String,
    val platform: String, // "PC", "iOS", "Android", "PlayStation", "Xbox"
    val tier: String,
    val score: Long,
    val mmr: Int,
    val isUser: Boolean = false
)

data class StreakRewardTier(
    val day: Int,
    val shards: Int,
    val cores: Int,
    val multiplier: Float,
    val multiplierText: String,
    val bonusItem: String? = null,
    val isCosmeticReward: Boolean = false,
    val iconEmoji: String = "💎"
)
