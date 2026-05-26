package com.appuntes.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Materias : Screen("materias")
    object DetalleMateria : Screen("detalle_materia/{materiaId}") {
        fun createRoute(materiaId: Long) = "detalle_materia/$materiaId"
    }
    object CrearMateria : Screen("crear_materia")
    object EditarMateria : Screen("editar_materia/{materiaId}") {
        fun createRoute(materiaId: Long) = "editar_materia/$materiaId"
    }
    object Tareas : Screen("tareas")
    object CrearTarea : Screen("crear_tarea?materiaId={materiaId}") {
        fun createRoute(materiaId: Long? = null) =
            if (materiaId != null) "crear_tarea?materiaId=$materiaId" else "crear_tarea?materiaId=-1"
    }
    object EditarTarea : Screen("editar_tarea/{tareaId}") {
        fun createRoute(tareaId: Long) = "editar_tarea/$tareaId"
    }
    object Calendario : Screen("calendario")
    object Recordatorios : Screen("recordatorios")
    object Perfil : Screen("perfil")
}
