package uz.unidev.musicplayer.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import uz.unidev.musicplayer.BuildConfig
import uz.unidev.musicplayer.utils.Constants.CHANNEL_ID

/**
 *  Created by Nurlibay Koshkinbaev on 22/09/2022 18:49
 */

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Now playing song", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is a important channel for showing song!!"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}