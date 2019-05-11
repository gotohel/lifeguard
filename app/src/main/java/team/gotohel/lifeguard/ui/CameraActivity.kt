package team.gotohel.lifeguard.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_camera.*
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.camerakit.CameraKitView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import team.gotohel.lifeguard.R
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {
    companion object {
        const val TEXT_SIZE_LARGE = 25F
        const val TEXT_SIZE_SMALL = 13F
    }

    enum class MODE {
        FOOD_CAPTURE, BARCODE_SCAN
    }

    var currentMode: MODE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {// in Activity's onCreate() for instance
            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_camera)
        camera2.setLifecycleOwner(this);

        camera2.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)

                // Picture was taken!
                // If planning to show a Bitmap, we will take care of
                // EXIF rotation and background threading for you...
//                result.toBitmap(maxWidth, maxHeight, callback)

                // If planning to save a file on a background thread,
                // just use toFile. Ensure you have permissions.
//                result.toFile(file, callback)

                // Access the raw data if needed.
                val data = result.data




                Log.d("테스트", "capture Image received")
                val folder = File(getExternalStorageDirectory(), "lifeguard")
                if (!folder.exists()) {
                    Log.d("테스트", "folder is not exist")
                    folder.mkdir()
                }
                val savedPhoto = File(folder, "${System.currentTimeMillis()}.jpg")
                try {
                    val outputStream = FileOutputStream(savedPhoto.path)
                    outputStream.write(data)
                    outputStream.close()
                    Glide.with(this@CameraActivity).load(savedPhoto).into(img_captured)
                    img_captured.visibility = View.VISIBLE
                    Log.d("테스트", "file load success")
                } catch (e: java.io.IOException) {
                    Log.d("테스트", "error")
                    e.printStackTrace()
                }

            }
        })

        changeToFoodCaptureMode()
    }

    fun changeToFoodCaptureMode(view: View? = null) {
        if (currentMode != MODE.FOOD_CAPTURE) {
            currentMode = MODE.FOOD_CAPTURE
            btn_food_capture.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_LARGE)
            btn_food_capture.alpha = 1.0F
            btn_barcode_scan.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL)
            btn_barcode_scan.alpha = 0.5F

            part_food_capture.visibility = View.VISIBLE
            part_barcode_scan.visibility = View.GONE
        }

    }

    fun changeToBarcodeScanMode(view: View? = null) {
        if (currentMode != MODE.BARCODE_SCAN) {
            currentMode = MODE.BARCODE_SCAN
            btn_food_capture.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SMALL)
            btn_food_capture.alpha = 0.5F
            btn_barcode_scan.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_LARGE)
            btn_barcode_scan.alpha = 1.0F

            part_food_capture.visibility = View.GONE
            part_barcode_scan.visibility = View.VISIBLE
        }
    }

    //    var savedPhoto: File? = null
    fun captureImage(view: View) {
        Log.d("테스트", "capture Image")
        camera2.takePicture()
    }

    fun finishActivity(view: View) {
        finish()
    }
}