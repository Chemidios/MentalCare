package com.example.mentalcare.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalcare.data.UserDao
import com.example.mentalcare.model.UserEntity
import com.example.mentalcare.model.UserSession
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    userDao: UserDao,
    onLoginSuccess: (UserSession) -> Unit
) {
    var userText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estado para controlar la visibilidad del diálogo de registro
    var showRegisterDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "MentalCare", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = userText,
            onValueChange = { userText = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = passwordText,
            onValueChange = { passwordText = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    // Busca en la DB
                    val user = userDao.getUserByUsername(userText)
                    if (user != null && user.password == passwordText) {
                        // Pasamos TODOS los datos, incluyendo si es Admin
                        onLoginSuccess(UserSession(user.username, user.isAdmin))
                    } else {
                        errorMessage = "Usuario o contraseña incorrectos"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Al pulsar aquí, abrimos el diálogo
        Button(
            onClick = { showRegisterDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Registrarse",color = Color.White)
        }
    }

    // Mostrar el Diálogo si el estado es true
    if (showRegisterDialog) {
        RegisterDialog(
            userDao = userDao,
            onDismiss = { showRegisterDialog = false },
            onRegistrationSuccess = {
                showRegisterDialog = false
                errorMessage = "¡Registro con éxito! Inicia sesión ahora."
            }
        )
    }
}

@Composable
fun RegisterDialog(
    userDao: UserDao,
    onDismiss: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = user, onValueChange = { user = it }, label = { Text("Usuario") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") })
                TextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation()
                )
                TextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = { Text("Confirmar Contraseña") },
                    visualTransformation = PasswordVisualTransformation()
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        // Busca este bloque dentro de RegisterDialog en el Login Screen
        confirmButton = {
            Button(onClick = {
                // 1. Verificación de campos vacíos
                if (user.isBlank() || email.isBlank() || pass.isBlank()) {
                    errorMsg = "Rellena todos los campos"
                }
                // 2. Verificación de Email (debe contener @)
                else if (!email.contains("@")) {
                    errorMsg = "El correo electrónico no es válido (falta @)"
                }
                // 3. Verificación de longitud de contraseña (mínimo 8)
                else if (pass.length < 8) {
                    errorMsg = "La contraseña debe tener al menos 8 caracteres"
                }
                // 4. Verificación de coincidencia
                else if (pass != confirmPass) {
                    errorMsg = "Las contraseñas no coinciden"
                }

                // Ejecución en Corrutina para no bloquear el hilo de la UI
                else {
                    scope.launch {
                        try {
                            val newUser = UserEntity(
                                username = user,
                                email = email,
                                password = pass,
                                isAdmin = user.lowercase() == "admin"
                            )
                            userDao.registerUser(newUser)
                            onRegistrationSuccess()
                        } catch (e: Exception) {
                            errorMsg = "El usuario ya existe"
                        }
                    }
                }
            }) {
                Text("Confirmar Registro",color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}