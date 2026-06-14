package com.example.services

import android.content.Context
import com.example.data.DailyFocusGoalEntity
import com.example.data.UserPrefDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FocusStreakService {
    
    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun updateDailyGoal(context: Context, addedFocusMinutes: Int, dailyGoalMinutes: Int): Boolean {
        val db = UserPrefDatabase.getDatabase(context)
        val todayStr = getCurrentDateString()
        val dao = db.dailyFocusGoalDao()
        var goal = dao.getGoalForDate(todayStr)
        
        if (goal == null) {
            goal = DailyFocusGoalEntity(
                dateString = todayStr,
                completedMinutes = addedFocusMinutes,
                goalMinutes = dailyGoalMinutes,
                isGoalMet = addedFocusMinutes >= dailyGoalMinutes
            )
        } else {
            val totalMinutes = goal.completedMinutes + addedFocusMinutes
            goal = goal.copy(
                completedMinutes = totalMinutes,
                goalMinutes = dailyGoalMinutes,
                isGoalMet = totalMinutes >= dailyGoalMinutes
            )
        }
        dao.insertGoal(goal)
        return goal.isGoalMet
    }
    
    suspend fun checkStreakMilestone(context: Context, onMilestoneReached: (Int) -> Unit) {
        val db = UserPrefDatabase.getDatabase(context)
        val goals = db.dailyFocusGoalDao().getRecentGoals()
        var streak = 0
        
        for (goal in goals) {
            if (goal.isGoalMet) {
                streak++
            } else {
                break
            }
        }
        
        if (streak > 0 && streak % 3 == 0) {
            // Milestone every 3 days for example, or based on logic
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onMilestoneReached(streak)
            }
        } else if (streak == 1) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onMilestoneReached(1)
            }
        }
    }
}
