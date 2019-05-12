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
    }
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }

}