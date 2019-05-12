package team.gotohel.lifeguard.ui

import android.content.Intent
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
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
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_camera.*
import team.gotohel.lifeguard.R
import team.gotohel.lifeguard.api.*
import team.gotohel.lifeguard.ui.ResultActivity.Companion.KEY_IMAGE_FILE_NAME
import team.gotohel.lifeguard.ui.ResultActivity.Companion.KEY_INGREDIENT_LIST
import java.io.File
import java.io.FileOutputStream
import kotlin.Exception

class CameraActivity : AppCompatActivity() {
    companion object {
        const val TEXT_SIZE_LARGE = 25F
        const val TEXT_SIZE_SMALL = 13F
        const val LIFEGUARD_FOLDER_NAME = "lifeguard"

        val barcodeRecognitionMetadata: FirebaseVisionImageMetadata = FirebaseVisionImageMetadata.Builder()
            .setRotation(FirebaseVisionImageMetadata.ROTATION_0).build()
    }

    enum class MODE {
        FOOD_CAPTURE, BARCODE_SCAN
    }

    var currentMode: MODE? = null
    private lateinit var firebaseVisionBarcodeDetectorOptions: FirebaseVisionBarcodeDetectorOptions
    private lateinit var firebaseVisionBarcodeDetector: FirebaseVisionBarcodeDetector

    var savedImageFileName: String? = null
    var ingredientListResult: List<Pair<String, String?>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            // in Activity's onCreate() for instance
            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_camera)

        progress_bar.indeterminateDrawable.setColorFilter(resources.getColor(R.color.red_main), PorterDuff.Mode.MULTIPLY)

        firebaseVisionBarcodeDetectorOptions = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS).build()

        firebaseVisionBarcodeDetector = FirebaseVision.getInstance()
            .getVisionBarcodeDetector(firebaseVisionBarcodeDetectorOptions)

        initialCameraSettings()
        changeToFoodCaptureMode()
    }

    private fun initialCameraSettings() {
        camera2.setLifecycleOwner(this)
        camera2.addCameraListener(object : CameraListener() {
            @SuppressLint("CheckResult")
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)

//                result.toBitmap(maxWidth, maxHeight, callback)
//                result.toFile(file, callback)

                // Access the raw data if needed.
                val data = result.data

                // 이미지 저장
                saveImageToFile(data)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { fileName, e ->
                        if (fileName != null) {
                            savedImageFileName = fileName
                            checkResultAndGoToNextActivity()
                        } else {
                            detectFailed()
                        }
                        e?.printStackTrace()
                    }

                if (currentMode == MODE.FOOD_CAPTURE) {
                    // food name 확인
                    val encoded = Base64.encodeToString(data, Base64.DEFAULT);
                    ClarifaiClient.getInstance().call.getFoodInfoFromImage(ClarifaiUploadModel(encoded))
                        .flatMap { clarifaiResponseModel ->
                            val conceptList = clarifaiResponseModel.outputs?.firstOrNull()?.data?.concepts
                            Log.d("테스트", "결과1 = ${conceptList?.map { it.name }?.joinToString(", ")}")

                            val firstName = conceptList?.firstOrNull()?.name
                            if (firstName != null) {
                                RecipesClient.getInstance().call.searchRecipes(firstName)
                            } else {
                                throw Exception("이미지 인식 실패")
                            }
                        }
                        .flatMap { recipesResponse ->
                            Log.d("테스트", "결과2 = ${recipesResponse.results?.map { it.title }?.joinToString(",")}")
                            val recipesId = recipesResponse.results?.firstOrNull()?.id
                            if (recipesId != null) {
                                RecipesClientTextHtml.getInstance().call.getIngredientByRecipes(recipesId)
                            } else {
                                throw Exception("레시피 조회 실패")
                            }
                        }
                        .flatMap { html ->
                            RecipesClientTextHtml.getContentsFromWeb(html)
                        }
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { ingredientList, e ->
                            if (ingredientList != null) {
                                Toast.makeText(
                                    this@CameraActivity,
                                    "Ingredient result = $ingredientList",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("테스트", "결과3 = Ingredient result =  ${ingredientList.joinToString(", ")}")
                                ingredientListResult = ingredientList
                                checkResultAndGoToNextActivity()
                            } else {
                                detectFailed()
                            }
                            e?.printStackTrace()
                        }
                } else {
                    // 바코드
                    firebaseVisionBarcodeDetector.detectInImage(
                        FirebaseVisionImage.fromBitmap(
                            BitmapFactory.decodeByteArray(
                                data,
                                0,
                                data.size
                            )
                        )
                    ).addOnSuccessListener { barcodeRecognitionMetadata ->
                        for (x in 0 until barcodeRecognitionMetadata.size) {
                            val barcode = barcodeRecognitionMetadata[x]
                            val valueType = barcode.valueType

                            Log.e("TEST", "asdf - ${valueType}")

                            if (valueType == FirebaseVisionBarcode.TYPE_PRODUCT && barcode.rawValue != null) {
                                NutritionixClient.getInstance().call.searchFoodByUpc(barcode.rawValue!!)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { nutritionixResponse, e ->
                                        if (nutritionixResponse != null) {
                                            val ingredientList = nutritionixResponse.nf_ingredient_statement
                                                .split(",")
                                                .flatMap { it.split("(") }
                                                .flatMap { it.split(")") }
                                                .flatMap { it.split("and") }
                                                .flatMap { it.split(".") }
                                                .map { it.trim() }
                                                .filter { it.isNotBlank() }
                                                .map { Pair(it, null) }

                                            ingredientListResult = ingredientList
                                            checkResultAndGoToNextActivity()
                                        } else {
                                            detectFailed()
                                        }
                                        e?.printStackTrace()
                                    }
                            } else {
                                Toast.makeText(this@CameraActivity, "can not found barcode", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.addOnFailureListener {
                        it.printStackTrace()
                    }
                }
            }
        })
    }

    fun detectFailed() {
        Toast.makeText(this, "failed.. ", Toast.LENGTH_SHORT).show()
        savedImageFileName = null
        ingredientListResult = null
        img_captured.setImageDrawable(null)
        img_captured.visibility = View.GONE
        part_progress_recognizing.visibility = View.GONE
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

    fun saveImageToFile(data: ByteArray): Single<String> {
        return Single.create {
            val folder = File(getExternalStorageDirectory(), LIFEGUARD_FOLDER_NAME)
            if (!folder.exists()) {
                folder.mkdir()
            }
            val fileName = System.currentTimeMillis().toString()
            val savedPhoto = File(folder, "$fileName.jpg")
            try {
                val outputStream = FileOutputStream(savedPhoto.path)
                outputStream.write(data)
                outputStream.close()

                img_captured.visibility = View.VISIBLE
                Glide.with(this@CameraActivity).load(savedPhoto).into(img_captured)

                it.onSuccess(fileName)
            } catch (e: java.io.IOException) {
                e.printStackTrace()
                it.onError(e)
            }
        }
    }

    fun checkResultAndGoToNextActivity() {
        Log.d("테스트", "checkResultAndGoToNextActivity")
        if (savedImageFileName != null && ingredientListResult != null) {
            finish()
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra(KEY_IMAGE_FILE_NAME, savedImageFileName)
            intent.putExtra(KEY_INGREDIENT_LIST, ingredientListResult as? ArrayList<Pair<String, String?>>)

            startActivity(intent)
        } else {
            Log.d("테스트", "checkResultAndGoToNextActivity failed")
        }
    }

    //    var savedPhoto: File? = null
    fun captureImage(view: View) {
        Log.d("테스트", "capture Image")
        savedImageFileName = null
        ingredientListResult = null
        camera2.takePicture()
        part_progress_recognizing.visibility = View.VISIBLE
    }

    fun finishActivity(view: View) {
        finish()
    }
}