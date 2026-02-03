package com.example.mentalcare.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Clase de datos que utiliza la interfaz Parcelable para permitir el paso de
// información del usuario activo entre pantallas (NavHost) de forma eficiente.
@Parcelize
data class UserSession(
    val username: String,    val isAdmin: Boolean
) : Parcelable

