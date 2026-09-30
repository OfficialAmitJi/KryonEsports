package com.example.data.dao

import androidx.room.*
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DailyChallengeEntity
import com.example.data.model.InGameNotificationEntity
import com.example.data.model.MatchHistoryEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET quantumShards = quantumShards + :shards, energyCores = energyCores + :cores WHERE userId = :userId")
    suspend fun addCurrency(userId: String, shards: Int, cores: Int)

    @Query("UPDATE user_profile SET shipSkinId = :skinId WHERE userId = :userId")
    suspend fun updateShipSkin(userId: String, skinId: String)

    @Query("UPDATE user_profile SET trailFxId = :trailId WHERE userId = :userId")
    suspend fun updateTrailFx(userId: String, trailId: String)

    @Query("UPDATE user_profile SET title = :title WHERE userId = :userId")
    suspend fun updateTitle(userId: String, title: String)

    @Query("UPDATE user_profile SET displayName = :name WHERE userId = :userId")
    suspend fun updateDisplayName(userId: String, name: String)

    @Query("UPDATE user_profile SET voiceMuted = :muted WHERE userId = :userId")
    suspend fun setVoiceMuted(userId: String, muted: Boolean)

    @Query("UPDATE user_profile SET voiceNoiseSuppression = :enabled WHERE userId = :userId")
    suspend fun setNoiseSuppression(userId: String, enabled: Boolean)

    @Query("UPDATE user_profile SET highScore = MAX(highScore, :newScore), totalMatches = totalMatches + 1, wins = wins + :winIncr, currentXp = currentXp + :xpIncr WHERE userId = :userId")
    suspend fun updateMatchStats(userId: String, newScore: Long, winIncr: Int, xpIncr: Int)

    @Query("UPDATE user_profile SET quantumShards = quantumShards + :shards, energyCores = energyCores + :cores, streakDays = :newStreak, streakMultiplier = :multiplier, streakClaimedToday = 1, lastClaimDateString = :claimDate WHERE userId = :userId")
    suspend fun claimStreakReward(userId: String, shards: Int, cores: Int, newStreak: Int, multiplier: Float, claimDate: String)

    @Query("UPDATE user_profile SET streakClaimedToday = 0 WHERE userId = :userId")
    suspend fun resetStreakClaimedForNewDay(userId: String)

    @Query("UPDATE user_profile SET streakFreezeCores = streakFreezeCores + 1, quantumShards = quantumShards - :cost WHERE userId = :userId AND quantumShards >= :cost")
    suspend fun buyStreakFreeze(userId: String, cost: Int)

    @Query("UPDATE user_profile SET streakFreezeCores = MAX(0, streakFreezeCores - 1) WHERE userId = :userId")
    suspend fun consumeStreakFreeze(userId: String)

    @Query("UPDATE user_profile SET streakDays = 1, streakMultiplier = 1.0, streakClaimedToday = 0 WHERE userId = :userId")
    suspend fun resetStreak(userId: String)

    @Query("UPDATE user_profile SET streakDays = streakDays + 1, streakClaimedToday = 0 WHERE userId = :userId")
    suspend fun simulateAdvanceDay(userId: String)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllMatches(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistoryEntity)

    @Query("DELETE FROM match_history")
    suspend fun clearHistory()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM (SELECT * FROM chat_messages WHERE channel = :channel AND isFlagged = 0 ORDER BY timestamp DESC LIMIT 80) ORDER BY timestamp ASC")
    fun getMessagesForChannel(channel: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isFlagged = 1 WHERE messageId = :messageId")
    suspend fun flagMessage(messageId: Long)

    @Query("DELETE FROM chat_messages WHERE senderId = :senderId")
    suspend fun deleteMessagesFromUser(senderId: String)

    @Query("DELETE FROM chat_messages WHERE messageId NOT IN (SELECT messageId FROM chat_messages ORDER BY timestamp DESC LIMIT 150)")
    suspend fun pruneOldMessages()
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM daily_challenges")
    fun getAllChallenges(): Flow<List<DailyChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<DailyChallengeEntity>)

    @Query("UPDATE daily_challenges SET currentCount = MIN(targetCount, currentCount + :increment), isCompleted = CASE WHEN (currentCount + :increment) >= targetCount THEN 1 ELSE 0 END WHERE challengeId = :id")
    suspend fun progressChallenge(id: String, increment: Int)

    @Query("UPDATE daily_challenges SET isClaimed = 1 WHERE challengeId = :id")
    suspend fun claimChallenge(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 40")
    fun getAllNotifications(): Flow<List<InGameNotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: InGameNotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
