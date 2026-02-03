package com.example.mentalcare.data

import java.util.Calendar

// Objeto Singleton que gestiona la selección de frases motivacionales.
// Independiente de la UI, permitiendo cambiar el set de frases sin tocar las pantallas.
object MotivationManager {
    private val frases = listOf(
        "El bienestar no es un destino, es un camino.",
        "Cada día es una nueva oportunidad para cuidar de ti.",
        "Pequeños hábitos hoy, grandes cambios mañana.",
        "Tu salud mental es una prioridad, no un lujo.",
        "Sé amable contigo mismo hoy.",
        "La constancia es la clave del éxito en tus rutinas.",
        "Respira, enfócate y sigue adelante."
    )

    // Selecciona una frase basada en el día del año para asegurar
    // que el usuario vea una frase distinta cada día.
    fun getDailyQuote(): String {
        // Obtencion dia del año
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        // Uso el resto de la división para asegurar que el índice siempre exista
        val index = dayOfYear % frases.size
        return frases[index]
    }
}

