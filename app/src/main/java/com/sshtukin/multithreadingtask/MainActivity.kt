package com.sshtukin.multithreadingtask

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity which contains [DownloadFragment].
 *
 * @author Sergey Shtukin
 */

class MainActivity : AppCompatActivity(), ImageLoaderService.ServiceListener {

    private val downloadFragment = DownloadFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentHolder, downloadFragment)
                .commit()
        }
    }

    override fun onDownloadCompleted() {
        downloadFragment.onDownloadCompleted()
    }
}
