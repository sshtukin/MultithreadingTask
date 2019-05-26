package com.sshtukin.multithreadingtask

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_download.*

/**
 * Fragment with [ImageView] and [btnLoad] for loading image.
 *
 * @author Sergey Shtukin
 */

class DownloadFragment : Fragment() {

    private lateinit var imageLoaderService: ImageLoaderService
    private var intentService: Intent? = null
    private var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentService = Intent(activity, ImageLoaderService::class.java)
        activity?.startService(intentService)
        retainInstance = true
    }

    override fun onStart() {
        super.onStart()
        activity?.bindService(intentService, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_download, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnLoad.setOnClickListener {
            imageLoaderService.downloadImage(IMAGE_URL)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            activity?.unbindService(connection)
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.stopService(intentService)
    }

    fun updateImageView() {
        if (imageLoaderService.bitmap != null) {
            imageView?.setImageBitmap(imageLoaderService.bitmap)
        }
    }

    fun onDownloadCompleted() {
        imageView?.post {
            updateImageView()
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            imageLoaderService = (service as ImageLoaderService.LocalBinder).getService()
            isBound = true
            imageLoaderService.callBack = activity as MainActivity
            updateImageView()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            isBound = false
        }
    }

    companion object {
        private const val IMAGE_URL =
            "https://s26sas.storage.yandex.net/rdisk/56adb18bdfbb7fd0b0e558ea3bc41289847e67d935fa12ba89130db8bc92e970/5ceb0fb8/8tA_fRQHNT5eeUsS0OzfCZfOB6uD46zym_9jREWZclWeOi_hTE0Ppt89RN9VVKcgyfSpEasPaSyVOIIvShbO-g==?uid=323544744&filename=rebelhelmet.png&disposition=inline&hash=&limit=0&content_type=image%2Fpng&fsize=16075465&hid=179ebebb41ee0b46bfa0f27ca4f4c6cd&media_type=image&tknv=v2&etag=a58732077919c2dee8cd13293b0533b3&rtoken=Eqbpg8UTkT9N&force_default=yes&ycrid=na-a9ca3b537fcea3b0d5a7c2d2dfaabbe6-downloader14h&ts=589d1c0995e00&s=434e996bdb5f00e0e8d61430d5ea819bc1ad104f510abaf103cdd7a74158efcc&pb=U2FsdGVkX18r7KM3ZwhvEaBTZ1s852GnUtrQUCPpfoCOaY4K5PzNjE1dEawnPUpWqy2p4bPKQ3NcN0yqjL9fG_g5I49mzjMK2sSf8MPTZnE"
    }
}