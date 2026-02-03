package com.example.mentalcare.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mentalcare.model.DayRecord
import com.example.mentalcare.model.UserEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(
    // Lista de users, de records y uso de onBack
    users: List<UserEntity>,
    allRecords: List<DayRecord>,
    onBack: () -> Unit
) {
    // Calcular el emoji más usado
    // Filtración y agrupamos los registros de todos los usuarios para hallar la moda
    val mostUsedEmoji = remember(allRecords) {
        if (allRecords.isEmpty()) "N/A"
        else {
            allRecords
                // Extraemos solo los emojis
                .map { it.emoji }
                // Los agrupamos
                .groupBy { it }
                // Buscamos el grupo más grande
                .maxByOrNull { it.value.size }
                ?.key ?: "N/A"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Panel de Control", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Estadísticas Globales", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Muestra de usuarios registrados
                StatisticCard("Total Usuarios", "${users.size}", Modifier.weight(1f))

                // Muestra de emoji mas usado
                StatisticCard("Humor General", mostUsedEmoji, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Directorio de Usuarios", style = MaterialTheme.typography.titleMedium)

            // Directorio de usuarios registrados en el sistema
                LazyColumn(modifier = Modifier.weight(1f)) {
                items(users) { user ->
                    ListItem(
                        headlineContent = { Text(user.username) },
                        supportingContent = { Text(user.email) },
                        leadingContent = { Icon(Icons.Default.Person, null) }
                    )
                    HorizontalDivider()
                }
            }

            Button(onClick = onBack, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)) {
                Text("Cerrar Panel", color = Color.White)
            }
        }
    }
}

@Composable
fun StatisticCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}