package com.appuntes.data.local.dao

import androidx.room.*
import com.appuntes.data.local.entity.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {

    @Query("SELECT * FROM tareas ORDER BY fechaEntrega ASC")
    fun getAllTareas(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE materiaId = :materiaId ORDER BY fechaEntrega ASC")
    fun getTareasByMateria(materiaId: Long): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE estado != 'COMPLETADA' ORDER BY fechaEntrega ASC")
    fun getTareasPendientes(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun getTareaById(id: Long): TareaEntity?

    @Query("SELECT COUNT(*) FROM tareas WHERE materiaId = :materiaId AND estado != 'COMPLETADA'")
    fun countTareasPendientesByMateria(materiaId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) * 1.0 / NULLIF((SELECT COUNT(*) FROM tareas WHERE materiaId = :materiaId), 0)
        FROM tareas WHERE materiaId = :materiaId AND estado = 'COMPLETADA'
    """)
    fun getProgresoMateria(materiaId: Long): Flow<Float>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: TareaEntity): Long

    @Update
    suspend fun updateTarea(tarea: TareaEntity)

    @Delete
    suspend fun deleteTarea(tarea: TareaEntity)

    @Query("DELETE FROM tareas WHERE id = :id")
    suspend fun deleteTareaById(id: Long)

    @Query("UPDATE tareas SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Long, estado: String)
}
