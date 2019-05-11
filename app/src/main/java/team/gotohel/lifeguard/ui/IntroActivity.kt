package team.gotohel.lifeguard.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import team.gotohel.lifeguard.R

class IntroActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {// in Activity's onCreate() for instance
            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            statusBarColor = Color.TRANSPARENT
        }
//        window.apply {// in Activity's onCreate() for instance
//            setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            statusBarColor = Color.TRANSPARENT
//        }
        setContentView(R.layout.activity_intro)

//        finish()
//        startActivity(Intent(this, CameraActivity::class.java))
    }

    fun goToAllergyListActivity(view: View) {
        finish()
        startActivity(Intent(this, AllergenListActivity::class.java))
    }
}