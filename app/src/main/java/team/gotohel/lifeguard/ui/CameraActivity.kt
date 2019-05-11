package team.gotohel.lifeguard.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_camera.*
import team.gotohel.lifeguard.R
import team.gotohel.lifeguard.api.ClarifaiClient
import team.gotohel.lifeguard.api.ClarifaiUploadModel
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

//                result.toBitmap(maxWidth, maxHeight, callback)
//                result.toFile(file, callback)

                // Access the raw data if needed.
                val data = result.data

                // food name 확인
                val encoded = Base64.encodeToString(data, Base64.DEFAULT);
                ClarifaiClient.getInstance().call.getFoodInfoFromImage(ClarifaiUploadModel(encoded))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { response, e ->
                        if (response != null) {
                            val conceptList = response.outputs?.firstOrNull()?.data?.concepts
                            Toast.makeText(this@CameraActivity, "결과 = ${conceptList?.map { it.name }?.joinToString(", ")}", Toast.LENGTH_SHORT).show()

                        }
                        e?.printStackTrace()
                    }

                // 이미지 표시
                val folder = File(getExternalStorageDirectory(), "lifeguard")
                if (!folder.exists()) {
                    folder.mkdir()
                }
                val savedPhoto = File(folder, "${System.currentTimeMillis()}.jpg")
                try {
                    val outputStream = FileOutputStream(savedPhoto.path)
                    outputStream.write(data)
                    outputStream.close()
                    Glide.with(this@CameraActivity).load(savedPhoto).into(img_captured)
                    img_captured.visibility = View.VISIBLE
                } catch (e: java.io.IOException) {
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