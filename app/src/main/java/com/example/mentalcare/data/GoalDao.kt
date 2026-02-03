package com.example.mentalcare.data

import androidx.room.*
import com.example.mentalcare.model.GoalEntity
import kotlinx.coroutines.flow.Flow

// Define las consultas SQL necesarias para gestionar las metas de los usuarios.
@Dao
interface GoalDao {
    // Retorna un Flow que emite automáticamente la nueva lista de objetivos
    // cada vez que la tabla 'goals' sufre un cambio.
    @Query("SELECT * FROM goals WHERE userId = :userId")
    fun getGoalsByUser(userId: String): Flow<List<GoalEntity>>

    // Inserta un nuevo objetivo o reemplaza uno existente si hay conflicto de IDs.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    // Elimina físicamente un objetivo de la base de datos SQLite.
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // Actualiza el estado del objetivo (por ejemplo, al marcarlo como completado).
    @Update
    suspend fun updateGoal(goal: GoalEntity)
}