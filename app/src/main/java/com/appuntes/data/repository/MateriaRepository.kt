package com.appuntes.data.repository

import com.appuntes.data.local.dao.MateriaDao
import com.appuntes.data.local.entity.MateriaEntity
import com.appuntes.domain.model.Materia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MateriaRepository(private val dao: MateriaDao) {

    val materias: Flow<List<Materia>> = dao.getAllMaterias().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getMateriaById(id: Long): Materia? =
        dao.getMateriaById(id)?.toDomain()

    suspend fun insertMateria(materia: Materia): Long =
        dao.insertMateria(materia.toEntity())

    suspend fun updateMateria(materia: Materia) =
        dao.updateMateria(materia.toEntity())

    suspend fun deleteMateria(id: Long) =
        dao.deleteMateriaById(id)

    private fun MateriaEntity.toDomain() = Materia(
        id = id, nombre = nombre, profesor = profesor,
        color = color, icono = icono, aula = aula, horario = horario
    )

    private fun Materia.toEntity() = MateriaEntity(
        id = id, nombre = nombre, profesor = profesor,
        color = color, icono = icono, aula = aula, horario = horario
    )
}
