package com.example.game

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class TeammateVoiceState(
    val id: String,
    val name: String,
    val platform: String, // "PC", "iOS", "Xbox", "PlayStation"
    val isTalking: Boolean,
    val volume: Float = 1.0f,
    val lastPing: String? = null
)

class GameAudioComms {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _isMicEnabled = MutableStateFlow(true)
    val isMicEnabled: StateFlow<Boolean> = _isMicEnabled.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _noiseSuppression = MutableStateFlow(true)
    val noiseSuppression: StateFlow<Boolean> = _noiseSuppression.asStateFlow()

    private val _liveWaveform = MutableStateFlow(List(12) { 0.08f })
    val liveWaveform: StateFlow<List<Float>> = _liveWaveform.asStateFlow()

    private val _teammates = MutableStateFlow(
        listOf(
            TeammateVoiceState("T1", "ValkyrieX", "PC", false, 0.9f),
            TeammateVoiceState("T2", "Kira_Kinetix", "iOS", false, 1.0f),
            TeammateVoiceState("T3", "TitanStrike", "Xbox", false, 0.85f)
        )
    )
    val teammates: StateFlow<List<TeammateVoiceState>> = _teammates.asStateFlow()

    private val _activeVoiceSpeaker = MutableStateFlow<String?>(null)
    val activeVoiceSpeaker: StateFlow<String?> = _activeVoiceSpeaker.asStateFlow()

    private var waveformJob: Job? = null
    private var chatterJob: Job? = null
    private var isSimulating = false

    init {
        startComms()
    }

    fun startComms() {
        if (isSimulating) return
        isSimulating = true
        startWaveformSimulation()
        startTeammateChatterSimulation()
    }

    fun stopComms() {
        isSimulating = false
        waveformJob?.cancel()
        waveformJob = null
        chatterJob?.cancel()
        chatterJob = null
        _liveWaveform.value = List(12) { 0.08f }
        _activeVoiceSpeaker.value = null
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
    }

    fun toggleNoiseSuppression() {
        _noiseSuppression.value = !_noiseSuppression.value
    }

    fun setTeammateVolume(id: String, volume: Float) {
        _teammates.value = _teammates.value.map {
            if (it.id == id) it.copy(volume = volume) else it
        }
    }

    fun broadcastTacticalPing(pingMessage: String) {
        // Broadcasts quick tactical voice comm
        _activeVoiceSpeaker.value = "You: \"$pingMessage\""
        scope.launch {
            delay(2800)
            if (_activeVoiceSpeaker.value?.startsWith("You") == true) {
                _activeVoiceSpeaker.value = null
            }
        }
    }

    private fun startWaveformSimulation() {
        waveformJob?.cancel()
        waveformJob = scope.launch {
            while (isActive && isSimulating) {
                if (!_isMuted.value && _isMicEnabled.value) {
                    val base = if (Random.nextFloat() > 0.45f) 0.6f else 0.2f
                    _liveWaveform.value = List(12) {
                        (base * (0.35f + Random.nextFloat() * 0.65f)).coerceIn(0.1f, 1.0f)
                    }
                } else {
                    _liveWaveform.value = List(12) { 0.08f }
                }
                delay(180) // 180ms is smooth and avoids aggressive thread scheduling
            }
        }
    }

    private fun startTeammateChatterSimulation() {
        chatterJob?.cancel()
        chatterJob = scope.launch {
            val callouts = listOf(
                "Shields up! Asteroid incoming!",
                "Focus fire on the center drone!",
                "EMP ready in 5 seconds!",
                "Nice shot! Lead secured!",
                "Enemy frigate approaching from right!"
            )
            while (isActive && isSimulating) {
                delay(Random.nextLong(9000, 18000))
                if (!isSimulating) break
                val randomTeammate = _teammates.value.randomOrNull() ?: continue
                val callout = callouts.random()

                // Mark talking
                _teammates.value = _teammates.value.map {
                    if (it.id == randomTeammate.id) it.copy(isTalking = true, lastPing = callout) else it
                }
                _activeVoiceSpeaker.value = "${randomTeammate.name} (${randomTeammate.platform}): \"$callout\""

                delay(3000)

                _teammates.value = _teammates.value.map {
                    if (it.id == randomTeammate.id) it.copy(isTalking = false) else it
                }
                if (_activeVoiceSpeaker.value?.contains(randomTeammate.name) == true) {
                    _activeVoiceSpeaker.value = null
                }
            }
        }
    }

    fun cleanUp() {
        stopComms()
        scope.cancel()
    }
}
