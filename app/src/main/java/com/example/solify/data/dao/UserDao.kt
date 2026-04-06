package com.example.solify.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.solify.data.db_models.UserDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // LoginUserUseCase — найти пользователя по email
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserDbModel?

    // получение текущего пользователя
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserDbModel>

    // Для RegisterUserUseCase
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserDbModel)

    // Для UpdateUserProfileUseCase
    @Update
    suspend fun updateUser(user: UserDbModel)

    // Для DeleteAccountUseCase
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}