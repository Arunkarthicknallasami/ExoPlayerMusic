package com.github.yoheimuta.amplayer

import android.content.ComponentName
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import com.github.yoheimuta.amplayer.playback.MusicService

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i(TAG, "onCreate")

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MusicService::class.java),
            ConnectionCallback(),
            null // optional Bundle
        )
    }

    public override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    public override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onStop() {
        super.onStop()
        mediaBrowser.disconnect()
    }

    private inner class ConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.i(TAG, "onConnected")

            mediaBrowser.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(
                    this@MainActivity, // Context
                    token
                )

                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }

            mediaBrowser.subscribe(mediaBrowser.getRoot(), SubscriptionCallback());
        }

        override fun onConnectionSuspended() {
            Log.i(TAG, "The Service has crashed")
        }

        override fun onConnectionFailed() {
            Log.i(TAG, "The Service has refused our connection")
        }
    }

    private inner class SubscriptionCallback: MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)
            if (children.isNotEmpty()) {
                val first = children.get(0).getMediaId()
                mediaController.getTransportControls().playFromMediaId(first, null)
            }
        }
    }
}

