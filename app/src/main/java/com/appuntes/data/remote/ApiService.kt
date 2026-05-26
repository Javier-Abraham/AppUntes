package com.appuntes.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class MateriaDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("profesor") val profesor: String,
    @SerializedName("color") val color: String,
    @SerializedName("icono") val icono: String,
    @SerializedName("aula") val aula: String = "",
    @SerializedName("horario") val horario: String = ""
)

data class TareaDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("materiaId") val materiaId: Long,
    @SerializedName("fechaEntrega") val fechaEntrega: String,
    @SerializedName("prioridad") val prioridad: String = "MEDIA",
    @SerializedName("estado") val estado: String = "PENDIENTE"
)

interface ApiService {
    @GET("materias")
    suspend fun getMaterias(): Response<List<MateriaDto>>
    @GET("materias/{id}")
    suspend fun getMateriaById(@Path("id") id: Long): Response<MateriaDto>
    @POST("materias")
    suspend fun createMateria(@Body materia: MateriaDto): Response<MateriaDto>
    @PUT("materias/{id}")
    suspend fun updateMateria(@Path("id") id: Long, @Body materia: MateriaDto): Response<MateriaDto>
    @DELETE("materias/{id}")
    suspend fun deleteMateria(@Path("id") id: Long): Response<Unit>
    @GET("tareas")
    suspend fun getTareas(): Response<List<TareaDto>>
    @GET("tareas/materia/{materiaId}")
    suspend fun getTareasByMateria(@Path("materiaId") materiaId: Long): Response<List<TareaDto>>
    @POST("tareas")
    suspend fun createTarea(@Body tarea: TareaDto): Response<TareaDto>
    @PUT("tareas/{id}")
    suspend fun updateTarea(@Path("id") id: Long, @Body tarea: TareaDto): Response<TareaDto>
    @DELETE("tareas/{id}")
    suspend fun deleteTarea(@Path("id") id: Long): Response<Unit>
    @PATCH("tareas/{id}/estado")
    suspend fun updateEstadoTarea(@Path("id") id: Long, @Body body: Map<String, String>): Response<TareaDto>
}
