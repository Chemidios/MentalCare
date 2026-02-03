package com.example.mentalcare.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalcare.model.DayRecord
import com.example.mentalcare.model.GoalEntity
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    dailyRecords: Map<String, DayRecord>,
    goals: List<GoalEntity>,
    onBack: () -> Unit
) {
    var selectedRecord by remember { mutableStateOf<DayRecord?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    // Para saber qué fecha filtrar
    var selectedDate by remember { mutableStateOf("") }
    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    // Variables de control de mes y año para LazyVerticalGrid
    val monthName = currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        ?.replaceFirstChar { it.uppercase() } ?: ""
    val year = currentCalendar.get(Calendar.YEAR)
    val month = currentCalendar.get(Calendar.MONTH)
    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val firstDayOfMonth = remember(currentCalendar) {
        val tempCal = currentCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        (tempCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    if (showDialog) {
        // Buscamos los objetivos que coincidan con la fecha pulsada
        val goalsForDay = goals.filter { it.date == selectedDate }
        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) { Text("Cerrar") }
            },
            title = {
                Text(
                    text = "Detalles del día ($selectedDate)",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                ) {
                    // Apartado de diario para el Dialog
                    if (selectedRecord != null) {
                        Text("📔 Diario ${selectedRecord!!.emoji}:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = selectedRecord!!.journalText.ifEmpty { "No escribiste nada este día." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        //Apartado de Hábitos para el Dialog
                        Text("✅ Hábitos realizados:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (selectedRecord!!.completedHabits.isEmpty()) {
                            Text("No se completaron hábitos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else {
                            selectedRecord!!.completedHabits.forEach { habit ->
                                Text("• $habit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Apartado del Dialog de Objetivos
                    Text("🎯 Objetivos del día:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (goalsForDay.isEmpty()) {
                        Text("No hay objetivos para hoy.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        goalsForDay.forEach { goal ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (goal.isCompleted) Color(0xFF4CAF50) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Calendario", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = currentCalendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    currentCalendar = newCal
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.onSurface)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(onClick = {
                    val newCal = currentCalendar.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    currentCalendar = newCal
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val labels = listOf("L", "M", "X", "J", "V", "S", "D")
                items(labels) { dayLabel ->
                    Text(
                        text = dayLabel,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(firstDayOfMonth) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }

                items(daysInMonth) { index ->
                    val day = index + 1
                    val monthValue = month + 1
                    val dateKey = String.format("%04d-%02d-%02d", year, monthValue, day)

                    val record = dailyRecords[dateKey]
                    val hasGoal = goals.any { it.date == dateKey }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = if (record != null) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable(
                                // Ahora habilitamos el click si hay registro O si hay objetivo
                                enabled = record != null || hasGoal,
                                onClick = {
                                    // Guardamos la fecha pulsada
                                    selectedDate = dateKey
                                    // Puede ser null, el diálogo lo gestiona
                                    selectedRecord = record
                                    showDialog = true
                                }
                            )
                    ) {
                        DayItem(day = day, emoji = record?.emoji ?: "", hasGoal = hasGoal)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Se incluye una tarjeta informativa inferior que guía al usuario sobre
            // la interactividad del calendario, mejorando la usabilidad.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = "💡 Pulsa en los días con emoji para ver tu diario y hábitos.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DayItem(day: Int, emoji: String, hasGoal: Boolean) {
    val hasEmoji = emoji.isNotEmpty()
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = day.toString(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (hasEmoji) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        if (hasEmoji) {
            Text(
                text = emoji,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp
            )
        }

        if (hasGoal) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.tertiary, shape = CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}