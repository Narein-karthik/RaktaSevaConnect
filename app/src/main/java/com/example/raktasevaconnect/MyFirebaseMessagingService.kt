package com.example.raktasevaconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService :
    FirebaseMessagingService() {

    override fun onMessageReceived(
        message: RemoteMessage
    ) {

        super.onMessageReceived(message)

        createNotificationChannel()

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val builder =
            NotificationCompat.Builder(
                this,
                "blood_alerts"
            )

                .setSmallIcon(
                    android.R.drawable.ic_dialog_alert
                )

                .setContentTitle(
                    message.notification?.title
                )

                .setContentText(
                    message.notification?.body
                )

                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )

                .setContentIntent(
                    pendingIntent
                )

                .setAutoCancel(true)

        NotificationManagerCompat
            .from(this)
            .notify(1, builder.build())
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    "blood_alerts",
                    "Blood Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )

            val manager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(
                channel
            )
        }
    }
}