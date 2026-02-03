package com.example.mentalcare.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import com.example.mentalcare.components.NotificationReceiver
import com.example.mentalcare.model.UserSession
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserSession?,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    fontSizeMultiplier: Float,
    onFontSizeChange: (Float) -> Unit,
    primaryColor: Color,
    onBack: () -> Unit
) {
    // Generamos claves únicas por usuario para que los ajustes de PIN y Biometría
    // sean independientes entre las diferentes cuentas registradas.
    val userPinKey = "security_pin_${currentUser?.username}"
    val userBiometricKey = "use_biometric_${currentUser?.username}"
    // Estado del Scroll
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("mentalcare_prefs", Context.MODE_PRIVATE)

    // Usamos 'rememberSaveable' para que los cambios en los switches y horas
    // no se pierdan si el usuario gira la pantalla.
    // Estado del interruptor general de notificaciones
    var notificationsEnabled by rememberSaveable {
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", true))
    }
    // Almacena la hora seleccionada para el recordatorio (formato 24h)
    var notificationHour by rememberSaveable {
        mutableStateOf(sharedPrefs.getInt("notification_hour", 20))
    }
    // Almacena los minutos seleccionados
    var notificationMinute by rememberSaveable {
        mutableStateOf(sharedPrefs.getInt("notification_minute", 0))
    }
    // Controla la visibilidad del selector de hora (TimePicker)
    var showTimePicker by remember { mutableStateOf(false) }
    // Mantiene el estado interno del componente TimePicker de Material3
    val timePickerState = rememberTimePickerState(
        initialHour = notificationHour,
        initialMinute = notificationMinute,
        is24Hour = true
    )
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = primaryColor.copy(alpha = 0.7f),
        uncheckedThumbColor = if (darkTheme) Color.LightGray else Color.White
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajustes", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = primaryColor)
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(scrollState)) {

            // Visualizacion
            Text(
                "Visualización",
                style = MaterialTheme.typography.labelLarge,
                color = if (darkTheme) Color.White else primaryColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ListItem(
                        headlineContent = { Text("Modo Oscuro") },
                        trailingContent = {
                            Switch(
                                checked = darkTheme,
                                onCheckedChange = onDarkThemeChange,
                                colors = switchColors
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )

                    // Control de tamaño de fuente mediante un Slider que afecta a la tipografía
                    // global de la app, permitiendo adaptabilidad visual.
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tamaño de fuente", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = fontSizeMultiplier,
                            onValueChange = onFontSizeChange,
                            valueRange = 0.8f..1.5f,
                            steps = 5,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "Ejemplo de tamaño de texto",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notificaciones
            Text(
                "Notificaciones",
                style = MaterialTheme.typography.labelLarge,
                color = if (darkTheme) Color.White else primaryColor,
            )
            Card(
                Modifier.padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Recordatorio diario") },
                        trailingContent = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = {
                                    notificationsEnabled = it
                                    sharedPrefs.edit()
                                        .putBoolean("notifications_enabled", it)
                                        .apply()
                                    scheduleNotification(
                                        context,
                                        notificationHour,
                                        notificationMinute,
                                        it
                                    )
                                }
                            )
                        }
                    )
                    if (notificationsEnabled) {
                        ListItem(
                            headlineContent = { Text("Hora") },
                            supportingContent = {
                                Text(
                                    String.format(
                                        "%02d:%02d",
                                        notificationHour,
                                        notificationMinute
                                    )
                                )
                            },
                            modifier = Modifier.clickable { showTimePicker = true }
                        )
                    }
                }
            }

            // Verificación de 'Exact Alarm' para Android 12+.
            // Si el permiso es revocado, la app guía al usuario directamente a los ajustes del sistema.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    Card(
                        modifier = Modifier.padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚠️ Permiso requerido", color = Color(0xFFD32F2F))
                            Text(
                                "Activa las alarmas exactas para recibir notificaciones.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = {
                                context.startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            }) { Text("Activar") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Seguridad",
                style = MaterialTheme.typography.labelLarge,
                color = if (darkTheme) Color.White else primaryColor,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Sincroniza la UI con las preferencias guardadas en SharedPreferences
            var useBiometric by rememberSaveable {
                mutableStateOf(sharedPrefs.getBoolean(userBiometricKey, false))
            }
            // Comprueba si el usuario tiene un PIN configurado comparándolo con null
            var usePin by rememberSaveable {
                mutableStateOf(sharedPrefs.getString(userPinKey, null) != null)
            }
            // Controla el diálogo emergente para introducir el nuevo PIN
            var showPinDialog by remember { mutableStateOf(false) }

            Card(Modifier.padding(vertical = 8.dp)) {
                Column {
                    // Switch de autenticación biométrica
                    ListItem(
                        headlineContent = { Text("Huella/Face ID") },
                        supportingContent = { Text("Requiere autenticación al abrir la app") },
                        trailingContent = {
                            Switch(
                                checked = useBiometric,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        showBiometricPrompt(context) { success ->
                                            if (success) {
                                                useBiometric = true
                                                sharedPrefs.edit().putBoolean(userBiometricKey, true).apply()
                                            }
                                        }
                                    } else {
                                        useBiometric = false
                                        sharedPrefs.edit().putBoolean(userBiometricKey, false).apply()
                                    }
                                }
                            )
                        }
                    )

                    HorizontalDivider()

                    // Configurar PIN
                    ListItem(
                        headlineContent = { Text(if (usePin) "Cambiar PIN" else "Configurar PIN") },
                        supportingContent = { Text("PIN de 4 dígitos como alternativa") },
                        modifier = Modifier.clickable { showPinDialog = true }
                    )
                }
            }

// Diálogo para configurar PIN
            if (showPinDialog) {
                SetPinDialog(
                    currentPin = sharedPrefs.getString(userPinKey, null),
                    onDismiss = { showPinDialog = false },
                    isDark = darkTheme,
                    onPinSet = { newPin ->
                        sharedPrefs.edit().putString(userPinKey, newPin).apply()
                        usePin = newPin != null
                        showPinDialog = false
                    }
                )
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                isDark = darkTheme,
                onDismissRequest = { showTimePicker = false },
                onConfirm = {
                    notificationHour = timePickerState.hour
                    notificationMinute = timePickerState.minute
                    sharedPrefs.edit()
                        .putInt("notification_hour", notificationHour)
                        .putInt("notification_minute", notificationMinute)
                        .apply()
                    scheduleNotification(context, notificationHour, notificationMinute, notificationsEnabled)
                    showTimePicker = false
                }
            ) {
                TimeInput(state = timePickerState)
            }
        }
    }
}

@Composable
fun TimePickerDialog(
isDark: Boolean,
onDismissRequest: () -> Unit,
onConfirm: () -> Unit,
content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = if (isDark) Color(0xFF2C2C2C) else MaterialTheme.colorScheme.surface,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK", color = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("CANCELAR", color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray)
            }
        },
        text = { content() }
    )
}

fun scheduleNotification(context: Context, hour: Int, minute: Int, enabled: Boolean) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (!enabled) {
        alarmManager.cancel(pendingIntent)
        return
    }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    } catch (e: Exception) { Log.e("Schedule", e.message ?: "") }
}

@Composable
fun SetPinDialog(
    currentPin: String?,
    onDismiss: () -> Unit,
    isDark: Boolean,
    onPinSet: (String?) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (currentPin == null) "Configurar PIN" else "Cambiar PIN",
                color = if (isDark) Color.White else Color.Unspecified
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Introduce 4 dígitos",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text("Nuevo PIN", color = if (isDark) Color.White else MaterialTheme.colorScheme.primary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4) confirmPin = it },
                    label = { Text("Confirmar PIN", color = if (isDark) Color.White else MaterialTheme.colorScheme.primary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    // Control de colores
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Color.White else Color.Gray,
                        focusedLabelColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (isDark) Color.White else Color.Gray,
                        cursorColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Row {
                if (currentPin != null) {
                    TextButton(onClick = { onPinSet(null) }) {
                        Text("QUITAR PIN", color = Color.Red)
                    }
                }
                TextButton(
                    onClick = { if (pin == confirmPin && pin.length == 4) onPinSet(pin) },
                    enabled = pin.length == 4 && pin == confirmPin
                ) {
                    Text(
                        "GUARDAR",
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = if (isDark) Color.White else Color.Gray)
            }
        }
    )
}


// Planteamiento biometria
fun showBiometricPrompt(context: Context, onResult: (Boolean) -> Unit) {
    val activity = context as? FragmentActivity
    if (activity == null) {
        onResult(false)
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onResult(false)
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación necesaria")
        .setSubtitle("Usa tu huella para acceder")
        .setNegativeButtonText("Cancelar")
        .build()

    biometricPrompt.authenticate(promptInfo)
}