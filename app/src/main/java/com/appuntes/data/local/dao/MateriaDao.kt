package com.appuntes.data.local.dao

import androidx.room.*
import com.appuntes.data.local.entity.MateriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaDao {

    @Query("SELECT * FROM materias ORDER BY nombre ASC")
    fun getAllMaterias(): Flow<List<MateriaEntity>>

    @Query("SELECT * FROM materias WHERE id = :id")
    suspend fun getMateriaById(id: Long): MateriaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateria(materia: MateriaEntity): Long

    @Update
    suspend fun updateMateria(materia: MateriaEntity)

    @Delete
    suspend fun deleteMateria(materia: MateriaEntity)

    @Query("DELETE FROM materias WHERE id = :id")
    suspend fun deleteMateriaById(id: Long)

    @Query("SELECT COUNT(*) FROM materias")
    suspend fun countMaterias(): Int
}
