package com.appuntes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tareas",
    foreignKeys = [
        ForeignKey(
            entity = MateriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["materiaId"],
            onDelete = ForeignKey.CASCADE  // Si borras la materia, se borran sus tareas
        )
    ],
    indices = [Index("materiaId")]  // Indice para busquedas rapidas por materias
)
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val descripcion: String = "",
    val materiaId: Long,
    val fechaEntrega: String,
    val prioridad: String = "MEDIA",
    val estado: String = "PENDIENTE",
    val imagenUri: String? = null,
    val notaVoz: String? = null,
    val recordatorioActivo: Boolean = false,
    val fechaCreacion: String
)
