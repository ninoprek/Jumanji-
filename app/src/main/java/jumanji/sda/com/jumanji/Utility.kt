package jumanji.sda.com.jumanji

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog

object Utility {
    const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun checkPermission(fragment: Fragment? = null, context: Context, callback: OnPermissionGrantedCallback) {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    val alertBuilder = AlertDialog.Builder(context)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Permission necessary")
                    alertBuilder.setMessage("Storage permission is necessary to get photos.")
                    alertBuilder.setPositiveButton(android.R.string.yes) { _, _ ->
                        if (fragment != null) {
                            fragment.requestPermissions(
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                        } else {
                            ActivityCompat.requestPermissions(context,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                        }
                    }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    if (fragment != null) {
                        fragment.requestPermissions(
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                    } else {
                        ActivityCompat.requestPermissions(context,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                    }
                }
            } else {
                callback.actionWithPermission(context)
            }
        } else {
            callback.actionWithPermission(context)
        }
    }
}
