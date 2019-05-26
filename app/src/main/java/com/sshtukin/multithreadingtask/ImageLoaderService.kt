package com.sshtukin.multithreadingtask

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.net.URL

/**
 * Service for loading images.
 *
 * @author Sergey Shtukin
 */

class ImageLoaderService : Service() {

    lateinit var savedUrl: String
    var bitmap: Bitmap? = null
    var callBack: ServiceListener? = null
    var loadingIsBegan = false
    var downloadFailed = false


    override fun onBind(intent: Intent?): IBinder? {
        return LocalBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onLost(network: Network?) {
            super.onAvailable(network)
            if (loadingIsBegan) {
                downloadFailed = true
            }
        }

        override fun onAvailable(network: Network?) {
            super.onAvailable(network)
            if (downloadFailed) {
                downloadImage(savedUrl)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val networkRequest =
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun downloadImage(url: String) {
        savedUrl = url
        val notification = getNotificationBuilder()
            .setProgress(0, 0, true)
            .build()

        startForeground(FOREGROUND_ID, notification)
        Thread {
            loadingIsBegan = true
            bitmap = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
            stopForeground(true)
            loadingIsBegan = false
            callBack?.onDownloadCompleted()
        }.start()
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        } else ""

        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.downloading))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            createNotificationChannel(channel)
        }
        return channelId
    }

    inner class LocalBinder : Binder() {
        fun getService(): ImageLoaderService {
            return this@ImageLoaderService
        }
    }

    interface ServiceListener {
        fun onDownloadCompleted()
    }

    companion object {
        const val FOREGROUND_ID = 4242
        const val CHANNEL_NAME = "channel_name"
        const val CHANNEL_ID = "channel_id"
    }
}