package com.appuntes.domain.model

import java.time.LocalDate

enum class PrioridadTarea { BAJA, MEDIA, ALTA }
enum class EstadoTarea { PENDIENTE, EN_PROGRESO, COMPLETADA }

data class Tarea(
    val id: Long = 0,
    val titulo: String,
    val descripcion: String = "",
    val materiaId: Long,
    val materiaNombre: String = "",
    val materiaColor: String = "#6750A4",
    val fechaEntrega: LocalDate,
    val prioridad: PrioridadTarea = PrioridadTarea.MEDIA,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val imagenUri: String? = null,
    val notaVoz: String? = null,
    val recordatorioActivo: Boolean = false,
    val fechaCreacion: LocalDate = LocalDate.now()
)
