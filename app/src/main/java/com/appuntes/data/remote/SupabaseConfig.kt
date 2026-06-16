package com.appuntes.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// ⚠️ Reemplazá estos valores con los reales desde:
// Supabase Dashboard → Project Settings → API
object SupabaseConfig {
    const val SUPABASE_URL = "https://hlxldqmxnhnlrhftjyav.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_KoJuCUUvL_UFBWVhrunIlQ_WLMowJ2O"
    const val BASE_URL     = "$SUPABASE_URL/rest/v1/"
}

// DTOs con nombres de columna en snake_case tal como los define PostgreSQL/PostgREST
data class SupabaseMateriaDto(
    @SerializedName("id")       val id: Long   = 0,
    @SerializedName("nombre")   val nombre:    String,
    @SerializedName("profesor") val profesor:  String,
    @SerializedName("color")    val color:     String,
    @SerializedName("icono")    val icono:     String,
    @SerializedName("aula")     val aula:      String = "",
    @SerializedName("horario")  val horario:   String = ""
)

data class SupabaseTareaDto(
    @SerializedName("id")                  val id:                 Long    = 0,
    @SerializedName("titulo")              val titulo:             String,
    @SerializedName("descripcion")         val descripcion:        String  = "",
    @SerializedName("materia_id")          val materiaId:          Long,
    @SerializedName("fecha_entrega")       val fechaEntrega:       String,
    @SerializedName("prioridad")           val prioridad:          String  = "MEDIA",
    @SerializedName("estado")             val estado:             String  = "PENDIENTE",
    @SerializedName("imagen_uri")          val imagenUri:          String? = null,
    @SerializedName("nota_voz")           val notaVoz:            String? = null,
    @SerializedName("recordatorio_activo") val recordatorioActivo: Boolean = false,
    @SerializedName("fecha_creacion")      val fechaCreacion:      String
)

// PostgREST API — Supabase expone las tablas directamente como REST endpoints.
// Filtros usan sintaxis "eq.<valor>" en query params, p.ej. ?id=eq.5
interface SupabaseApiService {

    // MATERIAS ─────────────────────────────────────────────────────────────
    @GET("materias")
    suspend fun getMaterias(): Response<List<SupabaseMateriaDto>>

    // resolution=merge-duplicates → UPSERT por PK (INSERT ... ON CONFLICT UPDATE)
    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("materias")
    suspend fun upsertMateria(
        @Body dto: SupabaseMateriaDto
    ): Response<List<SupabaseMateriaDto>>

    @Headers("Prefer: return=representation")
    @PATCH("materias")
    suspend fun updateMateria(
        @Query("id") idFilter: String, // pasar como "eq.{id}"
        @Body dto: SupabaseMateriaDto
    ): Response<List<SupabaseMateriaDto>>

    @DELETE("materias")
    suspend fun deleteMateria(
        @Query("id") idFilter: String  // pasar como "eq.{id}"
    ): Response<Unit>

    // TAREAS ───────────────────────────────────────────────────────────────
    @GET("tareas")
    suspend fun getTareas(): Response<List<SupabaseTareaDto>>

    @GET("tareas")
    suspend fun getTareasByMateria(
        @Query("materia_id") materiaIdFilter: String // "eq.{materiaId}"
    ): Response<List<SupabaseTareaDto>>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("tareas")
    suspend fun upsertTarea(
        @Body dto: SupabaseTareaDto
    ): Response<List<SupabaseTareaDto>>

    @Headers("Prefer: return=representation")
    @PATCH("tareas")
    suspend fun updateTarea(
        @Query("id") idFilter: String,
        @Body dto: SupabaseTareaDto
    ): Response<List<SupabaseTareaDto>>

    @DELETE("tareas")
    suspend fun deleteTarea(
        @Query("id") idFilter: String
    ): Response<Unit>
}
