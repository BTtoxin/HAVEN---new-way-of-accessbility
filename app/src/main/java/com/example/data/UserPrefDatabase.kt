package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [UserEntity::class, UserPreferenceEntity::class, FocusSessionEntity::class, AutomationRuleEntity::class, DailyFocusGoalEntity::class], version = 3, exportSchema = false)
abstract class UserPrefDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun dailyFocusGoalDao(): DailyFocusGoalDao

    companion object {
        @Volatile
        private var INSTANCE: UserPrefDatabase? = null
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `daily_goals` (`dateString` TEXT NOT NULL, `completedMinutes` INTEGER NOT NULL, `goalMinutes` INTEGER NOT NULL, `isGoalMet` INTEGER NOT NULL, PRIMARY KEY(`dateString`))"
                )
            }
        }

        fun getDatabase(context: Context): UserPrefDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserPrefDatabase::class.java,
                    "user_preferences_db"
                ).addMigrations(MIGRATION_2_3)
                 .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
