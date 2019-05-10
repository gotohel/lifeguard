package team.gotohel.lifeguard

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_camera.*
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream


class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
    }

//    var savedPhoto: File? = null
    fun captureImage(view: View) {
        camera.captureImage { cameraKitView, capturedImage ->
            val folder = File(getExternalStorageDirectory(), "lifeguard")
            if (!folder.exists()) {
                folder.mkdir()
            }
            val savedPhoto = File(folder, "${System.currentTimeMillis()}.jpg")
            try {
                val outputStream = FileOutputStream(savedPhoto.path)
                outputStream.write(capturedImage)
                outputStream.close()

                Glide.with(this).load(savedPhoto).into(img_captured)
                img_captured.visibility = View.VISIBLE


            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        camera.onStart()
    }

    override fun onResume() {
        super.onResume()
        camera.onResume()
    }

    override fun onPause() {
        super.onPause()
        camera.onPause()
    }

    override fun onStop() {
        super.onStop()
        camera.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        camera.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}