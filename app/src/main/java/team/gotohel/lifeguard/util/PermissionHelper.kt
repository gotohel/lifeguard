package team.gotohel.lifeguard.util


import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.TextView
import team.gotohel.lifeguard.R

import java.util.ArrayList

/**
 * Created by HwaminSon on 2018-10-16.
 */

class PermissionHelper private constructor(
        private val activity: Activity,
        //스낵바 띄울때 필요한 부모 뷰.
        private val snackbarParentView: View?,
        private val requiredPermissions: Array<String>,
        private val permissionCallback: () -> Unit) {

    companion object {
        const val REQUEST_CODE_PERMISSION = 0

        fun createAndDo(activity: Activity, snackbarParentView: View?, requiredPermissions: Array<String>, permissionCallback: () -> Unit): PermissionHelper {
            val permissionHelper = PermissionHelper(
                activity,
                snackbarParentView,
                requiredPermissions,
                permissionCallback
            )
            permissionHelper.requestPermissionAndDoNext()
            return permissionHelper
        }
    }

    private fun requestPermissionAndDoNext() {
        val deniedPermissionList = deniedPermissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || deniedPermissionList.isEmpty()) {
            // 권한을 가지고 하는 행동.
            permissionCallback.invoke()
        } else {
            ActivityCompat.requestPermissions(activity, deniedPermissionList,
                REQUEST_CODE_PERMISSION
            )
        }
    }

    private val deniedPermissions: Array<String>
        get() {
            val deniedPermissionList = ArrayList<String>()

            for (permission in requiredPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permission)
                }
            }

            return deniedPermissionList.toTypedArray()
        }

    var errorMessage: String = "권한이 없이는 이 기능을 사용할 수 없습니다."
    var finalErrorMessage: String = "권한이 없이는 이 기능을 사용할 수 없습니다."

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionAgain() {

        val deniedPermissionList = deniedPermissions
        var showRationale = false

        for (permission in deniedPermissionList) {
            showRationale = showRationale || ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }

        snackbarParentView?.let { parentView ->
            val snackbar = Snackbar.make(parentView, if (showRationale) errorMessage else finalErrorMessage, Snackbar.LENGTH_INDEFINITE)
            snackbar.setActionTextColor(activity.resources.getColor(R.color.colorPrimary))

            if (showRationale) {
                snackbar.setAction("권한 수락") { _ -> ActivityCompat.requestPermissions(activity, deniedPermissionList,
                    REQUEST_CODE_PERMISSION
                ) }
                        .show()
            } else {
                snackbar.setAction("수동 권한 설정") { _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", activity.packageName, null)
                    activity.startActivity(intent)
                }
                        .show()
            }
        }
    }

    fun afterRequestPermissionsResult(grantResults: IntArray) {
        var deniedSomething = false
        for (grantResult in grantResults) {
            deniedSomething = deniedSomething || grantResult == PackageManager.PERMISSION_DENIED
        }

        if (deniedSomething) { // 적어도 하나 거절당함..
            requestPermissionAgain()
        } else { // 모든 권한 다 받음.
//            requestPermissionAndDoNext()
            permissionCallback.invoke()
        }
    }
}
