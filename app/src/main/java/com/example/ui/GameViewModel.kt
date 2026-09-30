package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CosmeticItem
import com.example.data.model.DailyChallengeEntity
import com.example.data.model.InGameNotificationEntity
import com.example.data.model.LeaderboardPlayer
import com.example.data.model.MatchHistoryEntity
import com.example.data.model.StreakRewardTier
import com.example.data.model.UserProfileEntity
import com.example.data.repository.GameRepository
import com.example.game.GameAudioComms
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    HOME,
    GAME_PLAY,
    MULTIPLAYER_LOBBY,
    LEADERBOARD,
    DAILY_CHALLENGES,
    COMMS_CHAT,
    PROFILE_HANGAR,
    NOTIFICATIONS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = GameRepository(
        database.userDao(),
        database.matchDao(),
        database.chatDao(),
        database.challengeDao(),
        database.notificationDao()
    )

    val audioComms = GameAudioComms()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active Game Session Config
    private val _activeGameMode = MutableStateFlow("Endless Odyssey")
    val activeGameMode: StateFlow<String> = _activeGameMode.asStateFlow()

    // Matchmaking State
    private val _isMatchmaking = MutableStateFlow(false)
    val isMatchmaking: StateFlow<Boolean> = _isMatchmaking.asStateFlow()

    private val _matchFound = MutableStateFlow(false)
    val matchFound: StateFlow<Boolean> = _matchFound.asStateFlow()

    private val _roomCode = MutableStateFlow("#CYBER-8842")
    val roomCode: StateFlow<String> = _roomCode.asStateFlow()

    private val _crossPlayFilter = MutableStateFlow("ALL") // "ALL", "PC", "CONSOLE", "MOBILE"
    val crossPlayFilter: StateFlow<String> = _crossPlayFilter.asStateFlow()

    // Selected Chat Channel
    private val _selectedChatChannel = MutableStateFlow("GLOBAL")
    val selectedChatChannel: StateFlow<String> = _selectedChatChannel.asStateFlow()

    // Reporting / Moderation State
    private val _reportTargetMessage = MutableStateFlow<ChatMessageEntity?>(null)
    val reportTargetMessage: StateFlow<ChatMessageEntity?> = _reportTargetMessage.asStateFlow()

    private val _reportConfirmationToast = MutableStateFlow<String?>(null)
    val reportConfirmationToast: StateFlow<String?> = _reportConfirmationToast.asStateFlow()

    // Sync Account Dialog State
    private val _showSyncDialog = MutableStateFlow(false)
    val showSyncDialog: StateFlow<Boolean> = _showSyncDialog.asStateFlow()

    // Data from repository
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = repository.matchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyChallenges: StateFlow<List<DailyChallengeEntity>> = repository.dailyChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<InGameNotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _selectedChatChannel
        .flatMapLatest { channel -> repository.getChatMessages(channel) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Streak Reward & Celebration State
    private val _streakRewardClaimed = MutableStateFlow<StreakRewardTier?>(null)
    val streakRewardClaimed: StateFlow<StreakRewardTier?> = _streakRewardClaimed.asStateFlow()

    val streakTiers: List<StreakRewardTier> = repository.getStreakTiers()

    val cosmetics: List<CosmeticItem> = repository.getCosmetics()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    fun claimDailyStreak() {
        viewModelScope.launch {
            val reward = repository.claimStreakReward()
            if (reward != null) {
                _streakRewardClaimed.value = reward
            }
        }
    }

    fun dismissStreakCelebration() {
        _streakRewardClaimed.value = null
    }

    fun buyStreakShield() {
        viewModelScope.launch {
            val success = repository.buyStreakShield()
            if (success) {
                _reportConfirmationToast.value = "Purchased 1 Streak Shield! Your streak is protected."
                delay(3000)
                _reportConfirmationToast.value = null
            } else {
                _reportConfirmationToast.value = "Need 300 Quantum Shards to buy a Streak Shield."
                delay(3000)
                _reportConfirmationToast.value = null
            }
        }
    }

    fun simulateAdvanceDay() {
        viewModelScope.launch {
            repository.simulateAdvanceDay()
            _reportConfirmationToast.value = "Simulated next day! Day streak reward unlocked."
            delay(3000)
            _reportConfirmationToast.value = null
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.GAME_PLAY || screen == AppScreen.MULTIPLAYER_LOBBY) {
            audioComms.startComms()
        } else {
            audioComms.stopComms()
        }
    }

    fun startQuickPlay(gameMode: String) {
        _activeGameMode.value = gameMode
        _currentScreen.value = AppScreen.GAME_PLAY
        audioComms.startComms()
    }

    fun startMatchmakingQueue() {
        _isMatchmaking.value = true
        _matchFound.value = false
        viewModelScope.launch {
            // Simulated matchmaking search with server latency check
            delay(2800)
            _isMatchmaking.value = false
            _matchFound.value = true
            delay(1600)
            _matchFound.value = false
            startQuickPlay("Multiplayer Arena")
        }
    }

    fun cancelMatchmaking() {
        _isMatchmaking.value = false
        _matchFound.value = false
    }

    fun regenerateRoomCode() {
        val rand = Random.nextInt(1000, 9999)
        _roomCode.value = "#NEON-$rand"
    }

    fun selectChatChannel(channel: String) {
        _selectedChatChannel.value = channel
    }

    fun sendChatMessage(text: String) {
        val user = userProfile.value ?: return
        if (text.isBlank()) return

        // Basic client-side moderation check
        val censoredText = censorText(text)

        viewModelScope.launch {
            repository.sendChatMessage(_selectedChatChannel.value, censoredText, user)

            // Simulated response in global chat to keep social experience alive
            if (_selectedChatChannel.value == "GLOBAL" && Random.nextFloat() < 0.6f) {
                delay(1500)
                val botReplies = listOf(
                    "GG! Ready for next queue!",
                    "That was intense!",
                    "Who's hosting the 4-player cross-play room?",
                    "Nice ship build!",
                    "Let's beat the community challenge today!"
                )
                val botSenders = listOf(
                    Triple("ValkyrieX", "Grandmaster", "PC"),
                    Triple("Kira_Kinetix", "Master", "iOS"),
                    Triple("TitanStrike", "Diamond III", "Xbox")
                )
                val bot = botSenders.random()
                val botUser = user.copy(
                    userId = "BOT_${Random.nextInt(100, 999)}",
                    displayName = bot.first,
                    title = bot.second,
                    avatarEmoji = listOf("🚀", "🛸", "⚡", "🔥").random()
                )
                repository.sendChatMessage("GLOBAL", botReplies.random(), botUser)
            }
        }
    }

    fun openReportDialog(message: ChatMessageEntity) {
        _reportTargetMessage.value = message
    }

    fun closeReportDialog() {
        _reportTargetMessage.value = null
    }

    fun submitReport(reason: String) {
        val target = _reportTargetMessage.value ?: return
        viewModelScope.launch {
            repository.reportChatMessage(target.messageId)
            _reportConfirmationToast.value = "Report submitted for '${target.senderName}'. Thanks for keeping the community safe!"
            _reportTargetMessage.value = null
            delay(3500)
            _reportConfirmationToast.value = null
        }
    }

    fun claimChallenge(challenge: DailyChallengeEntity) {
        viewModelScope.launch {
            repository.claimChallengeReward(challenge)
        }
    }

    fun equipShipSkin(skinId: String) {
        viewModelScope.launch {
            repository.equipShipSkin(skinId)
        }
    }

    fun equipTrail(trailId: String) {
        viewModelScope.launch {
            repository.equipTrail(trailId)
        }
    }

    fun equipTitle(title: String) {
        viewModelScope.launch {
            repository.equipTitle(title)
        }
    }

    fun recordGameFinished(
        mode: String,
        score: Long,
        placement: Int,
        kills: Int,
        survivalTimeSec: Int,
        opponentsSummary: String
    ) {
        viewModelScope.launch {
            repository.recordMatch(mode, score, placement, kills, survivalTimeSec, opponentsSummary)
        }
    }

    fun toggleVoiceMute() {
        audioComms.toggleMute()
        viewModelScope.launch {
            repository.setVoiceMuted(audioComms.isMuted.value)
        }
    }

    fun toggleNoiseSuppression() {
        audioComms.toggleNoiseSuppression()
        viewModelScope.launch {
            repository.setVoiceNoiseSuppression(audioComms.noiseSuppression.value)
        }
    }

    fun setShowSyncDialog(show: Boolean) {
        _showSyncDialog.value = show
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun getGlobalLeaderboard(): List<LeaderboardPlayer> {
        val userScore = userProfile.value?.highScore ?: 84250L
        return repository.getGlobalLeaderboard(userScore)
    }

    private fun censorText(text: String): String {
        val prohibitedWords = listOf("hate", "cheat", "scam", "trash", "badword")
        var result = text
        for (w in prohibitedWords) {
            val regex = Regex("(?i)\\b$w\\b")
            result = result.replace(regex, "****")
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        audioComms.cleanUp()
    }
}
