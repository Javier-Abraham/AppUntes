package com.appuntes.domain.model

data class Materia(
    val id: Long = 0,
    val nombre: String,
    val profesor: String,
    val color: String,
    val icono: String,
    val aula: String = "",
    val horario: String = "",
    val cantidadTareasPendientes: Int = 0,
    val progreso: Float = 0f
)
