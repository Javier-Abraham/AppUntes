package com.appuntes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materias")
data class MateriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val profesor: String,
    val color: String,
    val icono: String,
    val aula: String = "",
    val horario: String = ""
)
