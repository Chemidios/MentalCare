package com.example.mentalcare.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.mentalcare.data.AppDatabase
import com.example.mentalcare.model.GoalEntity
import android.provider.Settings
import androidx.activity.compose.BackHandler
import com.example.mentalcare.model.UserEntity
import com.example.mentalcare.model.UserSession
import kotlinx.coroutines.launch

// Paleta de Colores
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1A237E),
    surface = Color(0xFF121212),
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00B0FF),
    surface = Color.White,
    onSurface = Color.Black
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    // Inicialización de contexto y SharedPreferences
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("mentalcare_prefs", Context.MODE_PRIVATE) }
    var permissionGranted by remember { mutableStateOf(false) }
    // Lanzador para solicitar notificaciones
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            Log.d("AppNavigation", "Permiso de notificaciones concedido")
        } else {
            Log.d("AppNavigation", "Permiso de notificaciones denegado")
        }
    }

    LaunchedEffect(Unit) {
        // Solicitud de permiso de notificaciones POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Si es Android < 13, marcar como concedido automáticamente
            permissionGranted = true
        }
    }

// Cuando el permiso es concedido, verificar alarmas exactas
    LaunchedEffect(permissionGranted) {
        if (permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Verificar si NO tiene el permiso de alarmas exactas
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                    Log.d("AppNavigation", "Abriendo ajustes de alarmas exactas")
                } catch (e: Exception) {
                    Log.e("AppNavigation", "No se pudo abrir ajustes de alarmas: ${e.message}")
                }
            } else {
                Log.d("AppNavigation", "Ya tiene permiso de alarmas exactas")
            }
        }
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Preferencias de usuario para Tema y Fuente
    // Usamos rememberSaveable para que no se pierdan al rotar la pantalla
    var darkTheme by rememberSaveable { mutableStateOf(sharedPrefs.getBoolean("dark_mode", false)) }
    var fontSizeMultiplier by rememberSaveable { mutableStateOf(sharedPrefs.getFloat("font_size_multiplier", 1.0f)) }

    // Control de Tipografía
    val defaultTypography = Typography()
    val customTypography = Typography(
        bodyLarge = defaultTypography.bodyLarge.copy(fontSize = defaultTypography.bodyLarge.fontSize * fontSizeMultiplier),
        bodyMedium = defaultTypography.bodyMedium.copy(fontSize = defaultTypography.bodyMedium.fontSize * fontSizeMultiplier),
        titleLarge = defaultTypography.titleLarge.copy(fontSize = defaultTypography.titleLarge.fontSize * fontSizeMultiplier),
        labelLarge = defaultTypography.labelLarge.copy(fontSize = defaultTypography.labelLarge.fontSize * fontSizeMultiplier),
        labelMedium = defaultTypography.labelMedium.copy(fontSize = defaultTypography.labelMedium.fontSize * fontSizeMultiplier)
        )

    // Gestión de Colores en función de si es modo oscuro o no
    val primaryColor = if (darkTheme) Color(0xFF1A237E) else Color(0xFF00B0FF)
    val dividerColor = if (darkTheme) Color.Gray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f)

    // Variable para guardar el usuario actual
    var currentUser by rememberSaveable {
        val savedUsername = sharedPrefs.getString("last_username", null)
        val savedIsAdmin = sharedPrefs.getBoolean("last_is_admin", false)
        mutableStateOf(if (savedUsername != null) UserSession(savedUsername, savedIsAdmin) else null)
    }

    // Inicialización de la base de datos Room y DAOs
    val db = remember { Room.databaseBuilder(context, AppDatabase::class.java, "mentalcare-db").fallbackToDestructiveMigration().build() }
    val userDao = db.userDao()

    // Crear usuarios de prueba si no existen
    LaunchedEffect(Unit) {
        // Usuario admin
        val existingAdmin = userDao.getUserByUsername("admin")
        if (existingAdmin == null) {
            userDao.registerUser(
                UserEntity(
                    username = "admin",
                    email = "admin@mentalcare.com",
                    password = "admin123",
                    isAdmin = true
                )
            )
            Log.d("AppNavigation", "Usuario admin creado")
        }

        // Usuario prueba
        val existingPrueba = userDao.getUserByUsername("prueba")
        if (existingPrueba == null) {
            userDao.registerUser(
                UserEntity(
                    username = "prueba",
                    email = "prueba@mentalcare.com",
                    password = "prueba123",
                    isAdmin = false
                )
            )
            Log.d("AppNavigation", "Usuario prueba creado")
        }
    }
    val dailyDao = db.dailyRecordDao()
    val goalDao = db.goalDao()

    val recordsList by dailyDao.getRecordsByUser(currentUser?.username ?: "").collectAsState(initial = emptyList())
    val dailyRecords = remember(recordsList) { recordsList.associateBy { it.dateKey } }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    MaterialTheme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme, typography = customTypography) {
        Surface(color = MaterialTheme.colorScheme.background) {
            // Estado de autenticación
            var isAuthenticated by rememberSaveable { mutableStateOf(false) }
            val userPinKey = "security_pin_${currentUser?.username}"
            val userBiometricKey = "use_biometric_${currentUser?.username}"
            val securityEnabled = sharedPrefs.getBoolean(userBiometricKey, false) ||
                    sharedPrefs.getString(userPinKey, null) != null

            // Determinación de pantalla inicial
            val startDestination = when {
                currentUser == null -> "login"
                securityEnabled && !isAuthenticated -> "biometric_auth"
                else -> "home"
            }


            // Define qué pantalla se muestra según si el usuario está logueado o tiene seguridad activa
            NavHost(navController = navController, startDestination = startDestination) {

                composable("login") {
                    LoginScreen(userDao = userDao, onLoginSuccess = { session ->
                        currentUser = session
                        sharedPrefs.edit()
                            .putString("last_username", session.username)
                            .putBoolean("last_is_admin", session.isAdmin)
                            .apply()

                        // Verificar si hay seguridad habilitada
                        val loginUserPinKey = "security_pin_${session.username}"
                        val loginUserBiometricKey = "use_biometric_${session.username}"
                        val hasSecurityEnabled = sharedPrefs.getBoolean(loginUserBiometricKey, false) ||
                                sharedPrefs.getString(loginUserPinKey, null) != null

                        // Comprueba si el usuario tiene seguridad habilitada y si no está logueado
                        if (hasSecurityEnabled && !isAuthenticated) {
                            navController.navigate("biometric_auth") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            isAuthenticated = true
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    })
                }

                // Rutas de login, auth, home, objetives,settings y admin_panel
                composable("biometric_auth") {
                    BiometricAuthScreen(
                        username = currentUser?.username ?: "",
                        onAuthSuccess = {
                            isAuthenticated = true
                            navController.navigate("home") {
                                popUpTo("biometric_auth") { inclusive = true }
                            }
                        },
                        onLogout = {
                            // Logout
                            sharedPrefs.edit()
                                .remove("last_username")
                                .remove("last_is_admin")
                                .apply()
                            currentUser = null
                            isAuthenticated = false
                            navController.navigate("login") {
                                popUpTo("biometric_auth") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    BackHandler(enabled = drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    }
                    // Creación de la barra lateral. Aquí se encontrarán los botones de navegación
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = primaryColor,
                                drawerContentColor = Color.White
                            ) {
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    "MentalCare",
                                    Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                HorizontalDivider(color = dividerColor)
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            Icons.Default.DateRange,
                                            null,
                                            tint = Color.White
                                        )
                                    },
                                    label = { Text("Mi Calendario", color = Color.White) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }; navController.navigate(
                                        "calendar"
                                    )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = Color.Transparent
                                    )
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Star, null, tint = Color.White) },
                                    label = { Text("Mis Objetivos", color = Color.White) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }; navController.navigate(
                                        "goals"
                                    )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = Color.Transparent
                                    )
                                )

                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            null,
                                            tint = Color.White
                                        )
                                    },
                                    label = { Text("Ajustes", color = Color.White) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }; navController.navigate(
                                        "settings"
                                    )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = Color.Transparent
                                    )
                                )
                                // Si el usuario es administrador le aparece el panel de adminsitración.
                                if (currentUser?.isAdmin == true) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = dividerColor
                                    )
                                    Text(
                                        "Administración",
                                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )

                                    NavigationDrawerItem(
                                        icon = {
                                            Icon(
                                                Icons.Default.AdminPanelSettings,
                                                null,
                                                tint = Color.White
                                            )
                                        },
                                        label = {
                                            Text(
                                                "Estadísticas Globales",
                                                color = Color.White
                                            )
                                        },
                                        selected = false,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("admin_panel")
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            unselectedContainerColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        // Home Screen
                        MainHomeScreen(
                            user = currentUser,
                            dailyRecords = dailyRecords,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onSaveRecord = { record -> scope.launch { dailyDao.insertRecord(record) } },
                            topBarColor = primaryColor,
                            // Resetear autenticación
                            onLogout = {
                                sharedPrefs.edit()
                                    .remove("last_username")
                                    .remove("last_is_admin")
                                    .apply()
                                currentUser = null
                                isAuthenticated = false
                                navController.navigate("login") {
                                    popUpTo("home") {
                                        inclusive = true
                                    }
                                }
                            },
                        )
                    }
                }

                // Composable Calendar
                composable("calendar") {
                    if (securityEnabled && !isAuthenticated) {
                        LaunchedEffect(Unit) { navController.navigate("biometric_auth") }
                    }
                    else{
                        val goalsList by goalDao.getGoalsByUser(currentUser?.username ?: "")
                            .collectAsState(initial = emptyList())
                        CalendarScreen(dailyRecords = dailyRecords, goals = goalsList, onBack = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        })
                    }
                }

                // Composable logros
                composable("goals") {
                    if (securityEnabled && !isAuthenticated) {
                        LaunchedEffect(Unit) { navController.navigate("biometric_auth") }
                    }
                    else {
                        val goalsList by goalDao.getGoalsByUser(currentUser?.username ?: "")
                            .collectAsState(initial = emptyList())
                        GoalsScreen(
                            goals = goalsList,
                            onAddGoal = { title, date ->
                                scope.launch {
                                    goalDao.insertGoal(
                                        GoalEntity(
                                            userId = currentUser?.username ?: "",
                                            title = title,
                                            date = date
                                        )
                                    )
                                }
                            },
                            onDeleteGoal = { goal -> scope.launch { goalDao.deleteGoal(goal) } },
                            onToggleGoal = { goal ->
                                scope.launch {
                                    goalDao.updateGoal(
                                        goal.copy(
                                            isCompleted = !goal.isCompleted
                                        )
                                    )
                                }
                            },
                            isDark = darkTheme,
                            onBack = {
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }

                // Composable Panel de Administracion
                composable("admin_panel") {
                    if (securityEnabled && !isAuthenticated) {
                        LaunchedEffect(Unit) { navController.navigate("biometric_auth") }
                    }
                    else {
                        // Obtención todos los usuarios y registros para la pantalla de admin
                        val allUsers by userDao.getAllUsers().collectAsState(initial = emptyList())
                        val allRecords by dailyDao.getAllRecords()
                            .collectAsState(initial = emptyList())

                        AdminStatsScreen(
                            users = allUsers,
                            allRecords = allRecords,
                            onBack = {
                                if (navController.previousBackStackEntry != null) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }

                // Composable Settings
                composable("settings") {
                    SettingsScreen(
                        darkTheme = darkTheme,
                        onDarkThemeChange = {
                            darkTheme = it
                            sharedPrefs.edit().putBoolean("dark_mode", it).apply()
                        },
                        fontSizeMultiplier = fontSizeMultiplier,
                        onFontSizeChange = { newValue ->
                            fontSizeMultiplier = newValue
                            sharedPrefs.edit().putFloat("font_size_multiplier", newValue).apply()
                        },
                        primaryColor = primaryColor,
                        currentUser = currentUser!!,
                        onBack = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }
        }
    }
}

