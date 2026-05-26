package com.appuntes.data.repository

import com.appuntes.data.local.dao.TareaDao
import com.appuntes.data.local.entity.TareaEntity
import com.appuntes.domain.model.EstadoTarea
import com.appuntes.domain.model.PrioridadTarea
import com.appuntes.domain.model.Tarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TareaRepository(private val dao: TareaDao) {

    val tareas: Flow<List<Tarea>> = dao.getAllTareas().map { it.map { e -> e.toDomain() } }

    val tareasPendientes: Flow<List<Tarea>> = dao.getTareasPendientes().map { it.map { e -> e.toDomain() } }

    fun getTareasByMateria(materiaId: Long): Flow<List<Tarea>> =
        dao.getTareasByMateria(materiaId).map { it.map { e -> e.toDomain() } }

    fun countPendientesByMateria(materiaId: Long): Flow<Int> =
        dao.countTareasPendientesByMateria(materiaId)

    fun getProgresoMateria(materiaId: Long): Flow<Float> =
        dao.getProgresoMateria(materiaId)

    suspend fun getTareaById(id: Long): Tarea? = dao.getTareaById(id)?.toDomain()

    suspend fun insertTarea(tarea: Tarea): Long = dao.insertTarea(tarea.toEntity())

    suspend fun updateTarea(tarea: Tarea) = dao.updateTarea(tarea.toEntity())

    suspend fun deleteTarea(id: Long) = dao.deleteTareaById(id)

    suspend fun marcarCompletada(id: Long) = dao.updateEstado(id, EstadoTarea.COMPLETADA.name)

    suspend fun marcarEnProgreso(id: Long) = dao.updateEstado(id, EstadoTarea.EN_PROGRESO.name)

    private fun TareaEntity.toDomain() = Tarea(
        id = id, titulo = titulo, descripcion = descripcion,
        materiaId = materiaId,
        fechaEntrega = LocalDate.parse(fechaEntrega),
        prioridad = PrioridadTarea.valueOf(prioridad),
        estado = EstadoTarea.valueOf(estado),
        imagenUri = imagenUri, notaVoz = notaVoz,
        recordatorioActivo = recordatorioActivo,
        fechaCreacion = LocalDate.parse(fechaCreacion)
    )

    private fun Tarea.toEntity() = TareaEntity(
        id = id, titulo = titulo, descripcion = descripcion,
        materiaId = materiaId,
        fechaEntrega = fechaEntrega.toString(),
        prioridad = prioridad.name, estado = estado.name,
        imagenUri = imagenUri, notaVoz = notaVoz,
        recordatorioActivo = recordatorioActivo,
        fechaCreacion = fechaCreacion.toString()
    )
}
