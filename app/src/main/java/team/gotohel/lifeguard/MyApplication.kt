package team.gotohel.lifeguard

import android.app.Application
import android.content.Context

class MyApplication: Application() {
    companion object {
        private var instance: MyApplication? = null

        val context: Context
            get() = instance!!
    }
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

    }

}