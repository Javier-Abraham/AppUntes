package com.appuntes.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.appuntes.presentation.calendario.CalendarioScreen
import com.appuntes.presentation.home.HomeScreen
import com.appuntes.presentation.home.HomeViewModel
import com.appuntes.presentation.materias.CrearMateriaScreen
import com.appuntes.presentation.materias.DetalleMateriaScreen
import com.appuntes.presentation.materias.MateriasScreen
import com.appuntes.presentation.materias.MateriasViewModel
import com.appuntes.presentation.onboarding.OnboardingScreen
import com.appuntes.presentation.perfil.PerfilScreen
import com.appuntes.presentation.tareas.CrearTareaScreen
import com.appuntes.presentation.tareas.TareasScreen
import com.appuntes.presentation.tareas.TareasViewModel

@Composable
fun AppUntesNavGraph(
    navController: NavHostController,
    startDestination: String,
    homeViewModel: HomeViewModel,
    materiasViewModel: MateriasViewModel,
    tareasViewModel: TareasViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToMaterias = { navController.navigate(Screen.Materias.route) },
                onNavigateToTareas = { navController.navigate(Screen.Tareas.route) },
                onNavigateToCalendario = { navController.navigate(Screen.Calendario.route) },
                onNavigateToPerfil = { navController.navigate(Screen.Perfil.route) },
                onTareaClick = { tareaId -> navController.navigate(Screen.EditarTarea.createRoute(tareaId)) }
            )
        }

        composable(Screen.Materias.route) {
            MateriasScreen(
                viewModel = materiasViewModel,
                onMateriaClick = { materiaId -> navController.navigate(Screen.DetalleMateria.createRoute(materiaId)) },
                onCrearMateria = { navController.navigate(Screen.CrearMateria.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DetalleMateria.route,
            arguments = listOf(navArgument("materiaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val materiaId = backStackEntry.arguments?.getLong("materiaId") ?: 0L
            DetalleMateriaScreen(
                materiaId = materiaId,
                materiasViewModel = materiasViewModel,
                tareasViewModel = tareasViewModel,
                onCrearTarea = { navController.navigate(Screen.CrearTarea.createRoute(materiaId)) },
                onEditarTarea = { tareaId -> navController.navigate(Screen.EditarTarea.createRoute(tareaId)) },
                onEditarMateria = { navController.navigate(Screen.EditarMateria.createRoute(materiaId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CrearMateria.route) {
            CrearMateriaScreen(
                viewModel = materiasViewModel,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditarMateria.route,
            arguments = listOf(navArgument("materiaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val materiaId = backStackEntry.arguments?.getLong("materiaId") ?: 0L
            CrearMateriaScreen(
                viewModel = materiasViewModel,
                editMateriaId = materiaId,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Tareas.route) {
            TareasScreen(
                viewModel = tareasViewModel,
                onCrearTarea = { navController.navigate(Screen.CrearTarea.createRoute()) },
                onEditarTarea = { tareaId -> navController.navigate(Screen.EditarTarea.createRoute(tareaId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CrearTarea.route,
            arguments = listOf(navArgument("materiaId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val materiaId = backStackEntry.arguments?.getLong("materiaId")?.takeIf { it != -1L }
            CrearTareaScreen(
                viewModel = tareasViewModel,
                materiasViewModel = materiasViewModel,
                preselectedMateriaId = materiaId,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditarTarea.route,
            arguments = listOf(navArgument("tareaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getLong("tareaId") ?: 0L
            CrearTareaScreen(
                viewModel = tareasViewModel,
                materiasViewModel = materiasViewModel,
                editTareaId = tareaId,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Calendario.route) {
            CalendarioScreen(
                tareasViewModel = tareasViewModel,
                onTareaClick = { tareaId -> navController.navigate(Screen.EditarTarea.createRoute(tareaId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(onBack = { navController.popBackStack() })
        }
    }
}
