package team.gotohel.lifeguard

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_allergen_list.*

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

        refreshAllergenList()
    }

    fun addAllergen(view: View) {
        val oldList = MyPreference.savedAllergenList
        val newAllergen = edit_new_allergen.text.toString().trim()

        if (newAllergen.isEmpty()) {
            Toast.makeText(this, "empty!", Toast.LENGTH_SHORT).show()
        } else if (oldList?.contains(newAllergen) == true) {
            Toast.makeText(this, "Already added", Toast.LENGTH_SHORT).show()
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
        allergenListAdapter.setList(allergenList ?: listOf())

    }

    private var permissionHelper: PermissionHelper? = null
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper?.afterRequestPermissionsResult(grantResults)
    }

    fun startCamera(view: View) {
        permissionHelper = PermissionHelper.createAndDo(this, view, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )) {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }
}