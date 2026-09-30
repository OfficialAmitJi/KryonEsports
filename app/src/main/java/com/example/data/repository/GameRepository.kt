package com.example.data.repository

import com.example.data.dao.ChallengeDao
import com.example.data.dao.ChatDao
import com.example.data.dao.MatchDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.UserDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CosmeticItem
import com.example.data.model.DailyChallengeEntity
import com.example.data.model.InGameNotificationEntity
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.MatchHistoryEntity
import com.example.data.model.StreakRewardTier
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(
    private val userDao: UserDao,
    private val matchDao: MatchDao,
    private val chatDao: ChatDao,
    private val challengeDao: ChallengeDao,
    private val notificationDao: NotificationDao
) {
    val userProfile: Flow<UserProfileEntity?> = userDao.getUserProfile()
    val matchHistory: Flow<List<MatchHistoryEntity>> = matchDao.getAllMatches()
    val dailyChallenges: Flow<List<DailyChallengeEntity>> = challengeDao.getAllChallenges()
    val notifications: Flow<List<InGameNotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()

    fun getChatMessages(channel: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForChannel(channel)

    suspend fun initializeDefaultsIfNeeded() {
        val existingUser = userDao.getUserProfile().firstOrNull()
        if (existingUser == null) {
            userDao.insertOrUpdateProfile(UserProfileEntity())
        }

        val existingChallenges = challengeDao.getAllChallenges().firstOrNull()
        if (existingChallenges.isNullOrEmpty()) {
            val initialChallenges = listOf(
                DailyChallengeEntity(
                    challengeId = "comm_goal_1",
                    title = "Global Community Blitz",
                    description = "Collect 10,000,000 Cosmic Orbs together across all platforms to unlock Neon Drake ship!",
                    rewardShards = 1500,
                    rewardTitle = "Cyber Drake Vanguard",
                    targetCount = 10000000,
                    currentCount = 7482000,
                    isCompleted = false,
                    isClaimed = false,
                    isCommunityGoal = true
                ),
                DailyChallengeEntity(
                    challengeId = "daily_1",
                    title = "Endless Survivor",
                    description = "Survive 90 seconds in Endless Odyssey without losing shield.",
                    rewardShards = 250,
                    rewardTitle = "Iron Hull",
                    targetCount = 90,
                    currentCount = 45,
                    isCompleted = false,
                    isClaimed = false
                ),
                DailyChallengeEntity(
                    challengeId = "daily_2",
                    title = "Drone Obliterator",
                    description = "Destroy 30 Rogue Cyber Drones in any game mode.",
                    rewardShards = 300,
                    rewardTitle = "EMP Striker",
                    targetCount = 30,
                    currentCount = 18,
                    isCompleted = false,
                    isClaimed = false
                ),
                DailyChallengeEntity(
                    challengeId = "daily_3",
                    title = "Cross-Play Contender",
                    description = "Finish a Multiplayer Arena match with a top 2 placement.",
                    rewardShards = 450,
                    rewardTitle = "Arena Glider",
                    targetCount = 2,
                    currentCount = 1,
                    isCompleted = false,
                    isClaimed = false
                )
            )
            challengeDao.insertChallenges(initialChallenges)
        }

        val existingGlobalChat = chatDao.getMessagesForChannel("GLOBAL").firstOrNull()
        if (existingGlobalChat.isNullOrEmpty()) {
            val seedChat = listOf(
                ChatMessageEntity(
                    channel = "GLOBAL",
                    senderId = "SYS",
                    senderName = "CosmoArena",
                    senderTitle = "Game Server",
                    senderPlatform = "Server",
                    avatarEmoji = "⚡",
                    text = "Welcome to CosmoRush Season 4: Cyber Rift! Cross-play servers live in US, EU & Asia.",
                    timestamp = System.currentTimeMillis() - 600000,
                    isSystem = true
                ),
                ChatMessageEntity(
                    channel = "GLOBAL",
                    senderId = "P_001",
                    senderName = "ValkyrieX",
                    senderTitle = "Grandmaster",
                    senderPlatform = "PC",
                    avatarEmoji = "🛸",
                    text = "Anyone down for 4-player competitive arena? Need one more!",
                    timestamp = System.currentTimeMillis() - 360000
                ),
                ChatMessageEntity(
                    channel = "GLOBAL",
                    senderId = "P_002",
                    senderName = "ZeroG_Pilot",
                    senderTitle = "Diamond III",
                    senderPlatform = "PlayStation",
                    avatarEmoji = "🛡️",
                    text = "Room code #CYBER-8821 is open, voice comms active!",
                    timestamp = System.currentTimeMillis() - 240000
                ),
                ChatMessageEntity(
                    channel = "GLOBAL",
                    senderId = "P_003",
                    senderName = "KiraKinetix",
                    senderTitle = "Master",
                    senderPlatform = "iOS",
                    avatarEmoji = "🚀",
                    text = "The new community boss reward looks insane. Let's push for the 10M goal!",
                    timestamp = System.currentTimeMillis() - 90000
                )
            )
            seedChat.forEach { chatDao.insertMessage(it) }

            // Clan channel seed
            val clanSeed = listOf(
                ChatMessageEntity(
                    channel = "CLAN",
                    senderId = "CLAN_LEADER",
                    senderName = "NebulaCaptain",
                    senderTitle = "Clan Commander",
                    senderPlatform = "PC",
                    avatarEmoji = "👑",
                    text = "[NEON CLAN] Raid tournament starts this Friday! Check your loadouts.",
                    timestamp = System.currentTimeMillis() - 500000
                ),
                ChatMessageEntity(
                    channel = "CLAN",
                    senderId = "P_004",
                    senderName = "Solaris9",
                    senderTitle = "Vanguard",
                    senderPlatform = "Android",
                    avatarEmoji = "🔥",
                    text = "Unlocked the plasma trail yesterday, ready to fly!",
                    timestamp = System.currentTimeMillis() - 120000
                )
            )
            clanSeed.forEach { chatDao.insertMessage(it) }
        }

        val existingNotifications = notificationDao.getAllNotifications().firstOrNull()
        if (existingNotifications.isNullOrEmpty()) {
            val initialNotifications = listOf(
                InGameNotificationEntity(
                    title = "Season 4 Ranked Kickoff!",
                    message = "Cyber Rift season is active! Climb from Diamond to Apex Grandmaster to earn exclusive animated wings.",
                    category = "EVENT",
                    timestamp = System.currentTimeMillis() - 7200000
                ),
                InGameNotificationEntity(
                    title = "Daily Login Streak Day 5",
                    message = "You received 150 Quantum Shards! Return tomorrow for the Epic Starlight Trail.",
                    category = "COMMUNITY",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                InGameNotificationEntity(
                    title = "Community Challenge Update",
                    message = "Global players have reached 74% on the Neon Drake community milestone!",
                    category = "CHALLENGE",
                    timestamp = System.currentTimeMillis() - 1800000
                )
            )
            initialNotifications.forEach { notificationDao.insertNotification(it) }
        }
    }

    suspend fun recordMatch(
        gameMode: String,
        score: Long,
        placement: Int,
        kills: Int,
        survivalTimeSec: Int,
        opponentsSummary: String
    ) {
        val shardsEarned = (score / 100).toInt() + (if (placement == 1) 150 else 50)
        val match = MatchHistoryEntity(
            matchId = "M-${System.currentTimeMillis() % 100000}",
            gameMode = gameMode,
            score = score,
            placement = placement,
            kills = kills,
            survivalTimeSec = survivalTimeSec,
            opponentsSummary = opponentsSummary,
            shardsEarned = shardsEarned
        )
        matchDao.insertMatch(match)

        val user = userDao.getUserProfile().firstOrNull()
        if (user != null) {
            val isWin = if (placement == 1) 1 else 0
            val xpGain = (score / 50).toInt() + 100
            userDao.updateMatchStats(user.userId, score, isWin, xpGain)
            userDao.addCurrency(user.userId, shards = shardsEarned, cores = 10)
        }

        // Progress challenges
        challengeDao.progressChallenge("comm_goal_1", score.toInt())
        if (survivalTimeSec >= 90) {
            challengeDao.progressChallenge("daily_1", survivalTimeSec)
        }
        if (kills > 0) {
            challengeDao.progressChallenge("daily_2", kills)
        }
        if (gameMode.contains("Multiplayer") && placement <= 2) {
            challengeDao.progressChallenge("daily_3", 1)
        }
    }

    suspend fun sendChatMessage(channel: String, text: String, user: UserProfileEntity) {
        val message = ChatMessageEntity(
            channel = channel,
            senderId = user.userId,
            senderName = user.displayName,
            senderTitle = user.title,
            senderPlatform = "Android",
            avatarEmoji = user.avatarEmoji,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)
        chatDao.pruneOldMessages()
    }

    suspend fun reportChatMessage(messageId: Long) {
        chatDao.flagMessage(messageId)
    }

    suspend fun claimChallengeReward(challenge: DailyChallengeEntity) {
        val user = userDao.getUserProfile().firstOrNull()
        if (user != null) {
            val mult = user.streakMultiplier.coerceAtLeast(1.0f)
            val boostedShards = (challenge.rewardShards * mult).toInt()
            val boostedCores = (25 * mult).toInt()
            userDao.addCurrency(user.userId, shards = boostedShards, cores = boostedCores)
            if (challenge.rewardTitle != null) {
                userDao.updateTitle(user.userId, challenge.rewardTitle)
            }
        }
        challengeDao.claimChallenge(challenge.challengeId)
    }

    fun getStreakTiers(): List<StreakRewardTier> {
        return listOf(
            StreakRewardTier(
                day = 1,
                shards = 150,
                cores = 15,
                multiplier = 1.0f,
                multiplierText = "1.0x Quest Base",
                iconEmoji = "💎"
            ),
            StreakRewardTier(
                day = 2,
                shards = 250,
                cores = 25,
                multiplier = 1.15f,
                multiplierText = "1.15x Community Boost",
                iconEmoji = "💎"
            ),
            StreakRewardTier(
                day = 3,
                shards = 400,
                cores = 35,
                multiplier = 1.25f,
                multiplierText = "1.25x Community Boost",
                bonusItem = "1x Streak Shield",
                iconEmoji = "🛡️"
            ),
            StreakRewardTier(
                day = 4,
                shards = 600,
                cores = 50,
                multiplier = 1.35f,
                multiplierText = "1.35x Community Boost",
                bonusItem = "Cosmic Energy Pack",
                iconEmoji = "⚡"
            ),
            StreakRewardTier(
                day = 5,
                shards = 850,
                cores = 75,
                multiplier = 1.5f,
                multiplierText = "1.5x Community Boost",
                bonusItem = "Title: Void Walker",
                iconEmoji = "🎖️"
            ),
            StreakRewardTier(
                day = 6,
                shards = 1200,
                cores = 100,
                multiplier = 1.75f,
                multiplierText = "1.75x Community Boost",
                bonusItem = "2x Streak Shields",
                iconEmoji = "🛡️"
            ),
            StreakRewardTier(
                day = 7,
                shards = 2500,
                cores = 200,
                multiplier = 2.0f,
                multiplierText = "2.0x DOUBLE COMMUNITY REWARD",
                bonusItem = "Legendary Trail: Starlight Stardust",
                isCosmeticReward = true,
                iconEmoji = "✨"
            )
        )
    }

    suspend fun claimStreakReward(): StreakRewardTier? {
        val user = userDao.getUserProfile().firstOrNull() ?: return null
        if (user.streakClaimedToday) return null

        val currentStreak = user.streakDays
        val streakTiers = getStreakTiers()
        val tierIndex = ((currentStreak - 1) % 7).coerceIn(0, 6)
        val reward = streakTiers[tierIndex]

        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        userDao.claimStreakReward(
            userId = user.userId,
            shards = reward.shards,
            cores = reward.cores,
            newStreak = currentStreak,
            multiplier = reward.multiplier,
            claimDate = todayDate
        )

        if (reward.bonusItem?.contains("Void Walker") == true) {
            userDao.updateTitle(user.userId, "Void Walker")
        }
        if (reward.isCosmeticReward) {
            userDao.updateTrailFx(user.userId, "cosmic_gold")
        }
        if (reward.bonusItem?.contains("Streak Shield") == true) {
            userDao.buyStreakFreeze(user.userId, 0)
        }

        // Directly boost the community challenge collective goal
        challengeDao.progressChallenge("comm_goal_1", 25000)

        notificationDao.insertNotification(
            InGameNotificationEntity(
                title = "🔥 Day $currentStreak Streak Claimed!",
                message = "Earned ${reward.shards} Shards, ${reward.cores} Cores & activated ${reward.multiplierText} for all Community Quests!",
                category = "COMMUNITY",
                timestamp = System.currentTimeMillis()
            )
        )

        return reward
    }

    suspend fun buyStreakShield(): Boolean {
        val user = userDao.getUserProfile().firstOrNull() ?: return false
        val cost = 300
        if (user.quantumShards >= cost) {
            userDao.buyStreakFreeze(user.userId, cost)
            notificationDao.insertNotification(
                InGameNotificationEntity(
                    title = "🛡️ Streak Shield Purchased",
                    message = "Your streak is protected if you miss a day.",
                    category = "COMMUNITY",
                    timestamp = System.currentTimeMillis()
                )
            )
            return true
        }
        return false
    }

    suspend fun simulateAdvanceDay() {
        val user = userDao.getUserProfile().firstOrNull() ?: return
        userDao.simulateAdvanceDay(user.userId)
        notificationDao.insertNotification(
            InGameNotificationEntity(
                title = "🌅 New Solar Day Active!",
                message = "Day ${user.streakDays + 1} daily streak reward is ready to claim!",
                category = "COMMUNITY",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun equipShipSkin(skinId: String) {
        val user = userDao.getUserProfile().firstOrNull() ?: return
        userDao.updateShipSkin(user.userId, skinId)
    }

    suspend fun equipTrail(trailId: String) {
        val user = userDao.getUserProfile().firstOrNull() ?: return
        userDao.updateTrailFx(user.userId, trailId)
    }

    suspend fun equipTitle(title: String) {
        val user = userDao.getUserProfile().firstOrNull() ?: return
        userDao.updateTitle(user.userId, title)
    }

    suspend fun updateDisplayName(name: String) {
        val user = userDao.getUserProfile().firstOrNull() ?: return
        userDao.updateDisplayName(user.userId, name)
    }

    suspend fun setVoiceMuted(muted: Boolean) {
        val user = userDao.getUserProfile().firstOrNull() ?: return
        userDao.setVoiceMuted(user.userId, muted)
    }

    suspend fun setVoiceNoiseSuppression(enabled: Boolean) {
        val user = userDao.getUserProfile().firstOrNull() ?: return
        userDao.setNoiseSuppression(user.userId, enabled)
    }

    suspend fun markNotificationRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
    }

    fun getCosmetics(): List<CosmeticItem> {
        return listOf(
            CosmeticItem(
                id = "apex_phoenix",
                name = "Apex Phoenix",
                category = "SHIP",
                description = "Aerodynamic hyper-hull with dual plasma wings and crimson exhaust.",
                rarity = "Legendary",
                previewEmoji = "🚀",
                shardCost = 0,
                isUnlocked = true
            ),
            CosmeticItem(
                id = "void_stalker",
                name = "Void Stalker",
                category = "SHIP",
                description = "Stealth obsidian armor with dark matter thrusters.",
                rarity = "Epic",
                previewEmoji = "🛸",
                shardCost = 800,
                isUnlocked = true
            ),
            CosmeticItem(
                id = "cyber_matrix",
                name = "Hyper Cyber 2099",
                category = "SHIP",
                description = "Illuminated neon cyan chassis engineered for precision drift.",
                rarity = "Epic",
                previewEmoji = "⚡",
                shardCost = 1200,
                isUnlocked = false
            ),
            CosmeticItem(
                id = "solar_flare",
                name = "Solaris Vanguard",
                category = "SHIP",
                description = "Forged in solar corona with blinding radiant flares.",
                rarity = "Legendary",
                previewEmoji = "🔥",
                shardCost = 2500,
                isUnlocked = false
            ),
            CosmeticItem(
                id = "plasma_cyan",
                name = "Plasma Cyan",
                category = "TRAIL",
                description = "Pulsing hyper-blue particle wake.",
                rarity = "Rare",
                previewEmoji = "🔷",
                shardCost = 0,
                isUnlocked = true
            ),
            CosmeticItem(
                id = "neon_magenta",
                name = "Neon Magenta Warp",
                category = "TRAIL",
                description = "Vivid ultraviolet energy stream.",
                rarity = "Epic",
                previewEmoji = "💖",
                shardCost = 600,
                isUnlocked = true
            ),
            CosmeticItem(
                id = "cosmic_gold",
                name = "Starlight Stardust",
                category = "TRAIL",
                description = "Glistening golden stardust trail from deep cosmos.",
                rarity = "Legendary",
                previewEmoji = "✨",
                shardCost = 1400,
                isUnlocked = false
            ),
            CosmeticItem(
                id = "title_apex",
                name = "Apex Pilot",
                category = "TITLE",
                description = "Earned by dominating the seasonal ladder.",
                rarity = "Rare",
                previewEmoji = "🎖️",
                shardCost = 0,
                isUnlocked = true
            ),
            CosmeticItem(
                id = "title_rift",
                name = "Rift Breaker",
                category = "TITLE",
                description = "Achieved by clearing 100k points in Endless Odyssey.",
                rarity = "Epic",
                previewEmoji = "🌌",
                shardCost = 500,
                isUnlocked = true
            ),
            CosmeticItem(
                id = "title_legend",
                name = "Quantum Legend",
                category = "TITLE",
                description = "Awarded to cross-play champions across the globe.",
                rarity = "Legendary",
                previewEmoji = "👑",
                shardCost = 2000,
                isUnlocked = false
            )
        )
    }

    fun getGlobalLeaderboard(currentUserScore: Long): List<LeaderboardPlayer> {
        val list = mutableListOf(
            LeaderboardPlayer(1, "UID-9841-PC", "Valkyrie_Zero", "Apex Grandmaster", "PC", "Apex", 194820L, 3420),
            LeaderboardPlayer(2, "UID-1284-XB", "TitanStrike", "Grandmaster", "Xbox", "Master", 178400L, 3180),
            LeaderboardPlayer(3, "UID-5529-PS", "Kira_Kinetix", "Grandmaster", "PlayStation", "Master", 162900L, 3050),
            LeaderboardPlayer(4, "UID-7712-IOS", "NovaAstral", "Master I", "iOS", "Master", 149300L, 2910),
            LeaderboardPlayer(5, "UID-3301-PC", "CyberSpecter", "Master II", "PC", "Master", 138700L, 2760),
            LeaderboardPlayer(6, "CR-7829-X", "NovaPilot (You)", "Apex Pilot", "Android", "Diamond II", currentUserScore, 2140, isUser = true),
            LeaderboardPlayer(7, "UID-6490-SW", "PixelStar", "Diamond I", "Switch", "Diamond", 81200L, 2110),
            LeaderboardPlayer(8, "UID-4412-AND", "ShadowRacer", "Diamond II", "Android", "Diamond", 77600L, 2040),
            LeaderboardPlayer(9, "UID-8032-PC", "HyperDrive99", "Platinum I", "PC", "Platinum", 71400L, 1920),
            LeaderboardPlayer(10, "UID-2194-IOS", "AuraGlide", "Platinum II", "iOS", "Platinum", 68500L, 1850)
        )
        return list.sortedByDescending { it.score }.mapIndexed { index, player ->
            player.copy(rank = index + 1)
        }
    }
}
