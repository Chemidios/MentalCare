package com.example.mentalcare.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mentalcare.screens.scheduleNotification

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        // Definimos prioridad alta para que aparezca el banner
        val builder = NotificationCompat.Builder(context, "mentalcare_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("MentalCare 🌟")
            .setContentText("Es momento de registrar cómo te sientes hoy.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1, builder.build())
            Log.d("NotificationReceiver", "Notificación enviada")
        } catch (e: SecurityException) {
            Log.e("NotificationReceiver", "Error de seguridad: ${e.message}")
        }

        val sharedPrefs = context.getSharedPreferences("mentalcare_prefs", Context.MODE_PRIVATE)
        val enabled = sharedPrefs.getBoolean("notifications_enabled", true)

        // Si las notificaciones están activas, programamos la del día siguiente
        // para crear un ciclo infinito de recordatorios diarios.
        if (enabled) {
            val hour = sharedPrefs.getInt("notification_hour", 20)
            val minute = sharedPrefs.getInt("notification_minute", 0)
            scheduleNotification(context, hour, minute, true)
            Log.d("NotificationReceiver", "Reprogramada para mañana a las $hour:$minute")
        }
    }
}