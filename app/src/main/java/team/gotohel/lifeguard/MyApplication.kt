package team.gotohel.lifeguard

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.google.firebase.FirebaseApp

class MyApplication: Application() {
    companion object {
        private var instance: MyApplication? = null

        val context: Context
            get() = instance!!

        fun toast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        fun toastDebug(message: String) {
            if (BuildConfig.DEBUG_MODE) {
                Toast.makeText(context, "[DEBUG]$message", Toast.LENGTH_SHORT).show()
            }
        }
        val API_KEY_CLARIFAI
                get() = instance?.resources?.getString(R.string.api_key_clarifai) ?: ""
        val API_KEY_RAKUTEN
                get() = instance?.resources?.getString(R.string.api_key_rakuten) ?: ""
    }
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }

}