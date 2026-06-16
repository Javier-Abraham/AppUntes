package com.appuntes.presentation.tareas

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.appuntes.domain.model.EstadoTarea
import com.appuntes.domain.model.PrioridadTarea
import com.appuntes.domain.model.Tarea
import com.appuntes.presentation.materias.MateriasViewModel
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTareaScreen(
    viewModel: TareasViewModel,
    materiasViewModel: MateriasViewModel,
    preselectedMateriaId: Long? = null,
    editTareaId: Long? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val materiasState by materiasViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var materiaSeleccionadaId by remember { mutableLongStateOf(preselectedMateriaId ?: -1L) }
    var prioridad by remember { mutableStateOf(PrioridadTarea.MEDIA) }
    var fechaEntrega by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var imagenUri by remember { mutableStateOf<String?>(null) }
    var recordatorioActivo by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showMateriaDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(editTareaId) {
        editTareaId?.let { id ->
            viewModel.getTareaById(id)?.let {
                titulo = it.titulo
                descripcion = it.descripcion
                materiaSeleccionadaId = it.materiaId
                prioridad = it.prioridad
                fechaEntrega = it.fechaEntrega
                imagenUri = it.imagenUri
                recordatorioActivo = it.recordatorioActivo
            }
        }
    }

    val photoFile = remember { File(context.cacheDir, "tarea_foto_${System.currentTimeMillis()}.jpg") }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imagenUri = photoFile.absolutePath
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(photoUri)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error, uiState.success) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.success?.let { onSuccess() }
    }

    val isEditing = editTareaId != null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar tarea" else "Nueva tarea", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = titulo, onValueChange = { titulo = it },
                label = { Text("Título *") }, leadingIcon = { Icon(Icons.Filled.Title, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it },
                label = { Text("Descripción (opcional)") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            ExposedDropdownMenuBox(expanded = showMateriaDropdown,
                onExpandedChange = { showMateriaDropdown = !showMateriaDropdown }) {
                val materiaSeleccionada = materiasState.materias.firstOrNull { it.id == materiaSeleccionadaId }
                OutlinedTextField(value = materiaSeleccionada?.nombre ?: "Seleccionar materia *",
                    onValueChange = {}, readOnly = true, label = { Text("Materia") },
                    leadingIcon = { Icon(Icons.Filled.Book, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMateriaDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = showMateriaDropdown,
                    onDismissRequest = { showMateriaDropdown = false }) {
                    materiasState.materias.forEach { materia ->
                        DropdownMenuItem(text = { Text(materia.nombre) },
                            onClick = { materiaSeleccionadaId = materia.id; showMateriaDropdown = false })
                    }
                }
            }
            Text("Prioridad", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrioridadTarea.values().forEach { p ->
                    FilterChip(selected = prioridad == p, onClick = { prioridad = p },
                        label = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = if (prioridad == p) ({ Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }) else null)
                }
            }
            OutlinedTextField(
                value = fechaEntrega.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                onValueChange = {}, readOnly = true, label = { Text("Fecha de entrega *") },
                leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.Edit, null) } },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true })
            Text("Foto adjunta", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            if (imagenUri != null) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    AsyncImage(model = imagenUri, contentDescription = "Foto",
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    IconButton(onClick = { imagenUri = null }, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                OutlinedCard(modifier = Modifier.fillMaxWidth().height(100.dp).clickable {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }, shape = RoundedCornerShape(12.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.PhotoCamera, null, Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text("Tomar foto", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Recordatorio", style = MaterialTheme.typography.bodyLarge)
                }
                Switch(checked = recordatorioActivo, onCheckedChange = { recordatorioActivo = it })
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (titulo.isBlank()) return@Button
                    val materiaId = if (materiaSeleccionadaId == -1L)
                        materiasState.materias.firstOrNull()?.id ?: 0L
                    else materiaSeleccionadaId
                    val tarea = Tarea(id = editTareaId ?: 0L, titulo = titulo.trim(),
                        descripcion = descripcion.trim(), materiaId = materiaId,
                        fechaEntrega = fechaEntrega, prioridad = prioridad,
                        estado = EstadoTarea.PENDIENTE, imagenUri = imagenUri,
                        recordatorioActivo = recordatorioActivo)
                    if (isEditing) viewModel.editarTarea(tarea) else viewModel.crearTarea(tarea)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (isEditing) Icons.Filled.Save else Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Guardar cambios" else "Crear tarea",
                    style = MaterialTheme.typography.titleSmall)
            }
            if (isEditing) {
                OutlinedButton(onClick = { editTareaId?.let { viewModel.eliminarTarea(it) } },
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Filled.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar tarea")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaEntrega.toEpochDay() * 86400000L)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaEntrega = LocalDate.ofEpochDay(millis / 86400000L)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}
