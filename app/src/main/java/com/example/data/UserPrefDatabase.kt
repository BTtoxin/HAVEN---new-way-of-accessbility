package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, UserPreferenceEntity::class], version = 1, exportSchema = false)
abstract class UserPrefDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: UserPrefDatabase? = null

        fun getDatabase(context: Context): UserPrefDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserPrefDatabase::class.java,
                    "user_preferences_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
