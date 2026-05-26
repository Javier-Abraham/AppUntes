package com.appuntes.presentation.materias

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appuntes.domain.model.EstadoTarea
import com.appuntes.domain.model.PrioridadTarea
import com.appuntes.domain.model.Tarea
import com.appuntes.presentation.tareas.TareasViewModel
import com.appuntes.presentation.theme.PrioridadAlta
import com.appuntes.presentation.theme.PrioridadBaja
import com.appuntes.presentation.theme.PrioridadMedia
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMateriaScreen(
    materiaId: Long,
    materiasViewModel: MateriasViewModel,
    tareasViewModel: TareasViewModel,
    onCrearTarea: () -> Unit,
    onEditarTarea: (Long) -> Unit,
    onEditarMateria: () -> Unit,
    onBack: () -> Unit
) {
    val materiasState by materiasViewModel.uiState.collectAsStateWithLifecycle()
    val tareasState by tareasViewModel.uiState.collectAsStateWithLifecycle()
    val materia = materiasState.materias.firstOrNull { it.id == materiaId }
    val tareasDeMat = tareasState.tareas.filter { it.materiaId == materiaId }
    val pendientes = tareasDeMat.count { it.estado != EstadoTarea.COMPLETADA }
    val completadas = tareasDeMat.count { it.estado == EstadoTarea.COMPLETADA }
    val progreso = if (tareasDeMat.isEmpty()) 0f else completadas.toFloat() / tareasDeMat.size
    val color = materia?.color?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrElse { Color(0xFF6750A4) }
    } ?: Color(0xFF6750A4)
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(materia?.nombre ?: "Materia", fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = onEditarMateria) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearTarea, containerColor = color) {
                Icon(Icons.Filled.Add, "Nueva tarea", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                                .background(color.copy(0.25f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Book, null, Modifier.size(30.dp), tint = color)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(materia?.nombre ?: "", style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold)
                                if (!materia?.profesor.isNullOrEmpty())
                                    Text(materia!!.profesor, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (!materia?.aula.isNullOrEmpty())
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, null, Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Aula ${materia!!.aula}", style = MaterialTheme.typography.labelSmall)
                                }
                            if (!materia?.horario.isNullOrEmpty())
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, null, Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(materia!!.horario, style = MaterialTheme.typography.labelSmall)
                                }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progreso", style = MaterialTheme.typography.labelMedium)
                            Text("$completadas / ${tareasDeMat.size} completadas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { progreso },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = color, trackColor = color.copy(alpha = 0.2f))
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Pendientes", pendientes.toString(), Icons.Filled.RadioButtonUnchecked,
                        MaterialTheme.colorScheme.error, Modifier.weight(1f))
                    StatCard("Completadas", completadas.toString(), Icons.Filled.CheckCircle,
                        Color(0xFF2E7D32), Modifier.weight(1f))
                    StatCard("Total", tareasDeMat.size.toString(), Icons.Filled.Assignment,
                        MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }
            }
            if (tareasDeMat.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AssignmentTurnedIn, null, Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(12.dp))
                            Text("Sin tareas aún", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tocá + para agregar una tarea.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            } else {
                item { Text("Tareas (${tareasDeMat.size})", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold) }
                items(tareasDeMat.sortedBy { it.fechaEntrega }, key = { it.id }) { tarea ->
                    DetalleTareaCard(tarea = tarea,
                        onCheck = { tareasViewModel.marcarCompletada(tarea.id) },
                        onClick = { onEditarTarea(tarea.id) })
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar materia") },
            text = { Text("Se eliminarán también las ${tareasDeMat.size} tareas asociadas. ¿Continuar?") },
            confirmButton = {
                Button(onClick = { materiasViewModel.eliminarMateria(materiaId); showDeleteDialog = false; onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(20.dp), tint = color)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DetalleTareaCard(tarea: Tarea, onCheck: () -> Unit, onClick: () -> Unit) {
    val prioColor = when (tarea.prioridad) {
        PrioridadTarea.ALTA -> PrioridadAlta
        PrioridadTarea.MEDIA -> PrioridadMedia
        PrioridadTarea.BAJA -> PrioridadBaja
    }
    val completada = tarea.estado == EstadoTarea.COMPLETADA
    val diasRestantes = LocalDate.now().until(tarea.fechaEntrega).days
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (completada)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(44.dp).clip(RoundedCornerShape(2.dp))
                .background(if (completada) MaterialTheme.colorScheme.outline else prioColor))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tarea.titulo, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (completada) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface)
                val fechaText = when {
                    completada -> "Completada"
                    diasRestantes < 0 -> "Vencida hace ${-diasRestantes}d"
                    diasRestantes == 0 -> "Vence hoy"
                    diasRestantes == 1 -> "Vence mañana"
                    else -> "En $diasRestantes días"
                }
                Text(fechaText, style = MaterialTheme.typography.labelSmall,
                    color = when {
                        completada -> MaterialTheme.colorScheme.onSurfaceVariant
                        diasRestantes <= 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    })
            }
            if (tarea.imagenUri != null)
                Icon(Icons.Filled.Image, null, Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Checkbox(checked = completada, onCheckedChange = { if (!completada) onCheck() })
        }
    }
}
