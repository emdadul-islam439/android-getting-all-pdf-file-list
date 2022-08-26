package com.example.gettingallpdffiles

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.allPermanentlyDenied
import com.fondesa.kpermissions.allShouldShowRationale
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import java.util.*

class MainActivity : AppCompatActivity(), PickiTCallbacks {
    //Declare PickiT
    var pickiT: PickiT? = null
    val TAG = "MainActivity"

    //Views
    var button_pick_video: Button? = null
    var button_pick_image: Button? = null
    var pickitTv: TextView? = null
    var originalTv: TextView? = null
    var originalTitle: TextView? = null
    var pickitTitle: TextView? = null
    var videoImageRef = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        buttonClickEvent()

        //Initialize PickiT
        pickiT = PickiT(this, this, this)
    }

    //Show Toast
    private fun showLongToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
    }

    private fun init() {
        button_pick_video = findViewById(R.id.button_pick_video)
        button_pick_image = findViewById(R.id.button_pick_image)
        pickitTv = findViewById(R.id.pickitTv)
        originalTv = findViewById(R.id.originalTv)
        originalTitle = findViewById(R.id.originalTitle)
        pickitTitle = findViewById(R.id.pickitTitle)
    }

    private fun buttonClickEvent() {
        button_pick_video!!.setOnClickListener { view: View? ->
            videoImageRef = "video"
            openGallery("video")

            //  Make TextView's invisible
            originalTitle!!.visibility = View.INVISIBLE
            originalTv!!.visibility = View.INVISIBLE
            pickitTitle!!.visibility = View.INVISIBLE
            pickitTv!!.visibility = View.INVISIBLE
        }
        button_pick_image!!.setOnClickListener { view: View? ->
            videoImageRef = "image"
            openGallery("image")

            //  Make TextView's invisible
            originalTitle!!.visibility = View.INVISIBLE
            originalTv!!.visibility = View.INVISIBLE
            pickitTitle!!.visibility = View.INVISIBLE
            pickitTv!!.visibility = View.INVISIBLE
        }
    }

    private fun openGallery(videoOrImage: String) {
        //  first check if permissions was granted
        if (checkSelfPermission()) {
            if (videoImageRef == "video") {
                videoImageRef = ""
                val intent: Intent = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                } else {
                    Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
                }
                //  In this example we will set the type to video
                intent.type = "video/*"
                intent.action = Intent.ACTION_GET_CONTENT
                intent.putExtra("return-data", true)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activityResultLauncher.launch(intent)
            } else {
                videoImageRef = ""
                val intent: Intent
                intent = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                } else {
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                }
                //  In this example we will set the type to video
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                intent.putExtra("return-data", true)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activityResultLauncher.launch(intent)
            }
        }
    }

    //  Check if permissions was granted
    private fun checkSelfPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE
            )
            return false
        }
        return true
    }

    //  Handle permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //  Permissions was granted, open the gallery
                if (videoImageRef == "video") {
                    openGallery("video")
                } else {
                    openGallery("image")
                }
            } else {
                showLongToast("No permission for " + Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private var activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            //  Get path from PickiT (The path will be returned in PickiTonCompleteListener)
            //
            //  If the selected file is from Dropbox/Google Drive or OnDrive:
            //  Then it will be "copied" to your app directory (see path example below) and when done the path will be returned in PickiTonCompleteListener
            //  /storage/emulated/0/Android/data/your.package.name/files/Temp/tempDriveFile.mp4
            //
            //  else the path will directly be returned in PickiTonCompleteListener
            val clipData = Objects.requireNonNull(data)?.clipData
            if (clipData != null) {
                val numberOfFilesSelected = clipData.itemCount
                if (numberOfFilesSelected > 1) {
                    pickiT!!.getMultiplePaths(clipData)
                    val allPaths = StringBuilder(
                        """
                              Multiple Files Selected:
                              
                              """.trimIndent()
                    )
                    for (i in 0 until clipData.itemCount) {
                        allPaths.append("\n\n").append(clipData.getItemAt(i).uri)
                    }
                    originalTv!!.text = allPaths.toString()
                } else {
                    pickiT!!.getPath(clipData.getItemAt(0).uri, Build.VERSION.SDK_INT)
                    originalTv!!.text = clipData.getItemAt(0).uri.toString()
                }
            } else {
                pickiT!!.getPath(data!!.data, Build.VERSION.SDK_INT)
                originalTv!!.text = data.data.toString()
            }
        }
    }

    //  PickiT Listeners
    //
    //  The listeners can be used to display a Dialog when a file is selected from Dropbox/Google Drive or OnDrive.
    //  The listeners are callbacks from an AsyncTask that creates a new File of the original in /storage/emulated/0/Android/data/your.package.name/files/Temp/
    //
    //  PickiTonUriReturned()
    //  When selecting a file from Google Drive, for example, the Uri will be returned before the file is available(if it has not yet been cached/downloaded).
    //  Google Drive will first have to download the file before we have access to it.
    //  This can be used to let the user know that we(the application), are waiting for the file to be returned.
    //
    //  PickiTonStartListener()
    //  This will be call once the file creations starts and will only be called if the selected file is not local
    //
    //  PickiTonProgressUpdate(int progress)
    //  This will return the progress of the file creation (in percentage) and will only be called if the selected file is not local
    //
    //  PickiTonCompleteListener(String path, boolean wasDriveFile)
    //  If the selected file was from Dropbox/Google Drive or OnDrive, then this will be called after the file was created.
    //  If the selected file was a local file then this will be called directly, returning the path as a String
    //  Additionally, a boolean will be returned letting you know if the file selected was from Dropbox/Google Drive or OnDrive.
    private lateinit var mProgressBar: ProgressBar
    lateinit var percentText: TextView
    private var mdialog: AlertDialog? = null
    lateinit var progressBar: ProgressDialog
    override fun PickiTonUriReturned() {
        progressBar = ProgressDialog(this)
        progressBar.setMessage("Waiting to receive file...")
        progressBar.setCancelable(false)
        progressBar.show()
    }

    override fun PickiTonStartListener() {
        if (progressBar.isShowing) {
            progressBar.cancel()
        }
        val mPro = AlertDialog.Builder(ContextThemeWrapper(this, R.style.myDialog))
        @SuppressLint("InflateParams") val mPView: View =
            LayoutInflater.from(this).inflate(R.layout.dailog_layout, null)
        percentText = mPView.findViewById(R.id.percentText)
        percentText.setOnClickListener(View.OnClickListener {
            pickiT!!.cancelTask()
            if (mdialog != null && mdialog!!.isShowing) {
                mdialog!!.cancel()
            }
        })
        mProgressBar = mPView.findViewById(R.id.mProgressBar)
        mProgressBar.setMax(100)
        mPro.setView(mPView)
        mdialog = mPro.create()
        mdialog!!.show()
    }

    override fun PickiTonProgressUpdate(progress: Int) {
        val progressPlusPercent = "$progress%"
        percentText.text = progressPlusPercent
        mProgressBar.progress = progress
    }

    @SuppressLint("SetTextI18n")
    override fun PickiTonCompleteListener(
        path: String,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        reason: String
    ) {
        if (mdialog != null && mdialog!!.isShowing) {
            mdialog!!.cancel()
        }

        //  Check if it was a Drive/local/unknown provider file and display a Toast
        if (wasDriveFile) {
            showLongToast("Drive file was selected")
        } else if (wasUnknownProvider) {
            showLongToast("File was selected from unknown provider")
        } else {
            showLongToast("Local file was selected")
        }

        //  Chick if it was successful
        if (wasSuccessful) {
            //  Set returned path to TextView
            if (path.contains("/proc/")) {
                pickitTv!!.text =
                    "Sub-directory inside Downloads was selected.\n We will be making use of the /proc/ protocol.\n You can use this path as you would normally.\n\nPickiT path:\n$path"
            } else {
                pickitTv!!.text = path
            }

            //  Make TextView's visible
            originalTitle!!.visibility = View.VISIBLE
            originalTv!!.visibility = View.VISIBLE
            pickitTitle!!.visibility = View.VISIBLE
            pickitTv!!.visibility = View.VISIBLE
        } else {
            showLongToast("Error, please see the log..")
            pickitTv!!.visibility = View.VISIBLE
            pickitTv!!.text = reason
        }
    }

    override fun PickiTonMultipleCompleteListener(
        paths: ArrayList<String>,
        wasSuccessful: Boolean,
        Reason: String
    ) {
        if (mdialog != null && mdialog!!.isShowing) {
            mdialog!!.cancel()
        }
        val allPaths = StringBuilder()
        for (i in paths.indices) {
            allPaths.append("\n").append(paths[i]).append("\n")
        }

        //  Set returned path to TextView
        pickitTv!!.text = allPaths.toString()

        //  Make TextView's visible
        originalTitle!!.visibility = View.VISIBLE
        originalTv!!.visibility = View.VISIBLE
        pickitTitle!!.visibility = View.VISIBLE
        pickitTv!!.visibility = View.VISIBLE
    }

    //
    //  Lifecycle methods
    //
    //  Deleting the temporary file if it exists
    override fun onBackPressed() {
        pickiT!!.deleteTemporaryFile(this)
        super.onBackPressed()
    }

    //  Deleting the temporary file if it exists
    //  As we know, this might not even be called if the system kills the application before onDestroy is called
    //  So, it is best to call pickiT.deleteTemporaryFile(); as soon as you are done with the file
    public override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            pickiT!!.deleteTemporaryFile(this)
        }
    }

    companion object {
        //Permissions
        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE =
            PERMISSION_REQ_ID_RECORD_AUDIO + 1
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