package com.example.mentalcare.screens

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// Esta pantalla actúa como un interceptor de seguridad.
// Asegura que los datos privados del diario no sean visibles tras el login
// sin una validación adicional (PIN o Biometría).
@Composable
fun BiometricAuthScreen(
    username: String,
    onAuthSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val sharedPrefs = context.getSharedPreferences("mentalcare_prefs", Context.MODE_PRIVATE)


    // Recuperamos las claves de seguridad específicas del usuario actual
    val userPinKey = "security_pin_$username"
    val userBiometricKey = "use_biometric_$username"
    val useBiometric = sharedPrefs.getBoolean(userBiometricKey, false)
    val savedPin = sharedPrefs.getString(userPinKey, null)

    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPinInput by remember { mutableStateOf(!useBiometric) }

    // Si el usuario tiene activada la biometría, lanzamos el prompt automáticamente al entrar.
    LaunchedEffect(Unit) {
        if (useBiometric && activity != null) {
            showBiometricPrompt(
                activity = activity,
                onSuccess = onAuthSuccess,
                onError = { showPinInput = true }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Botón Log Out: Permite volver atrás si el usuario olvidó su PIN o no es el titular
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar sesión",
                    // Usamos el color onSurface para que  se vea negro en light y blanco en dark
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MentalCare",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Autenticación requerida",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Interfaz de PIN (Método de seguridad alternativo)
                if (showPinInput && savedPin != null) {
                    TextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4) {
                                pinInput = it
                                errorMessage = null
                            }
                        },
                        label = {
                            Text(
                                "PIN de 4 dígitos",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        // Control de colores
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        // Teclado numérico y ocultación de caracteres
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Retroalimentación inmediata en caso de error
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (pinInput == savedPin) {
                                onAuthSuccess()
                            } else {
                                errorMessage = "PIN incorrecto"
                                pinInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        // El botón solo se activa con 4 dígitos (validación previa)
                        enabled = pinInput.length == 4,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Desbloquear")
                    }
                    // / Botón para reintentar biometría si el usuario la canceló por error
                    if (useBiometric && activity != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                showBiometricPrompt(activity, onAuthSuccess, { errorMessage = it })
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Usar huella/Face ID")
                        }
                    }
                }
            }
        }
    }
}

// Implementación de la API de Biometría de Android.
// Permite una interacción natural (huella/rostro) que reduce la fricción del usuario.
private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val biometricManager = BiometricManager.from(activity)

    // Verificación de disponibilidad de hardware biométrico
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Notifica el éxito al flujo de navegación
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Autenticación falló")
                    }
                }
            )

            // Configuración del diálogo nativo del sistema (UI consistente con Android)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación requerida")
                .setSubtitle("Usa tu huella o Face ID para acceder")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
        else -> {
            onError("Biometría no disponible")
        }
    }
}