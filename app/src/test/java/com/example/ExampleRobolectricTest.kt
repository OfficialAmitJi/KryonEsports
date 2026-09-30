package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CosmoRush", appName)
  }

  @Test
  fun `verify game engine initialization`() {
    val engine = GameEngine("Multiplayer Arena")
    engine.initializeArena(800f, 1200f)
    assertTrue(engine.isAlive)
    assertEquals(100f, engine.playerShield, 0.01f)
    assertEquals(3, engine.rivals.size)
  }

  @Test
  fun `verify daily streak reward tiers`() {
    val database = com.example.data.database.AppDatabase.getInstance(ApplicationProvider.getApplicationContext())
    val repository = com.example.data.repository.GameRepository(
        database.userDao(),
        database.matchDao(),
        database.chatDao(),
        database.challengeDao(),
        database.notificationDao()
    )
    val tiers = repository.getStreakTiers()
    assertEquals(7, tiers.size)
    assertEquals(1.0f, tiers[0].multiplier, 0.01f)
    assertEquals(2.0f, tiers[6].multiplier, 0.01f)
    assertTrue(tiers[6].isCosmeticReward)
  }
}
