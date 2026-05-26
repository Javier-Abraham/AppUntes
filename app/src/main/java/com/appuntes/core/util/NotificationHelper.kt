package com.appuntes.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "appuntes_recordatorios"
    const val CHANNEL_NAME = "Recordatorios de tareas"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de vencimiento de tareas"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    fun programarRecordatorio(context: Context, tareaId: Long, tituloTarea: String, fechaEntrega: LocalDate) {
        val momentoVencimiento = fechaEntrega.atTime(9, 0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val momentoRecordatorio = momentoVencimiento - TimeUnit.HOURS.toMillis(24)
        val delayMs = momentoRecordatorio - System.currentTimeMillis()
        if (delayMs <= 0) return
        val inputData = workDataOf(
            RecordatorioWorker.KEY_TAREA_ID to tareaId,
            RecordatorioWorker.KEY_TITULO to tituloTarea
        )
        val request = OneTimeWorkRequestBuilder<RecordatorioWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("recordatorio_$tareaId")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "recordatorio_$tareaId", ExistingWorkPolicy.REPLACE, request
        )
    }

    fun cancelarRecordatorio(context: Context, tareaId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("recordatorio_$tareaId")
    }
}

class RecordatorioWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_TAREA_ID = "tarea_id"
        const val KEY_TITULO = "titulo"
    }

    override suspend fun doWork(): Result {
        val tareaId = inputData.getLong(KEY_TAREA_ID, -1L)
        val titulo = inputData.getString(KEY_TITULO) ?: "Tarea sin título"
        if (tareaId == -1L) return Result.failure()
        mostrarNotificacion(tareaId, titulo)
        return Result.success()
    }

    private fun mostrarNotificacion(tareaId: Long, titulo: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificacion = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Tarea por vencer mañana")
            .setContentText(titulo)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tu tarea \"$titulo\" vence mañana. ¡No te olvides!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(tareaId.toInt(), notificacion)
    }
}
