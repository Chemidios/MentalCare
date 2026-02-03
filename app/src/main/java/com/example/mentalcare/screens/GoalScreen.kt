package com.example.mentalcare.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mentalcare.model.GoalEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    onAddGoal: (String, String) -> Unit,
    onToggleGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    isDark: Boolean,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var goalTitle by remember { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Pendientes", "Mis Logros")

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("Seleccionar fecha") }
    var selectedDateRaw by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Objetivos", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    goalTitle = ""
                    selectedDateText = "Seleccionar fecha"
                    selectedDateRaw = ""
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            val filteredGoals = if (selectedTab == 0) {
                goals.filter { !it.isCompleted }
            } else {
                goals.filter { it.isCompleted }
            }

            if (filteredGoals.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (selectedTab == 0) "No tienes objetivos pendientes" else "Aún no has cumplido ningún objetivo",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGoals) { goal ->
                        GoalItem(
                            goal = goal,
                            onToggle = { onToggleGoal(goal) },
                            onDelete = { onDeleteGoal(goal) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nuevo Objetivo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("¿Qué quieres lograr?", color = if (isDark) Color.White else MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedDateText,color = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank() && selectedDateRaw.isNotBlank()) {
                            onAddGoal(goalTitle, selectedDateRaw)
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = goalTitle.isNotBlank() && selectedDateRaw.isNotBlank()
                ) { Text("Añadir",color = Color.White) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Cancelar",color = Color.White) }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {        datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()

                    selectedDateRaw = date.toString()
                    selectedDateText = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }
                    showDatePicker = false
                }) { Text("OK", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Color.White) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    weekdayContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    titleContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,

                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = Color.White,

                    dayContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,

                    navigationContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    yearContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GoalItem(goal: GoalEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(goal.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            },
            supportingContent = {
                val dateFormatted = java.time.LocalDate.parse(goal.date)
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                Text("Vence: $dateFormatted", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingContent = {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (goal.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}