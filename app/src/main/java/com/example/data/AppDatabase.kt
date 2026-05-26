package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val isLoggedIn: Boolean = false,
    val name: String = "",
    val email: String = "",
    val gender: String = "",
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val stepGoal: Int = 10000,
    val activeEnergyGoal: Int = 500,
    val sleepGoalHours: Float = 8f,
    val distanceGoalKm: Float = 5f,
    val hydrationGoalLiters: Float = 2.5f
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert
    suspend fun insertMessage(message: ChatMessage)
    
    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfile)
}

@Database(entities = [ChatMessage::class, UserProfile::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun userProfileDao(): UserProfileDao
}
