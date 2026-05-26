package com.appuntes.presentation.calendario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    tareasViewModel: TareasViewModel,
    onTareaClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val tareasState by tareasViewModel.uiState.collectAsStateWithLifecycle()
    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    var diaSeleccionado by remember { mutableStateOf(LocalDate.now()) }
    val tareasPorFecha = tareasState.tareas.groupBy { it.fechaEntrega }
    val tareasDelDia = tareasPorFecha[diaSeleccionado] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario Académico", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                            Icon(Icons.Filled.ChevronLeft, null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(mesActual.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "AR")))
                            .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        IconButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                            Icon(Icons.Filled.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        listOf("Lu","Ma","Mi","Ju","Vi","Sa","Do").forEach { dia ->
                            Text(dia, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    val primerDiaMes = mesActual.atDay(1)
                    val offsetInicio = (primerDiaMes.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
                    val diasEnMes = mesActual.lengthOfMonth()
                    val totalCeldas = offsetInicio + diasEnMes
                    val filas = (totalCeldas + 6) / 7
                    val hoy = LocalDate.now()
                    repeat(filas) { fila ->
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { col ->
                                val index = fila * 7 + col
                                val numeroDia = index - offsetInicio + 1
                                val fecha = if (numeroDia in 1..diasEnMes) mesActual.atDay(numeroDia) else null
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f),
                                    contentAlignment = Alignment.Center) {
                                    if (fecha != null) {
                                        val tieneTareas = tareasPorFecha.containsKey(fecha)
                                        val isSeleccionado = fecha == diaSeleccionado
                                        val isHoy = fecha == hoy
                                        val tieneTareasVencidas = (tareasPorFecha[fecha] ?: emptyList())
                                            .any { it.estado != EstadoTarea.COMPLETADA && fecha.isBefore(hoy) }
                                        Box(modifier = Modifier.fillMaxSize().padding(2.dp).clip(CircleShape)
                                            .background(when {
                                                isSeleccionado -> MaterialTheme.colorScheme.primary
                                                isHoy -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                else -> Color.Transparent
                                            }).clickable { diaSeleccionado = fecha },
                                            contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(numeroDia.toString(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isHoy || isSeleccionado) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSeleccionado -> Color.White
                                                        isHoy -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    })
                                                if (tieneTareas) {
                                                    Box(Modifier.size(5.dp).clip(CircleShape).background(
                                                        when {
                                                            isSeleccionado -> Color.White
                                                            tieneTareasVencidas -> MaterialTheme.colorScheme.error
                                                            else -> MaterialTheme.colorScheme.tertiary
                                                        }))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val labelDia = when (diaSeleccionado) {
                LocalDate.now() -> "Hoy"
                LocalDate.now().plusDays(1) -> "Mañana"
                else -> diaSeleccionado.format(
                    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "AR")))
                    .replaceFirstChar { it.uppercase() }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(labelDia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                if (tareasDelDia.isNotEmpty())
                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("${tareasDelDia.size}") }
            }
            if (tareasDelDia.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.EventAvailable, null, Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("Nada para este día", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tareasDelDia, key = { it.id }) { tarea ->
                        CalendarioTareaCard(tarea = tarea, onClick = { onTareaClick(tarea.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarioTareaCard(tarea: Tarea, onClick: () -> Unit) {
    val prioColor = when (tarea.prioridad) {
        PrioridadTarea.ALTA -> PrioridadAlta
        PrioridadTarea.MEDIA -> PrioridadMedia
        PrioridadTarea.BAJA -> PrioridadBaja
    }
    val completada = tarea.estado == EstadoTarea.COMPLETADA
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(2.dp))
                .background(if (completada) MaterialTheme.colorScheme.outline else prioColor))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tarea.titulo, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (completada) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface)
                if (tarea.materiaNombre.isNotEmpty())
                    Text(tarea.materiaNombre, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (completada) Icon(Icons.Filled.CheckCircle, null, Modifier.size(20.dp), tint = Color(0xFF2E7D32))
            else Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
