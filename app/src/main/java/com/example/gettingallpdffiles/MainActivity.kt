package com.example.gettingallpdffiles

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.allPermanentlyDenied
import com.fondesa.kpermissions.allShouldShowRationale
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var btnGetAllPDF: Button
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGetAllPDF = findViewById<Button?>(R.id.btn_getAllPDF)
        btnGetAllPDF.setOnClickListener {
            checkPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermission() {
        Log.d(TAG, "checkPermission: .............")
        val request = permissionsBuilder(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
        ).build()
        request.send { result ->
            Log.d(TAG, "checkPermission: result = $result")
            when {
                result.allGranted() -> {
                    Log.d(TAG, "checkPermission: all-Granted")
//                    initiateContentShowingRelatedTasks()
                }
                result.allShouldShowRationale() -> {
                    Log.d(TAG, "checkPermission: all-Should-Show-Rationale")
//                    requireContext().showRationaleDialog(permissions = result, request = request)
                }
                result.allPermanentlyDenied() -> {
                    Log.d(TAG, "checkPermission: all-Permanently-Denied")
//                    requireContext().showPermanentlyDeniedDialog(permissions = result)
                }
                result.anyPermanentlyDenied() -> {
                    Log.d(TAG, "checkPermission: any-permanently-denied")
//                    initiateContentShowingRelatedTasks()
                }
            }
        }
    }
}