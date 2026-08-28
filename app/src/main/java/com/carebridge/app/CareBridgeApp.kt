package com.carebridge.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp

class CareBridgeApp : Application() {

    companion object {
        lateinit var instance: CareBridgeApp
            private set
        val appContext: Context get() = instance.applicationContext

        const val CHANNEL_ID_BOOKINGS = "carebridge_bookings_channel"
        const val CHANNEL_NAME_BOOKINGS = "CareBridge Bookings & Sessions"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (_: Exception) {}
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_BOOKINGS,
                CHANNEL_NAME_BOOKINGS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for booking requests, visits, check-in/out, and invoices"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
