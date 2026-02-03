package com.example.mentalcare.data

import androidx.room.*
import com.example.mentalcare.model.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")    suspend fun getUserByUsername(username: String): UserEntity?

    //Aborta si el usuario existe
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserEntity)

    //Función para obtener el número de users
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    // Función para obtener todos los users
    @Query("SELECT * FROM users")
    fun getAllUsers(): kotlinx.coroutines.flow.Flow<List<UserEntity>>
}