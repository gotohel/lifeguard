package team.gotohel.lifeguard.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_allergen_list.*
import team.gotohel.lifeguard.MyApplication
import team.gotohel.lifeguard.MyPreference
import team.gotohel.lifeguard.util.PermissionHelper
import team.gotohel.lifeguard.R

class AllergenListActivity: AppCompatActivity() {

    val allergenListAdapter = AllergenListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.apply {// in Activity's onCreate() for instance
//            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//            statusBarColor = Color.TRANSPARENT
//        }
        setContentView(R.layout.activity_allergen_list)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        list_allergen.adapter = allergenListAdapter

        edit_new_allergen.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addAllergen(v)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        refreshAllergenList()
    }

    fun addAllergen(view: View) {
        val oldList = MyPreference.savedAllergenList
        val newAllergen = edit_new_allergen.text.toString().trim()

        if (newAllergen.isEmpty()) {
            MyApplication.toast("empty")
        } else if (oldList?.contains(newAllergen) == true) {
            MyApplication.toast("Already added")
        } else {
            MyPreference.addAllergen(newAllergen)
            edit_new_allergen.setText("")
            refreshAllergenList()
        }
    }

    fun refreshAllergenList() {
        val allergenList = MyPreference.savedAllergenList
        if (allergenList == null) {
            content_empty.visibility = View.VISIBLE
            content_list.visibility = View.GONE
        } else {
            content_empty.visibility = View.GONE
            content_list.visibility = View.VISIBLE
        }
        text_detail_allergen_list.text = ("${allergenList?.size ?: 0} ingredients causing allergies.")
        allergenListAdapter.setList(allergenList ?: listOf())
    }

    private var permissionHelper: PermissionHelper? = null
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper?.afterRequestPermissionsResult(grantResults)
    }

    fun startCamera(view: View) {
        permissionHelper = PermissionHelper.createAndDo(
            this, view, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }
}