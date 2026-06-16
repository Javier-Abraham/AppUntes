package com.appuntes.data.remote

import android.util.Log
import com.appuntes.data.local.dao.MateriaDao
import com.appuntes.data.local.dao.TareaDao
import com.appuntes.data.local.entity.MateriaEntity
import com.appuntes.data.local.entity.TareaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG             = "SupabaseRepository"
private const val SYNC_TIMEOUT_MS = 8_000L

/**
 * Capa de sincronización offline-first:
 *   Room = fuente de verdad  →  siempre disponible, instantáneo.
 *   Supabase = backup cloud  →  best-effort, ignorado si no hay red.
 *
 * Flujo de escritura: Room primero → Supabase en segundo plano.
 * Flujo de lectura:   Flow de Room (ya sincronizado en syncFromSupabase).
 */
class SupabaseRepository(
    private val materiaDao: MateriaDao,
    private val tareaDao:   TareaDao,
    private val api: SupabaseApiService = RetrofitInstance.supabaseApi
) {
    // ── Flows como fuente de verdad (Room) ──────────────────────────────────
    val materias: Flow<List<MateriaEntity>> = materiaDao.getAllMaterias()
    val tareas:   Flow<List<TareaEntity>>   = tareaDao.getAllTareas()

    // ── Sincronización inicial ───────────────────────────────────────────────
    /** Trae todos los registros de Supabase y los upsertea en Room.
     *  Llamar desde el ViewModel al iniciar la app o al reconectar red. */
    suspend fun syncFromSupabase() {
        syncMaterias()
        syncTareas()
    }

    private suspend fun syncMaterias() {
        remoteCall("syncMaterias") {
            val response = api.getMaterias()
            if (response.isSuccessful) {
                response.body()?.forEach { dto ->
                    materiaDao.insertMateria(dto.toEntity()) // REPLACE si ya existe
                }
            } else {
                Log.w(TAG, "getMaterias HTTP ${response.code()}")
            }
        }
    }

    private suspend fun syncTareas() {
        remoteCall("syncTareas") {
            val response = api.getTareas()
            if (response.isSuccessful) {
                response.body()?.forEach { dto ->
                    tareaDao.insertTarea(dto.toEntity())     // REPLACE si ya existe
                }
            } else {
                Log.w(TAG, "getTareas HTTP ${response.code()}")
            }
        }
    }

    // ── Operaciones de Materia ───────────────────────────────────────────────
    suspend fun insertMateria(entity: MateriaEntity): Long {
        val localId = materiaDao.insertMateria(entity)
        remoteCall("upsertMateria") {
            api.upsertMateria(entity.copy(id = localId).toSupabaseDto())
        }
        return localId
    }

    suspend fun updateMateria(entity: MateriaEntity) {
        materiaDao.updateMateria(entity)
        remoteCall("updateMateria") {
            api.updateMateria("eq.${entity.id}", entity.toSupabaseDto())
        }
    }

    suspend fun deleteMateria(id: Long) {
        materiaDao.deleteMateriaById(id)
        remoteCall("deleteMateria") { api.deleteMateria("eq.$id") }
    }

    // ── Operaciones de Tarea ─────────────────────────────────────────────────
    suspend fun insertTarea(entity: TareaEntity): Long {
        val localId = tareaDao.insertTarea(entity)
        remoteCall("upsertTarea") {
            api.upsertTarea(entity.copy(id = localId).toSupabaseDto())
        }
        return localId
    }

    suspend fun updateTarea(entity: TareaEntity) {
        tareaDao.updateTarea(entity)
        remoteCall("updateTarea") {
            api.updateTarea("eq.${entity.id}", entity.toSupabaseDto())
        }
    }

    suspend fun deleteTarea(id: Long) {
        tareaDao.deleteTareaById(id)
        remoteCall("deleteTarea") { api.deleteTarea("eq.$id") }
    }

    // ── Helper: timeout + fallback silencioso ────────────────────────────────
    private suspend fun remoteCall(op: String, block: suspend () -> Unit) {
        try {
            withTimeoutOrNull(SYNC_TIMEOUT_MS) { block() }
                ?: Log.w(TAG, "$op: timeout — continuando offline")
        } catch (e: Exception) {
            Log.w(TAG, "$op falló — Room sigue disponible: ${e.message}")
        }
    }

    // ── Mapeos Entity ↔ SupabaseDto ──────────────────────────────────────────
    private fun MateriaEntity.toSupabaseDto() = SupabaseMateriaDto(
        id       = id,      nombre  = nombre,  profesor = profesor,
        color    = color,   icono   = icono,   aula     = aula,
        horario  = horario
    )

    private fun TareaEntity.toSupabaseDto() = SupabaseTareaDto(
        id                 = id,         titulo             = titulo,
        descripcion        = descripcion, materiaId         = materiaId,
        fechaEntrega       = fechaEntrega, prioridad        = prioridad,
        estado             = estado,      imagenUri         = imagenUri,
        notaVoz            = notaVoz,     recordatorioActivo = recordatorioActivo,
        fechaCreacion      = fechaCreacion
    )

    private fun SupabaseMateriaDto.toEntity() = MateriaEntity(
        id       = id,      nombre  = nombre,  profesor = profesor,
        color    = color,   icono   = icono,   aula     = aula,
        horario  = horario
    )

    private fun SupabaseTareaDto.toEntity() = TareaEntity(
        id                 = id,         titulo             = titulo,
        descripcion        = descripcion, materiaId         = materiaId,
        fechaEntrega       = fechaEntrega, prioridad        = prioridad,
        estado             = estado,      imagenUri         = imagenUri,
        notaVoz            = notaVoz,     recordatorioActivo = recordatorioActivo,
        fechaCreacion      = fechaCreacion
    )
}
