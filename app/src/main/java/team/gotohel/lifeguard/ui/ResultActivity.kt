package team.gotohel.lifeguard.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_result.*
import team.gotohel.lifeguard.MyApplication
import team.gotohel.lifeguard.R

class ResultActivity: AppCompatActivity() {
    companion object {
        const val KEY_IMAGE_FILE_NAME = "KEY_IMAGE_FILE_NAME"
        const val KEY_INGREDIENT_LIST = "KEY_INGREDIENT_LIST"
    }

    var resultListAdapter: ResultListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {// in Activity's onCreate() for instance
            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_result)

        val imageFileName = intent?.getStringExtra(KEY_IMAGE_FILE_NAME)
        val ingredientList = intent?.getSerializableExtra(KEY_INGREDIENT_LIST) as? ArrayList<Pair<String, String?>>

        if (imageFileName != null && ingredientList != null) {
            resultListAdapter = ResultListAdapter(this, imageFileName, ingredientList)
            list_result.adapter = resultListAdapter

        } else {
            MyApplication.toast("error...")
            finish()
        }
    }

    fun finishActivity(view: View) {
        finish()
    }
}