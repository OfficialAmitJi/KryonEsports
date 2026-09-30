package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameAudioComms
import com.example.game.GameEngine
import com.example.game.PowerupType
import com.example.ui.theme.*
import com.example.util.ShareHelper
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GamePlayScreen(
    gameMode: String,
    shipSkinId: String,
    trailFxId: String,
    audioComms: GameAudioComms,
    onFinishGame: (score: Long, placement: Int, kills: Int, survivalTime: Int, summary: String) -> Unit,
    onExitGame: () -> Unit
) {
    val engine = remember { GameEngine(gameMode, shipSkinId, trailFxId) }
    var isPaused by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var frameTick by remember { mutableLongStateOf(0L) }

    // Reusable cached Path objects to prevent allocating 200+ Path objects per second in draw loop
    val reusableDronePath = remember { Path() }
    val reusableHullPath = remember { Path() }
    val reusableCockpitPath = remember { Path() }
    val reusableGhostPath = remember { Path() }
    val precomputedStars = remember {
        val rand = kotlin.random.Random(1337)
        List(50) { Pair(rand.nextFloat(), rand.nextFloat()) }
    }

    val liveWaveform by audioComms.liveWaveform.collectAsState()
    val isMuted by audioComms.isMuted.collectAsState()
    val activeSpeaker by audioComms.activeVoiceSpeaker.collectAsState()

    // Pulse animation for EMP button
    val infiniteTransition = rememberInfiniteTransition(label = "emp_pulse")
    val empPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BackHandler {
        if (!showGameOverDialog) {
            isPaused = true
        } else {
            onExitGame()
        }
    }

    // 60FPS Game Loop - cleanly stopped when game is paused or over
    LaunchedEffect(isPaused, showGameOverDialog) {
        var lastTime = System.nanoTime()
        while (!isPaused && !showGameOverDialog) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1_000_000_000f
            lastTime = now

            if (engine.isAlive) {
                engine.update(dt)
                frameTick = now
            }

            if (!engine.isAlive && !showGameOverDialog) {
                showGameOverDialog = true
                val placement = engine.getPlayerPlacement()
                val summary = if (gameMode.contains("Multiplayer")) "Multiplayer Match vs 3 Rivals" else "Endless Odyssey Survival"
                onFinishGame(engine.score, placement, engine.kills, engine.survivalTimeSec, summary)
                break
            }
            delay(16) // ~60 FPS
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    engine.setTargetPosition(change.position.x, change.position.y)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    engine.setTargetPosition(offset.x, offset.y)
                }
            }
    ) {
        // Main Canvas for stars, ship, laser, enemies, particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentFrame = frameTick // Bind Draw phase invalidation to frame updates
            engine.initializeArena(size.width, size.height)

            // Draw Parallax Stars without per-frame allocations
            drawStarfield(size, precomputedStars, (System.currentTimeMillis() % 10000) * 0.05f)

            // Draw Asteroids
            for (asteroid in engine.asteroids) {
                rotate(asteroid.rotation, pivot = Offset(asteroid.x, asteroid.y)) {
                    drawCircle(
                        color = Color(0xFF6B7280),
                        radius = asteroid.radius,
                        center = Offset(asteroid.x, asteroid.y)
                    )
                    drawCircle(
                        color = Color(0xFF374151),
                        radius = asteroid.radius * 0.65f,
                        center = Offset(asteroid.x - asteroid.radius * 0.2f, asteroid.y - asteroid.radius * 0.2f)
                    )
                    // Crater
                    drawCircle(
                        color = Color(0xFF1F2937),
                        radius = asteroid.radius * 0.25f,
                        center = Offset(asteroid.x + asteroid.radius * 0.25f, asteroid.y + asteroid.radius * 0.1f)
                    )
                }
            }

            // Draw Drones using zero-allocation reusable Path
            for (drone in engine.drones) {
                reusableDronePath.reset()
                reusableDronePath.moveTo(drone.x, drone.y + 16f)
                reusableDronePath.lineTo(drone.x - 16f, drone.y - 14f)
                reusableDronePath.lineTo(drone.x + 16f, drone.y - 14f)
                reusableDronePath.close()
                drawPath(reusableDronePath, color = Color(0xFFFF1744))
                // Drone core
                drawCircle(color = CyberCyan, radius = 4f, center = Offset(drone.x, drone.y))
            }

            // Draw Powerups
            for (power in engine.powerups) {
                val color = when (power.type) {
                    PowerupType.SHIELD_BOOST -> EmeraldMatrix
                    PowerupType.EMP_NUKE -> CyberCyan
                    PowerupType.DOUBLE_SCORE -> CosmicAmber
                    PowerupType.QUANTUM_SHARD -> NeonMagenta
                }
                drawCircle(color = color.copy(alpha = 0.35f), radius = 18f, center = Offset(power.x, power.y))
                drawCircle(color = color, radius = 10f, center = Offset(power.x, power.y))
                drawCircle(color = Color.White, radius = 4f, center = Offset(power.x, power.y))
            }

            // Draw Projectiles
            for (p in engine.projectiles) {
                drawLine(
                    color = p.color,
                    start = Offset(p.x, p.y),
                    end = Offset(p.x, p.y + (if (p.isEnemy) -16f else 16f)),
                    strokeWidth = 4f
                )
            }

            // Draw Particles
            for (part in engine.particles) {
                drawCircle(
                    color = part.color.copy(alpha = part.alpha),
                    radius = part.size,
                    center = Offset(part.x, part.y)
                )
            }

            // Draw Multiplayer Rivals (Holographic Ghost Ships)
            if (engine.gameMode.contains("Multiplayer")) {
                for (rival in engine.rivals) {
                    if (rival.isAlive) {
                        drawRivalShip(rival.x, rival.y, rival.color, reusableGhostPath)
                    }
                }
            }

            // Draw Player Ship using zero-allocation reusable Paths
            if (engine.isAlive) {
                drawPlayerStarship(engine.playerX, engine.playerY, shipSkinId, reusableHullPath, reusableCockpitPath)

                // Shield bubble if player has shield
                if (engine.playerShield > 0f) {
                    val shieldAlpha = (engine.playerShield / 100f) * 0.45f
                    drawCircle(
                        color = CyberCyan.copy(alpha = shieldAlpha),
                        radius = 34f,
                        center = Offset(engine.playerX, engine.playerY),
                        style = Stroke(width = 2.5f)
                    )
                }
            }
        }

        // Top Game HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Multiplier
                Column {
                    Text(
                        text = "SCORE",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${engine.score}",
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (engine.scoreMultiplier > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CosmicAmber)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${engine.scoreMultiplier}X",
                                    color = VoidDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Survival Time
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SpaceCard)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏱️", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    val mins = engine.survivalTimeSec / 60
                    val secs = engine.survivalTimeSec % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Pause Button
                IconButton(
                    onClick = { isPaused = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SpaceCard)
                        .minimumInteractiveComponentSize()
                        .testTag("game_pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shield and Energy Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Shield Bar
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SHIELD", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${engine.playerShield.toInt()}%", color = EmeraldMatrix, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { (engine.playerShield / engine.maxShield).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldMatrix,
                        trackColor = SpaceBorder
                    )
                }

                // EMP Energy Bar
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("EMP OVERDRIVE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${engine.playerEnergy.toInt()}%", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { (engine.playerEnergy / engine.maxEnergy).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyberCyan,
                        trackColor = SpaceBorder
                    )
                }
            }

            // Multiplayer Mini-Scoreboard Banner (if in multiplayer)
            if (engine.gameMode.contains("Multiplayer")) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpaceCard.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RANK #${engine.getPlayerPlacement()}",
                        color = CosmicAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    for (rival in engine.rivals) {
                        Text(
                            text = "${rival.name}: ${rival.score}",
                            color = if (rival.isAlive) rival.color else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Live Voice Comms Notification (Floating Banner)
        AnimatedVisibility(
            visible = activeSpeaker != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(VoidDark.copy(alpha = 0.9f))
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activeSpeaker ?: "",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom Controls: Voice Chat HUD, EMP Detonate Button & Tactical Pings
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tactical Voice Comms bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceCard.copy(alpha = 0.9f))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mic Button
                IconButton(
                    onClick = { audioComms.toggleMute() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) DangerRed.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f))
                        .minimumInteractiveComponentSize()
                        .testTag("in_game_mic_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (isMuted) DangerRed else CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Animated Live Equalizer Waveform
                Row(
                    modifier = Modifier.height(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    liveWaveform.take(8).forEach { amp ->
                        val barHeight = (18.dp * amp).coerceIn(3.dp, 18.dp)
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(if (isMuted) TextMuted else CyberCyan)
                        )
                    }
                }

                // Tactical Quick Voice Pings
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val pings = listOf("🛡️ Shield", "⚡ EMP", "🎯 Focus", "🔥 GG")
                    pings.forEach { ping ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SpaceCardLight)
                                .clickable { audioComms.broadcastTacticalPing(ping) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(text = ping, fontSize = 10.sp, color = TextWhite)
                        }
                    }
                }
            }

            // EMP Detonation Button
            if (engine.isEmpReady) {
                Button(
                    onClick = { engine.triggerEmpBlast() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("emp_detonate_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Detonate EMP",
                        tint = VoidDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DETONATE EMP HYPER BLAST",
                        color = VoidDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Pause Modal
        if (isPaused && !showGameOverDialog) {
            AlertDialog(
                onDismissRequest = { isPaused = false },
                containerColor = SpaceCard,
                title = {
                    Text(
                        text = "MISSION PAUSED",
                        color = TextWhite,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Score: ${engine.score} | Survival: ${engine.survivalTimeSec}s",
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Voice comms remain active in the background.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_resume_game")
                    ) {
                        Text("RESUME", color = VoidDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onExitGame() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_exit_to_lobby")
                    ) {
                        Text("EXIT TO HANGAR", color = DangerRed)
                    }
                }
            )
        }

        // Game Over / Victory Modal
        if (showGameOverDialog) {
            val placement = engine.getPlayerPlacement()
            val isWin = placement == 1
            AlertDialog(
                onDismissRequest = { /* Force explicit button press */ },
                containerColor = SpaceCard,
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isWin) "🏆 VICTORY ROYALE!" else "💥 HULL DESTROYED",
                            color = if (isWin) CosmicAmber else DangerRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        if (gameMode.contains("Multiplayer")) {
                            Text(
                                text = "PLACEMENT: #$placement OF 4",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatRow("Final Score", "${engine.score}")
                        StatRow("Drones Obliterated", "${engine.kills}")
                        StatRow("Time Survived", "${engine.survivalTimeSec}s")
                        StatRow("Quantum Shards Earned", "+${(engine.score / 100) + (if (isWin) 150 else 50)}")

                        val context = LocalContext.current
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { ShareHelper.shareHighScore(context, engine.score, gameMode) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("btn_share_score_game_over"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicAmber),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(CosmicAmber, CosmicAmber.copy(alpha = 0.6f)))
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHARE RECORD / CHALLENGE FRIENDS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGameOverDialog = false
                            engine.restartGame()
                            isPaused = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_play_again")
                    ) {
                        Text("PLAY AGAIN", color = VoidDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onExitGame() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_return_lobby")
                    ) {
                        Text("HANGAR", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(VoidDark)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private fun DrawScope.drawStarfield(size: Size, stars: List<Pair<Float, Float>>, scrollOffset: Float) {
    stars.forEachIndexed { i, star ->
        val x = star.first * size.width
        val speedMultiplier = if (i % 2 == 0) 1.2f else 0.8f
        val y = (star.second * size.height + scrollOffset * speedMultiplier) % size.height
        val radius = if (i % 5 == 0) 1.8f else 1.0f
        drawCircle(
            color = Color.White.copy(alpha = if (i % 3 == 0) 0.8f else 0.4f),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawPlayerStarship(
    x: Float,
    y: Float,
    skinId: String,
    hullPath: Path,
    cockpitPath: Path
) {
    val primaryColor = when (skinId) {
        "void_stalker" -> Color(0xFF9C27B0)
        "cyber_matrix" -> Color(0xFF00E5FF)
        "solar_flare" -> Color(0xFFFF9100)
        else -> Color(0xFF00F0FF) // apex phoenix
    }

    // Reuse Hull Path
    hullPath.reset()
    hullPath.moveTo(x, y - 24f) // Nose
    hullPath.lineTo(x - 18f, y + 16f) // Left Wing
    hullPath.lineTo(x - 6f, y + 12f)
    hullPath.lineTo(x, y + 18f) // Thruster center
    hullPath.lineTo(x + 6f, y + 12f)
    hullPath.lineTo(x + 18f, y + 16f) // Right Wing
    hullPath.close()
    drawPath(hullPath, color = primaryColor)

    // Reuse Cockpit Path
    cockpitPath.reset()
    cockpitPath.moveTo(x, y - 14f)
    cockpitPath.lineTo(x - 4f, y + 2f)
    cockpitPath.lineTo(x + 4f, y + 2f)
    cockpitPath.close()
    drawPath(cockpitPath, color = Color.White)

    // Wing cannons
    drawCircle(color = NeonMagenta, radius = 2.5f, center = Offset(x - 14f, y - 6f))
    drawCircle(color = NeonMagenta, radius = 2.5f, center = Offset(x + 14f, y - 6f))
}

private fun DrawScope.drawRivalShip(x: Float, y: Float, color: Color, ghostPath: Path) {
    // Reuse Holographic Ghost Path
    ghostPath.reset()
    ghostPath.moveTo(x, y - 18f)
    ghostPath.lineTo(x - 12f, y + 12f)
    ghostPath.lineTo(x + 12f, y + 12f)
    ghostPath.close()
    drawPath(ghostPath, color = color.copy(alpha = 0.5f), style = Stroke(width = 2f))
    drawCircle(color = color.copy(alpha = 0.7f), radius = 3f, center = Offset(x, y))
}
