package com.appuntes.presentation.materias

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appuntes.presentation.theme.MateriaColorsHex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMateriaScreen(
    viewModel: MateriasViewModel,
    editMateriaId: Long? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = editMateriaId != null
    var nombre by remember { mutableStateOf("") }
    var profesor by remember { mutableStateOf("") }
    var aula by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var colorSeleccionado by remember { mutableStateOf(MateriaColorsHex[0]) }
    var nombreError by remember { mutableStateOf(false) }

    LaunchedEffect(editMateriaId) {
        editMateriaId?.let { id ->
            viewModel.getMateriaById(id)?.let {
                nombre = it.nombre
                profesor = it.profesor
                aula = it.aula
                horario = it.horario
                colorSeleccionado = it.color
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error, state.success) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        state.success?.let { onSuccess() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar materia" else "Nueva materia",
                    fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            val previewColor = runCatching { Color(android.graphics.Color.parseColor(colorSeleccionado)) }
                .getOrElse { MaterialTheme.colorScheme.primary }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(previewColor.copy(alpha = 0.2f))
                    .border(3.dp, previewColor, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Book, null, Modifier.size(36.dp), tint = previewColor)
                }
            }
            OutlinedTextField(value = nombre, onValueChange = { nombre = it; nombreError = false },
                label = { Text("Nombre de la materia *") },
                leadingIcon = { Icon(Icons.Filled.Book, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, isError = nombreError,
                supportingText = if (nombreError) ({ Text("Campo obligatorio") }) else null)
            OutlinedTextField(value = profesor, onValueChange = { profesor = it },
                label = { Text("Profesor/a") }, leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = aula, onValueChange = { aula = it },
                    label = { Text("Aula") }, leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                    modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = horario, onValueChange = { horario = it },
                    label = { Text("Horario") }, leadingIcon = { Icon(Icons.Filled.Schedule, null) },
                    modifier = Modifier.weight(1f), singleLine = true,
                    placeholder = { Text("Lun 18hs") })
            }
            Text("Color identificador", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MateriaColorsHex.forEach { hex ->
                    val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }
                        .getOrElse { Color.Gray }
                    val isSelected = hex == colorSeleccionado
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(c)
                        .then(if (isSelected) Modifier.border(3.dp,
                            MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        .clickable { colorSeleccionado = hex },
                        contentAlignment = Alignment.Center) {
                        if (isSelected) Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (nombre.isBlank()) { nombreError = true; return@Button }
                    if (isEditing && editMateriaId != null)
                        viewModel.editarMateria(editMateriaId, nombre, profesor, aula, horario)
                    else viewModel.crearMateria(nombre, profesor, aula, horario)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (isEditing) Icons.Filled.Save else Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Guardar cambios" else "Crear materia",
                    style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
