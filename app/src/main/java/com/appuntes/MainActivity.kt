package com.appuntes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.appuntes.core.navigation.AppUntesNavGraph
import com.appuntes.core.navigation.Screen
import com.appuntes.data.local.AppDatabase
import com.appuntes.data.repository.MateriaRepository
import com.appuntes.data.repository.TareaRepository
import com.appuntes.presentation.home.HomeViewModel
import com.appuntes.presentation.materias.MateriasViewModel
import com.appuntes.presentation.tareas.TareasViewModel
import com.appuntes.presentation.theme.AppUntesTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val android.content.Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")

class MainActivity : ComponentActivity() {
    private val db by lazy { AppDatabase.getInstance(this) }
    private val materiaRepo by lazy { MateriaRepository(db.materiaDao()) }
    private val tareaRepo by lazy { TareaRepository(db.tareaDao()) }
    private val homeViewModel by lazy { HomeViewModel(materiaRepo, tareaRepo) }
    private val materiasViewModel by lazy { MateriasViewModel(materiaRepo) }
    private val tareasViewModel by lazy { TareasViewModel(tareaRepo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppUntesTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    val done = dataStore.data.map { it[ONBOARDING_DONE_KEY] ?: false }.first()
                    startDestination = if (done) Screen.Home.route else Screen.Onboarding.route
                }
                startDestination?.let { start ->
                    val navController = rememberNavController()
                    AppUntesNavGraph(
                        navController = navController,
                        startDestination = start,
                        homeViewModel = homeViewModel,
                        materiasViewModel = materiasViewModel,
                        tareasViewModel = tareasViewModel
                    )
                    LaunchedEffect(navController) {
                        navController.currentBackStackEntryFlow.collect { entry ->
                            if (entry.destination.route == Screen.Home.route) {
                                lifecycleScope.launch {
                                    dataStore.updateData { prefs ->
                                        prefs.toMutablePreferences().apply {
                                            set(ONBOARDING_DONE_KEY, true)
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
}
