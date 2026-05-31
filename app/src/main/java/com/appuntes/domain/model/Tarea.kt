package com.appuntes.domain.model

import java.time.LocalDate

enum class PrioridadTarea { BAJA, MEDIA, ALTA }
enum class EstadoTarea { PENDIENTE, EN_PROGRESO, COMPLETADA }

data class Tarea(
    val id: Long = 0, // ID Único (Room la utoimcrementa)
    val titulo: String, // Título obligatorio
    val descripcion: String = "", //Descripción opcional
    val materiaId: Long, //Referencia a la materia
    val materiaNombre: String = "",
    val materiaColor: String = "#6750A4",
    val fechaEntrega: LocalDate,   // Fecha de Vencimiento
    val prioridad: PrioridadTarea = PrioridadTarea.MEDIA,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val imagenUri: String? = null, // Foto de camara (Puede ser Null)
    val notaVoz: String? = null,
    val recordatorioActivo: Boolean = false,
    val fechaCreacion: LocalDate = LocalDate.now()
)
