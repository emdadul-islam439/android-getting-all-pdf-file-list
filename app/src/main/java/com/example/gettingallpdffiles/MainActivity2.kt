package com.example.gettingallpdffiles

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.allPermanentlyDenied
import com.fondesa.kpermissions.allShouldShowRationale
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import java.util.*


class MainActivity2 : AppCompatActivity() {
    val TAG = "MainActivity2"
    var isFetchingComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val btnGetAllPdf = findViewById<Button>(R.id.btn_getAllPdf)
        btnGetAllPdf.setOnClickListener {
            Log.d(TAG, "onCreate: btnGetAllPdf clicked...........")
            checkPermission()
        }
    }

    private fun getPdfList(): List<String>? {
        val pdfList: MutableList<String> = ArrayList()
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgs = arrayOf(mimeType)
        var collection: Uri = MediaStore.Files.getContentUri("external")
        pdfList.addAll(getPdfList(collection, projection, selection, selectionArgs, sortOrder)!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Downloads.getContentUri("external")
            pdfList.addAll(
                getPdfList(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )!!
            )
        }
        return pdfList
    }

    private fun getPdfList(
        collection: Uri,
        projection: Array<String>,
        selection: String,
        selectionArgs: Array<String?>,
        sortOrder: String
    ): List<String>? {
        val pdfList: MutableList<String> = ArrayList()
        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    do {
                        pdfList.add(cursor.getString(columnData))
                        Log.d(TAG, "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        return pdfList
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermission() {
        Log.d(TAG, "checkPermission: .............")
        val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionsBuilder(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            ).build()
        } else {
            permissionsBuilder(Manifest.permission.MANAGE_EXTERNAL_STORAGE).build()
        }
        request.send { result ->
            Log.d(TAG, "checkPermission: result = $result")
            when {
                result.allGranted() -> {
                    Log.d(TAG, "checkPermission: all-Granted  pdfList = ${getPdfList()}")
//                    initiateContentShowingRelatedTasks()
                }
                result.allShouldShowRationale() -> {
                    Log.d(TAG, "checkPermission: all-Should-Show-Rationale")
//                    requireContext().showRationaleDialog(permissions = result, request = request)
                }
                result.allPermanentlyDenied() -> {
                    Log.d(TAG, "checkPermission: all-Permanently-Denied")
                    if(Environment.isExternalStorageManager()){
                        Log.d(TAG, "checkPermission: ${getPdfList()}.................")
                    }else{
                        this.showPermanentlyDeniedDialog(permissions = result)
                    }
                }
                result.anyPermanentlyDenied() -> {
                    Log.d(TAG, "checkPermission: any-permanently-denied  pdfList = ${getPdfList()}")
//                    initiateContentShowingRelatedTasks()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        if(Environment.isExternalStorageManager() and !isFetchingComplete){
            Log.d(TAG, "checkPermission: ${getPdfList()}.................")
            isFetchingComplete = true
        }
    }
}