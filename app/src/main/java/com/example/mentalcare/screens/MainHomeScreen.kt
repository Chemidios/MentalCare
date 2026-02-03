package com.example.mentalcare.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mentalcare.model.UserSession
import com.example.mentalcare.components.QuoteCard
import com.example.mentalcare.data.MotivationManager
import com.example.mentalcare.model.DayRecord
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    user: UserSession?,
    dailyRecords: Map<String, DayRecord>,
    onSaveRecord: (DayRecord) -> Unit,
    onOpenDrawer: () -> Unit,
    onLogout: () -> Unit,
    topBarColor: Color
) {
    val dailyQuote = remember { MotivationManager.getDailyQuote() }

    // Generación de una dateKey única (YYYY-MM-DD) para indexar en la BD
    val dateKey = remember {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        String.format("%04d-%02d-%02d", y, m, d)
    }

    val todayRecordFromDb = dailyRecords[dateKey]

    // Si no hay registro, buscamos los hábitos del último día registrado
    // Esto sirve para que el usuario no tenga que escribirlos de nuevo cada día.
    val lastHabitsList = remember(dailyRecords) {
        // Si la base de datos aún está cargando (está vacía), no tomamos una decisión todavía
        if (dailyRecords.isEmpty()) return@remember null
        dailyRecords.values
            .sortedByDescending { it.dateKey }
            .firstOrNull { it.allHabits.isNotEmpty() }
            ?.allHabits
    }

    // Definimos la lista final (si no hay nada en la BD, usamos los de por defecto)
    val finalHabits = lastHabitsList ?: listOf("Meditar 10 min", "Beber 2L agua", "Hacer ejercicio")

    val todayRecord = todayRecordFromDb ?: DayRecord(
        dateKey = dateKey,
        userId = user?.username ?: "",
        allHabits = finalHabits
    )

    // Garantiza la integridad de los datos: si el día no existe en la BD, se crea automáticamente
    // para que el usuario siempre tenga un lienzo donde escribir.
    LaunchedEffect(todayRecordFromDb, dailyRecords, dateKey) {
        // Solo actuamos si dailyRecords ya tiene los datos de la BD
        if (dailyRecords.isNotEmpty() && todayRecordFromDb == null) {
            onSaveRecord(todayRecord)
        }
    }

    var journalText by remember(todayRecord.journalText) { mutableStateOf(todayRecord.journalText) }

    // Estado para el campo de texto de nuevo hábito
    var newHabitName by remember { mutableStateOf("") }

    // Uso de los hábitos que ya vienen en el registro o unos por defecto si está vacío
    val currentHabits = remember(todayRecord.allHabits) { todayRecord.allHabits }

    // CenterAlignedTopAppBar donde irá el icono del menú y el botón de logout
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MentalCare", fontWeight = FontWeight.Bold, color = Color.White) },
                // Icono de Menú
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Menú", tint = Color.White)
                    }
                },
                // Icono de Logout
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = topBarColor)
            )
        },
        ) { padding ->
        // Componente reutilizable: Muestra frase motivacional dinámica
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                QuoteCard(quote = dailyQuote,topBarColor == Color(0xFF1A237E),)
            }

            // Emoji
            item {
                Text("¿Cómo te sientes hoy?", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val emojis = listOf("😢", "😐", "😊", "😁", "🤩")
                    // Al pulsar un emoji, disparamos onSaveRecord para persistir el cambio
                    emojis.forEach { emoji ->
                        FilterChip(
                            selected = todayRecord.emoji == emoji,
                            onClick = { onSaveRecord(todayRecord.copy(emoji = emoji)) },
                            label = { Text(emoji, style = MaterialTheme.typography.headlineSmall) }
                        )
                    }
                }
            }

            // Diario
            item {
                Text("Mi Diario Personal", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = journalText,
                    onValueChange = {
                        journalText = it
                        onSaveRecord(todayRecord.copy(journalText = it))
                    },
                    placeholder = { Text("¿Cómo fue tu día?") },
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp)
                )
            }


            item {
                Text("Mis Hábitos de Hoy", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Creación de Nuevos Habitos y guardado en la lista 'allHabits'
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Nuevo hábito") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newHabitName.isNotBlank()) {
                                val updatedAllHabits = currentHabits + newHabitName
                                onSaveRecord(todayRecord.copy(allHabits = updatedAllHabits))
                                newHabitName = ""
                            }
                        },
                        enabled = newHabitName.isNotBlank()
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Añadir", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }


            // Lista de Hábitos Dinámica
            // Permite añadir a la lista 'allHabits' y marcarlos como 'completed'
            items(currentHabits) { habitName ->
                val isDone = todayRecord.completedHabits.contains(habitName)
                HabitItem(
                    name = habitName,
                    isDone = isDone,
                    onCheckedChange = { checked ->
                        val newList = if (checked) {
                            todayRecord.completedHabits + habitName
                        } else {
                            todayRecord.completedHabits - habitName
                        }
                        onSaveRecord(todayRecord.copy(completedHabits = newList, allHabits = currentHabits))
                    },
                    onDelete = {
                        // Lógica de BORRADO
                        val updatedAllHabits = currentHabits - habitName
                        val updatedCompletedHabits = todayRecord.completedHabits - habitName
                        onSaveRecord(todayRecord.copy(
                            allHabits = updatedAllHabits,
                            completedHabits = updatedCompletedHabits
                        ))
                    }
                )
            }
        }
    }
}

@Composable
fun HabitItem(name: String, isDone: Boolean, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            Checkbox(checked = isDone, onCheckedChange = onCheckedChange,)
            Text(text = name, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}