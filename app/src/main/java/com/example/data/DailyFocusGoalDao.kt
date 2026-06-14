package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFocusGoalDao {
    @Query("SELECT * FROM daily_goals")
    fun getAllGoals(): Flow<List<DailyFocusGoalEntity>>

    @Query("SELECT * FROM daily_goals ORDER BY dateString DESC LIMIT 100")
    suspend fun getRecentGoals(): List<DailyFocusGoalEntity>

    @Query("SELECT * FROM daily_goals WHERE dateString = :date limit 1")
    suspend fun getGoalForDate(date: String): DailyFocusGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyFocusGoalEntity)
}
