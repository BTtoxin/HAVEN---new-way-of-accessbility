package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val durationMillis: Long,
    val focusModeEnabled: Boolean,
    val subject: String?,
    val distractionCount: Int = 0
)

@Entity(tableName = "focus_logs")
data class FocusLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val note: String,
    val blockedAppAttempts: Int
)

@Entity(tableName = "battery_health")
data class BatteryHealthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordedAt: Long,
    val cycleCount: Int,
    val estimatedCapacityMilliAmpereHour: Int
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examName: String,
    val dailyGoalMinutes: Int,
    val progressMinutes: Int
)

@Dao
interface StudySessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)

    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT SUM(durationMillis) FROM study_sessions WHERE startTime >= :since")
    fun getTotalFocusTimeSince(since: Long): Flow<Long?>
}

@Dao
interface NewFeaturesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusLog(log: FocusLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryHealth(health: BatteryHealthEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(plan: StudyPlanEntity)
}

@Database(entities = [StudySession::class, FocusLogEntity::class, BatteryHealthEntity::class, StudyPlanEntity::class], version = 2, exportSchema = false)
abstract class HavenDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao
    abstract fun newFeaturesDao(): NewFeaturesDao

    companion object {
        @Volatile
        private var INSTANCE: HavenDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `focus_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `note` TEXT NOT NULL, `blockedAppAttempts` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `battery_health` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordedAt` INTEGER NOT NULL, `cycleCount` INTEGER NOT NULL, `estimatedCapacityMilliAmpereHour` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `study_plans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `examName` TEXT NOT NULL, `dailyGoalMinutes` INTEGER NOT NULL, `progressMinutes` INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: android.content.Context): HavenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HavenDatabase::class.java,
                    "haven_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
