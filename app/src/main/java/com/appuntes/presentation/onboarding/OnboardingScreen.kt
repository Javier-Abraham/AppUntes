package com.appuntes.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class OnboardingPage(
    val icon: ImageVector,
    val titulo: String,
    val descripcion: String
)

val ONBOARDING_PAGES = listOf(
    OnboardingPage(Icons.Filled.School, "Organizá tus materias",
        "Registrá todas tus materias, profesores y horarios en un solo lugar."),
    OnboardingPage(Icons.Filled.Assignment, "Gestioná tus tareas",
        "Creá tareas, asignales prioridad y fecha de entrega. Recibí recordatorios automáticos."),
    OnboardingPage(Icons.Filled.CalendarMonth, "Tu calendario académico",
        "Visualizá todos tus vencimientos en un calendario y planificá tu semana.")
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            AnimatedContent(targetState = currentPage, label = "icon") {
                Box(
                    modifier = Modifier.size(160.dp).clip(RoundedCornerShape(40.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(ONBOARDING_PAGES[it].icon, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(80.dp))
                }
            }
            Spacer(Modifier.height(48.dp))
            AnimatedContent(targetState = currentPage, label = "title") {
                Text(ONBOARDING_PAGES[it].titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))
            AnimatedContent(targetState = currentPage, label = "desc") {
                Text(ONBOARDING_PAGES[it].descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp))
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)) {
                repeat(ONBOARDING_PAGES.size) { index ->
                    Box(modifier = Modifier.clip(CircleShape)
                        .background(if (index == currentPage) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant)
                        .size(if (index == currentPage) 24.dp else 8.dp, 8.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                if (currentPage < ONBOARDING_PAGES.size - 1) {
                    TextButton(onClick = onFinish) { Text("Saltar") }
                    Button(onClick = { currentPage++ }, shape = RoundedCornerShape(12.dp)) {
                        Text("Siguiente")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowForward, null)
                    }
                } else {
                    Button(onClick = onFinish, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("¡Empezar!", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
