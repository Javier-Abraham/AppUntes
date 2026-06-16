package com.appuntes.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appuntes.domain.model.Materia
import com.appuntes.domain.model.PrioridadTarea
import com.appuntes.domain.model.Tarea
import com.appuntes.presentation.theme.PrioridadAlta
import com.appuntes.presentation.theme.PrioridadBaja
import com.appuntes.presentation.theme.PrioridadMedia
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToMaterias: () -> Unit,
    onNavigateToTareas: () -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onTareaClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hoy = LocalDate.now()
    val diaSemana = hoy.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "AR"))
        .replaceFirstChar { it.uppercase() }
    val fechaFormateada = hoy.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "AR")))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola 👋", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$diaSemana, $fechaFormateada",
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPerfil) {
                        Icon(Icons.Filled.AccountCircle, "Perfil", modifier = Modifier.size(32.dp))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Filled.Home, "Inicio") },
                    label = { Text("Inicio") }, selected = true, onClick = {})
                NavigationBarItem(icon = { Icon(Icons.Outlined.Book, "Materias") },
                    label = { Text("Materias") }, selected = false, onClick = onNavigateToMaterias)
                NavigationBarItem(icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, "Tareas") },
                    label = { Text("Tareas") }, selected = false, onClick = onNavigateToTareas)
                NavigationBarItem(icon = { Icon(Icons.Outlined.CalendarMonth, "Agenda") },
                    label = { Text("Agenda") }, selected = false, onClick = onNavigateToCalendario)
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround) {
                        StatItem(uiState.materias.size.toString(), "Materias", Icons.Filled.Book)
                        StatItem(uiState.tareasPendientes.size.toString(), "Pendientes", Icons.AutoMirrored.Filled.Assignment)
                        StatItem(uiState.tareasHoy.size.toString(), "Hoy", Icons.Filled.Today,
                            highlight = uiState.tareasHoy.isNotEmpty())
                    }
                }
            }
            item { Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccesoRapidoItem(Icons.Filled.Book, "Materias", onNavigateToMaterias, Modifier.weight(1f))
                    AccesoRapidoItem(Icons.Filled.Add, "Nueva tarea", onNavigateToTareas, Modifier.weight(1f))
                    AccesoRapidoItem(Icons.Filled.CalendarMonth, "Agenda", onNavigateToCalendario, Modifier.weight(1f))
                }
            }
            if (uiState.materias.isNotEmpty()) {
                item { Text("Mis materias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.materias) { materia -> MateriaChipCard(materia) }
                    }
                }
            }
            if (uiState.tareasHoy.isNotEmpty()) {
                item { Text("Vencen hoy", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) }
                items(uiState.tareasHoy) { tarea ->
                    TareaCard(tarea, { viewModel.marcarTareaCompletada(tarea.id) }, { onTareaClick(tarea.id) })
                }
            }
            if (uiState.tareasProximas.isNotEmpty()) {
                item { Text("Esta semana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(uiState.tareasProximas.take(5)) { tarea ->
                    TareaCard(tarea, { viewModel.marcarTareaCompletada(tarea.id) }, { onTareaClick(tarea.id) })
                }
            }
            if (uiState.materias.isEmpty() && uiState.tareasPendientes.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.School, null, Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("¡Bienvenido a AppUntes!", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Comenzá agregando tus materias.", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onNavigateToMaterias, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp))
                            Text("Agregar primera materia")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (highlight) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun AccesoRapidoItem(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MateriaChipCard(materia: Materia) {
    val color = try { Color(android.graphics.Color.parseColor(materia.color)) }
    catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Card(modifier = Modifier.width(140.dp), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Book, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(materia.nombre, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (materia.cantidadTareasPendientes > 0) {
                Spacer(Modifier.height(4.dp))
                Text("${materia.cantidadTareasPendientes} pendientes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TareaCard(tarea: Tarea, onCheck: () -> Unit, onClick: () -> Unit) {
    val prioridadColor = when (tarea.prioridad) {
        PrioridadTarea.ALTA -> PrioridadAlta
        PrioridadTarea.MEDIA -> PrioridadMedia
        PrioridadTarea.BAJA -> PrioridadBaja
    }
    val diasRestantes = LocalDate.now().until(tarea.fechaEntrega).days
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(4.dp).height(48.dp).clip(RoundedCornerShape(2.dp))
                .background(prioridadColor))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tarea.titulo, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (tarea.materiaNombre.isNotEmpty())
                    Text(tarea.materiaNombre, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                val diasText = when {
                    diasRestantes < 0 -> "Vencida hace ${-diasRestantes}d"
                    diasRestantes == 0 -> "Vence hoy"
                    diasRestantes == 1 -> "Vence mañana"
                    else -> "En $diasRestantes días"
                }
                Text(diasText, style = MaterialTheme.typography.labelSmall,
                    color = if (diasRestantes <= 1) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(checked = false, onCheckedChange = { onCheck() })
        }
    }
}
