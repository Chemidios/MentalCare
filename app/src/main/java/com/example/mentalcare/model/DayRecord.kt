package com.example.mentalcare.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad que representa el registro diario de bienestar de un usuario.
// Se utiliza una 'dateKey' (YYYY-MM-DD) para indexar de forma única el registro por día.
@Entity(tableName = "daily_records")
data class DayRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Clave de búsqueda para el calendario
    val dateKey: String,
    // Clave ajena vinculada a UserEntity.username
    val userId: String,
    val emoji: String = "",
    val journalText: String = "",
    // Estas listas se transforman a String mediante Converters para su almacenamiento en SQLite
    val completedHabits: List<String> = emptyList(),
    val allHabits: List<String> = emptyList()
)

