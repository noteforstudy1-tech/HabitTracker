package com.habittracker.app.data.dao

import androidx.room.*
import com.habittracker.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfile)

    @Query("SELECT COUNT(*) FROM user_profile WHERE id = 1")
    suspend fun profileExists(): Int
}
