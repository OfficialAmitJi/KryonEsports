package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareHelper {
    const val PUBLIC_APP_URL = "https://ais-pre-j2nw5bnmk633bq6vhnd2nc-639781715696.asia-east1.run.app"

    fun shareApp(context: Context) {
        val shareText = """
            🚀 Play CosmoRush with me!
            Cross-platform multiplayer arcade arena with real-time matchmaking, live voice comms, and daily community challenges!
            
            Play now: $PUBLIC_APP_URL
        """.trimIndent()

        launchShareIntent(context, shareText, "Share CosmoRush")
    }

    fun shareLobby(context: Context, roomCode: String) {
        val shareText = """
            🎮 Join my CosmoRush 4-Player Arena party!
            Room Code: #$roomCode
            
            Join our lobby instantly: $PUBLIC_APP_URL?room=$roomCode
        """.trimIndent()

        launchShareIntent(context, shareText, "Invite to CosmoRush Lobby")
    }

    fun shareHighScore(context: Context, score: Long, gameMode: String) {
        val shareText = """
            ⚡ I just scored ${String.format("%,d", score)} points in CosmoRush ($gameMode)!
            Think you can beat my record?
            
            Challenge me here: $PUBLIC_APP_URL
        """.trimIndent()

        launchShareIntent(context, shareText, "Share CosmoRush High Score")
    }

    fun shareProfile(context: Context, crossPlayId: String, displayName: String, title: String) {
        val shareText = """
            👾 Pilot Profile: $displayName ($title)
            Cross-Play ID: $crossPlayId
            
            Connect with me in CosmoRush: $PUBLIC_APP_URL
        """.trimIndent()

        launchShareIntent(context, shareText, "Share Pilot Profile")
    }

    fun copyToClipboard(context: Context, text: String, label: String = "CosmoRush Link") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    private fun launchShareIntent(context: Context, text: String, chooserTitle: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, chooserTitle)
        context.startActivity(shareIntent)
    }
}
