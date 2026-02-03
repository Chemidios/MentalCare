package com.example.mentalcare.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

// Reutilizable: Tarjeta diseñada para mostrar frases motivacionales.
// Está desacoplada de la lógica de datos, recibiendo la información por parámetros,
// lo que permite usarla en la Home o en futuras secciones de la app.
@Composable
fun QuoteCard(quote: String,isDarkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Frase del día",
                style = MaterialTheme.typography.labelMedium,
                color = if (isDarkMode) Color.White else MaterialTheme.colorScheme.primary            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )
        }
    }
}