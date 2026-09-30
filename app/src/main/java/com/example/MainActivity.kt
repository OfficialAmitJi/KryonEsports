package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.components.CosmoBottomNavigation
import com.example.ui.components.CosmoTopAppBar
import com.example.ui.components.ModerationReportDialog
import com.example.ui.components.PublicShareDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoidDark

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle public deep link joins
        intent?.data?.let { uri ->
            val room = uri.getQueryParameter("room")
            if (!room.isNullOrBlank()) {
                viewModel.navigateTo(AppScreen.MULTIPLAYER_LOBBY)
            }
        }

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val user by viewModel.userProfile.collectAsState()
    val activeGameMode by viewModel.activeGameMode.collectAsState()
    val isMatchmaking by viewModel.isMatchmaking.collectAsState()
    val matchFound by viewModel.matchFound.collectAsState()
    val roomCode by viewModel.roomCode.collectAsState()
    val selectedChannel by viewModel.selectedChatChannel.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val reportTargetMessage by viewModel.reportTargetMessage.collectAsState()
    val reportToast by viewModel.reportConfirmationToast.collectAsState()
    val showSyncDialog by viewModel.showSyncDialog.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
    val dailyChallenges by viewModel.dailyChallenges.collectAsState()
    val streakRewardClaimed by viewModel.streakRewardClaimed.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }

    // Back handling for secondary screens
    if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.GAME_PLAY) {
        BackHandler {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    if (currentScreen == AppScreen.GAME_PLAY) {
        // Full screen arcade canvas game
        GamePlayScreen(
            gameMode = activeGameMode,
            shipSkinId = user?.shipSkinId ?: "apex_phoenix",
            trailFxId = user?.trailFxId ?: "plasma_cyan",
            audioComms = viewModel.audioComms,
            onFinishGame = { score, placement, kills, survivalSec, summary ->
                viewModel.recordGameFinished(activeGameMode, score, placement, kills, survivalSec, summary)
            },
            onExitGame = {
                viewModel.navigateTo(AppScreen.HOME)
            }
        )
    } else {
        Scaffold(
            containerColor = VoidDark,
            topBar = {
                CosmoTopAppBar(
                    title = when (currentScreen) {
                        AppScreen.HOME -> "COSMORUSH"
                        AppScreen.MULTIPLAYER_LOBBY -> "MATCHMAKING"
                        AppScreen.LEADERBOARD -> "LEADERBOARDS"
                        AppScreen.DAILY_CHALLENGES -> "DAILY QUESTS"
                        AppScreen.COMMS_CHAT -> "COMMS & CHAT"
                        AppScreen.PROFILE_HANGAR -> "HANGAR"
                        AppScreen.NOTIFICATIONS -> "COMMUNITY ALERTS"
                        else -> "COSMORUSH"
                    },
                    quantumShards = user?.quantumShards ?: 1850,
                    energyCores = user?.energyCores ?: 320,
                    unreadNotifications = unreadNotifs,
                    canNavigateBack = currentScreen == AppScreen.NOTIFICATIONS,
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onNotificationsClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
                    onShareClick = { showShareDialog = true }
                )
            },
            bottomBar = {
                CosmoBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VoidDark)
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                user = user,
                                onStartGame = { mode -> viewModel.startQuickPlay(mode) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                        AppScreen.MULTIPLAYER_LOBBY -> {
                            MultiplayerLobbyScreen(
                                user = user,
                                isMatchmaking = isMatchmaking,
                                matchFound = matchFound,
                                roomCode = roomCode,
                                audioComms = viewModel.audioComms,
                                onStartMatchmaking = { viewModel.startMatchmakingQueue() },
                                onCancelMatchmaking = { viewModel.cancelMatchmaking() },
                                onRegenerateRoomCode = { viewModel.regenerateRoomCode() },
                                onLaunchRoomMatch = { viewModel.startQuickPlay("Multiplayer Arena") }
                            )
                        }
                        AppScreen.LEADERBOARD -> {
                            LeaderboardScreen(
                                players = viewModel.getGlobalLeaderboard()
                            )
                        }
                        AppScreen.DAILY_CHALLENGES -> {
                            DailyChallengesScreen(
                                challenges = dailyChallenges,
                                user = user,
                                streakTiers = viewModel.streakTiers,
                                claimedRewardCelebration = streakRewardClaimed,
                                onClaimStreakReward = { viewModel.claimDailyStreak() },
                                onBuyStreakShield = { viewModel.buyStreakShield() },
                                onSimulateAdvanceDay = { viewModel.simulateAdvanceDay() },
                                onDismissCelebration = { viewModel.dismissStreakCelebration() },
                                onClaimChallenge = { viewModel.claimChallenge(it) }
                            )
                        }
                        AppScreen.COMMS_CHAT -> {
                            ChatScreen(
                                user = user,
                                messages = chatMessages,
                                selectedChannel = selectedChannel,
                                reportToastMessage = reportToast,
                                onSelectChannel = { viewModel.selectChatChannel(it) },
                                onSendMessage = { viewModel.sendChatMessage(it) },
                                onReportMessage = { viewModel.openReportDialog(it) }
                            )
                        }
                        AppScreen.PROFILE_HANGAR -> {
                            ProfileHangarScreen(
                                user = user,
                                cosmetics = viewModel.cosmetics,
                                showSyncDialog = showSyncDialog,
                                onEquipShip = { viewModel.equipShipSkin(it) },
                                onEquipTrail = { viewModel.equipTrail(it) },
                                onEquipTitle = { viewModel.equipTitle(it) },
                                onShowSyncDialog = { viewModel.setShowSyncDialog(it) }
                            )
                        }
                        AppScreen.NOTIFICATIONS -> {
                            NotificationsScreen(
                                notifications = notifications,
                                onMarkAllRead = { viewModel.markAllNotificationsRead() }
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    // Moderation Report Dialog
    reportTargetMessage?.let { target ->
        ModerationReportDialog(
            targetMessage = target,
            onDismiss = { viewModel.closeReportDialog() },
            onSubmitReport = { reason -> viewModel.submitReport(reason) }
        )
    }

    // Public Share Dialog
    if (showShareDialog) {
        PublicShareDialog(
            roomCode = if (currentScreen == AppScreen.MULTIPLAYER_LOBBY) roomCode else null,
            onDismiss = { showShareDialog = false }
        )
    }
}
