package team.gotohel.lifeguard

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class AllergyListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allergy_list)
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