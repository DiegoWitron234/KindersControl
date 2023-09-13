package com.miraimx.kinderscontrol

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Manejar la notificación recibida aquí
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body

            // Mostrar la notificación en la barra de notificaciones
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannelId = "1" // Define un ID de canal de notificación
            val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)

            // Configura el canal de notificación si estás usando Android 8.0 (Oreo) o superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    notificationChannelId,
                    "Nombre del Canal",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(notificationChannel)
            }

            notificationManager.notify(0, notificationBuilder.build())
        }
    }
}
