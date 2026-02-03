package com.example.mentalcare.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad diseñada para el seguimiento de metas a largo plazo.
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Relación con el usuario propietario de la meta
    val userId: String,
    val title: String,
    // Fecha límite almacenada en formato String para compatibilidad con filtros SQL
    val date: String,
    // Flag de estado para separar visualmente objetivos de logros
    val isCompleted: Boolean = false
)