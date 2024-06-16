package pt.ipca.keystore

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KeystoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()


    }
}
