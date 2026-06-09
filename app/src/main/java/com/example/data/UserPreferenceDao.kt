package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getPreferencesForUser(userId: Int): Flow<List<UserPreferenceEntity>>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId AND prefKey = :prefKey LIMIT 1")
    suspend fun getPreference(userId: Int, prefKey: String): UserPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreference(preference: UserPreferenceEntity)

    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    suspend fun clearPreferencesForUser(userId: Int)
}
