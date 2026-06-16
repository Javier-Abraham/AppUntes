package com.appuntes.presentation.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.appuntes.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme

private val NOMBRE_KEY = stringPreferencesKey("usuario_nombre")
private val CARRERA_KEY = stringPreferencesKey("usuario_carrera")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("Usuario 1") }
    var carrera by remember { mutableStateOf("Desarrollo de Aplicaciones Móviles") }
    var editando by remember { mutableStateOf(false) }
    var nombreTemp by remember { mutableStateOf("") }
    var carreraTemp by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        context.dataStore.data.map { prefs ->
            Pair(prefs[NOMBRE_KEY] ?: "Usuario 1", prefs[CARRERA_KEY] ?: "Desarrollo de Aplicaciones Móviles")
        }.collect { (n, c) -> nombre = n; carrera = c }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    if (!editando)
                        IconButton(onClick = { nombreTemp = nombre; carreraTemp = carrera; editando = true }) {
                            Icon(Icons.Filled.Edit, "Editar perfil")
                        }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                Text("U", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(16.dp))
            if (!editando) {
                Text(nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(carrera, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                OutlinedTextField(value = nombreTemp, onValueChange = { nombreTemp = it },
                    label = { Text("Nombre") }, leadingIcon = { Icon(Icons.Filled.Person, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = carreraTemp, onValueChange = { carreraTemp = it },
                    label = { Text("Carrera") }, leadingIcon = { Icon(Icons.Filled.School, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { editando = false }, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(onClick = {
                        scope.launch {
                            context.dataStore.edit { prefs ->
                                prefs[NOMBRE_KEY] = nombreTemp
                                prefs[CARRERA_KEY] = carreraTemp
                            }
                            nombre = nombreTemp; carrera = carreraTemp; editando = false
                            snackbarHostState.showSnackbar("Perfil actualizado")
                        }
                    }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                }
            }
            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            SectionLabel("Aplicación")
            SettingsItem(Icons.Filled.Info, "Versión", "1.0.0 — AppUntes")
            SettingsItem(Icons.Filled.School, "Institución", "IFTS N° 18")
            SettingsItem(Icons.Outlined.Code, "Tecnología", "Kotlin + Jetpack Compose + Room")
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            SectionLabel("Proyecto")
            SettingsItem(Icons.Filled.Group, "Equipo", "Turno Noche — 2° Año B")
            SettingsItem(Icons.Outlined.Star, "Materia", "Desarrollo de Aplicaciones para Dispositivos Móviles")
            SettingsItem(Icons.Filled.CalendarMonth, "Año lectivo", "2026")
            Spacer(Modifier.height(20.dp))
            val isDark = isSystemInDarkTheme()
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode, null,
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Modo ${if (isDark) "oscuro" else "claro"}",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Cambialo desde la configuración del sistema",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
