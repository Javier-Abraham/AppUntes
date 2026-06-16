package com.appuntes.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Headers requeridos por Supabase PostgREST
                .addHeader("apikey",        SupabaseConfig.SUPABASE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_KEY}")
                .addHeader("Content-Type",  "application/json")
                .addHeader("Accept",        "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Cliente PostgREST — usar este en código nuevo
    val supabaseApi: SupabaseApiService by lazy { retrofit.create(SupabaseApiService::class.java) }

    // Mantenido por compatibilidad con código existente
    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}

sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) NetworkResult.Success(body)
            else NetworkResult.Error("Respuesta vacía del servidor", response.code())
        } else {
            NetworkResult.Error(response.errorBody()?.string() ?: "Error desconocido", response.code())
        }
    } catch (e: java.net.UnknownHostException) {
        NetworkResult.Error("Sin conexión a internet. Trabajando en modo offline.")
    } catch (e: java.net.SocketTimeoutException) {
        NetworkResult.Error("Tiempo de espera agotado. Verificá tu conexión.")
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Error inesperado")
    }
}
