// MainActivity.kt
package com.example.mentalcare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.mentalcare.screens.AppNavigation

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creacion de Canal de Notificaciones
        createNotificationChannel()
        // Forzamos el locale a Español
        // para garantizar que los componentes nativos (DatePicker/TimePicker)
        // se muestren en el idioma del usuario
        val locale = java.util.Locale("es", "ES")
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        setContent {
            AppNavigation()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios MentalCare"
            val descriptionText = "Notificaciones para recordar registrar tu estado emocional"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("mentalcare_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
