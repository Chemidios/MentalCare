package com.example.mentalcare.data

import androidx.room.*
import com.example.mentalcare.model.DayRecord
import kotlinx.coroutines.flow.Flow
import com.example.mentalcare.model.UserEntity
import com.example.mentalcare.model.GoalEntity

// El retorno de tipo Flow permite que la UI se actualice en tiempo real
// cada vez que se inserta o modifica un registro en la tabla sin necesidad de recargar.
@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records WHERE userId = :userId")
    fun getRecordsByUser(userId: String): Flow<List<DayRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DayRecord)

    @Query("SELECT * FROM daily_records")
    fun getAllRecords(): kotlinx.coroutines.flow.Flow<List<DayRecord>>
}

// Clase principal de la persistencia de datos (RA5.a).
// @Database: Define las entidades de la BD y la versión del esquema.
// @TypeConverters: Permite a Room manejar tipos de datos complejos como Listas (RA5.a).
@Database(entities = [UserEntity::class, DayRecord::class, GoalEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun goalDao(): GoalDao
}