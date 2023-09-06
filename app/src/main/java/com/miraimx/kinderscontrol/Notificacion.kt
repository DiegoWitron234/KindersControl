package com.miraimx.kinderscontrol

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class Notificacion(private val ctx: Context, params: WorkerParameters ): Worker(ctx, params) {

    private val canalNombre = "dev.xcheko51x"
    private val canalId = "canalid"
    private val notificationId = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result{
        crearCanalNotificacion()
        crearNotificacion()
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun crearCanalNotificacion() {
        val canalImportancia = NotificationManager.IMPORTANCE_HIGH
        val canal = NotificationChannel(canalId, canalNombre, canalImportancia)
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }

    private fun crearNotificacion() {
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        val resultPendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(ctx, canalId).also {
            it.setContentTitle("Notificacion")
            it.setContentText("Cuerpo de la notificacion")
            it.setSmallIcon(R.mipmap.notificacion)
            it.priority = NotificationCompat.PRIORITY_HIGH
            it.setContentIntent(resultPendingIntent)
            it.setAutoCancel(true)
        }.build()
        val notificationManager = NotificationManagerCompat.from(ctx)
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notification)
    }
}