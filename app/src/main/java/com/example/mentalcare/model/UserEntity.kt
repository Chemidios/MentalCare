package com.example.mentalcare.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Almacena los datos de autenticación y nivel de acceso (admin/estándar).
@Entity(tableName = "users")
data class UserEntity(
    // Se opta por el username como PrimaryKey para simplificar las relaciones
    // y evitar duplicidad de nombres de usuario en el registro.
    @PrimaryKey val username: String,
    val email: String,
    val password: String,
    // Campo de control para habilitar funcionalidades exclusivas del administrador
    val isAdmin: Boolean = false
)