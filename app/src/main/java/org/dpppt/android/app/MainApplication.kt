/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.internal.backend.models.ApplicationInfo
import org.dpppt.android.sdk.internal.util.ProcessUtil

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (ProcessUtil.isMainProcess(this)) {
            registerReceiver(broadcastReceiver, DP3T.getUpdateIntentFilter())
            DP3T.init(this, ApplicationInfo("it.noiapp.demo", "https://protetti.app/"))
        }
    }

    override fun onTerminate() {
        if (ProcessUtil.isMainProcess(this)) {
            unregisterReceiver(broadcastReceiver)
        }
        super.onTerminate()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("notification_shown", false)) {
                val status = DP3T.getStatus(context)
                if (status.wasContactExposed()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel()
                    }
                    var contentIntent: PendingIntent? = null

                    context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
                        contentIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    }

                    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                            .setContentTitle(context.getString(R.string.push_exposed_title))
                            .setContentText(context.getString(R.string.push_exposed_text))
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setSmallIcon(R.drawable.ic_begegnungen)
                            .setContentIntent(contentIntent)
                            .build()
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(42, notification)
                    prefs.edit().putBoolean("notification_shown", true).commit()
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelName = getString(R.string.app_name)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "contact-channel"
    }
}