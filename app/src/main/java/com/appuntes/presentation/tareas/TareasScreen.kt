package com.appuntes.presentation.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appuntes.domain.model.EstadoTarea
import com.appuntes.domain.model.PrioridadTarea
import com.appuntes.domain.model.Tarea
import com.appuntes.presentation.theme.PrioridadAlta
import com.appuntes.presentation.theme.PrioridadBaja
import com.appuntes.presentation.theme.PrioridadMedia
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    onCrearTarea: () -> Unit,
    onEditarTarea: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tareasFiltradas by viewModel.tareasFiltradas.collectAsStateWithLifecycle()
    val tareasAgrupadas = tareasFiltradas
        .sortedWith(compareBy({ it.estado == EstadoTarea.COMPLETADA }, { it.fechaEntrega }))
        .groupBy { it.fechaEntrega }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.success) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        state.success?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    val pendientes = state.tareas.count { it.estado == EstadoTarea.PENDIENTE }
                    if (pendientes > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) { Text("$pendientes") }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearTarea,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nueva tarea") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = state.filtroActivo == null, onClick = { viewModel.setFiltro(null) },
                        label = { Text("Todas (${state.tareas.size})") },
                        leadingIcon = if (state.filtroActivo == null)
                            ({ Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }) else null)
                }
                items(EstadoTarea.values()) { estado ->
                    val count = state.tareas.count { it.estado == estado }
                    FilterChip(
                        selected = state.filtroActivo == estado,
                        onClick = { viewModel.setFiltro(if (state.filtroActivo == estado) null else estado) },
                        label = { Text("${estadoLabel(estado)} ($count)") },
                        leadingIcon = if (state.filtroActivo == estado)
                            ({ Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }) else null)
                }
            }
            HorizontalDivider()
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Scaffold
            }
            if (tareasFiltradas.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AssignmentTurnedIn, null, Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Sin tareas aquí", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text(if (state.filtroActivo != null) "No hay tareas con este filtro."
                            else "Tocá + para crear tu primera tarea.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                return@Scaffold
            }
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tareasAgrupadas.forEach { (fecha, tareas) ->
                    item(key = "header_$fecha") { FechaHeader(fecha = fecha) }
                    items(tareas, key = { "tarea_${it.id}" }) { tarea ->
                        TareaListItem(tarea = tarea,
                            onCheck = { if (tarea.estado != EstadoTarea.COMPLETADA) viewModel.marcarCompletada(tarea.id) },
                            onClick = { onEditarTarea(tarea.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FechaHeader(fecha: LocalDate) {
    val hoy = LocalDate.now()
    val label = when (fecha) {
        hoy -> "Hoy"
        hoy.plusDays(1) -> "Mañana"
        hoy.minusDays(1) -> "Ayer"
        else -> fecha.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "AR")))
            .replaceFirstChar { it.uppercase() }
    }
    val isVencida = fecha.isBefore(hoy)
    val isHoy = fecha == hoy
    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
            color = when { isVencida -> MaterialTheme.colorScheme.error
                isHoy -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant })
        Spacer(Modifier.width(8.dp))
        if (isVencida) {
            Box(Modifier.clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("Vencida", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer)
            }
        } else if (isHoy) {
            Box(Modifier.clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("Hoy", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.weight(2f),
            color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun TareaListItem(tarea: Tarea, onCheck: () -> Unit, onClick: () -> Unit) {
    val completada = tarea.estado == EstadoTarea.COMPLETADA
    val prioColor = when (tarea.prioridad) {
        PrioridadTarea.ALTA -> PrioridadAlta
        PrioridadTarea.MEDIA -> PrioridadMedia
        PrioridadTarea.BAJA -> PrioridadBaja
    }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (completada)
            MaterialTheme.colorScheme.surfaceVariant.copy(0.4f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (completada) 0.dp else 1.dp)) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(52.dp).clip(RoundedCornerShape(2.dp))
                .background(if (completada) MaterialTheme.colorScheme.outline.copy(0.3f) else prioColor))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tarea.titulo, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        textDecoration = if (completada) TextDecoration.LineThrough else null,
                        color = if (completada) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    if (tarea.imagenUri != null)
                        Icon(Icons.Filled.PhotoCamera, null, Modifier.size(14.dp).padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    EstadoChip(tarea.estado)
                    PrioridadChip(tarea.prioridad)
                }
            }
            Spacer(Modifier.width(8.dp))
            Checkbox(checked = completada, onCheckedChange = { onCheck() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E7D32),
                    uncheckedColor = prioColor))
        }
    }
}

@Composable
private fun EstadoChip(estado: EstadoTarea) {
    val (label, color, bg) = when (estado) {
        EstadoTarea.PENDIENTE -> Triple("Pendiente",
            MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        EstadoTarea.EN_PROGRESO -> Triple("En progreso", Color(0xFF1565C0), Color(0xFFE3F2FD))
        EstadoTarea.COMPLETADA -> Triple("Completada", Color(0xFF2E7D32), Color(0xFFE8F5E9))
    }
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun PrioridadChip(prioridad: PrioridadTarea) {
    val (label, color) = when (prioridad) {
        PrioridadTarea.ALTA -> Pair("Alta", PrioridadAlta)
        PrioridadTarea.MEDIA -> Pair("Media", PrioridadMedia)
        PrioridadTarea.BAJA -> Pair("Baja", PrioridadBaja)
    }
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.12f))
        .padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color,
            fontWeight = FontWeight.SemiBold)
    }
}

private fun estadoLabel(estado: EstadoTarea) = when (estado) {
    EstadoTarea.PENDIENTE -> "Pendientes"
    EstadoTarea.EN_PROGRESO -> "En progreso"
    EstadoTarea.COMPLETADA -> "Completadas"
}
