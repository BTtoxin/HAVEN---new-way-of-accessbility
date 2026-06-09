package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val database: UserPrefDatabase) {
    private val userDao = database.userDao()
    private val userPreferenceDao = database.userPreferenceDao()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserById(id: Int): UserEntity? {
        return userDao.getUserById(id)
    }

    suspend fun registerUser(
        username: String,
        pin: String,
        nickname: String,
        avatarColorHex: String
    ): UserEntity? {
        // Validate password/pin (e.g. 4-digit numeric pin)
        if (username.isEmpty() || pin.length < 4 || nickname.isEmpty()) return null
        
        // Check uniqueness
        val exists = userDao.getUserByUsername(username)
        if (exists != null) return null

        val newUser = UserEntity(
            username = username,
            pin = pin,
            nickname = nickname,
            avatarColorHex = avatarColorHex
        )
        val newId = userDao.insertUser(newUser)
        return newUser.copy(id = newId.toInt())
    }

    suspend fun authenticateUser(username: String, pin: String): UserEntity? {
        val user = userDao.getUserByUsername(username)
        return if (user != null && user.pin == pin) user else null
    }

    fun getPreferencesForUser(userId: Int): Flow<Map<String, String>> {
        return userPreferenceDao.getPreferencesForUser(userId).map { prefList ->
            prefList.associate { it.prefKey to it.prefValue }
        }
    }

    suspend fun savePreference(userId: Int, key: String, value: String) {
        userPreferenceDao.savePreference(
            UserPreferenceEntity(
                userId = userId,
                prefKey = key,
                prefValue = value,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getPreferenceValue(userId: Int, key: String): String? {
        return userPreferenceDao.getPreference(userId, key)?.prefValue
    }
}
