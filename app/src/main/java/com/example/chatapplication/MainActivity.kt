package com.example.chatapplication

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    var activityForResult = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            Log.e("image uri", "image uri ${data?.data}")

//                doSomeOperations()

        }
    }
    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {

//            latestTmpUri?.let { uri ->
//                previewImage.setImageURI(uri)
//            }
            }
        }

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        uri?.let { previewImage.setImageURI(uri) }
        }


    private var recorder: MediaRecorder? = null
    var mStartPlaying = true
    var mStartRecording = true
    private lateinit var filePath: File
    lateinit var record: Button
    private var player: MediaPlayer? = null
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {
                Log.i("DEBUG", "permission granted")
                // Permission is granted. Continue the action or workflow in your
                // app.
                record.isEnabled = true
            } else {
                Log.i("DEBUG", "permission denied")
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val downloadmanager: DownloadManager =
            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri =
            Uri.parse("https://upload.wikimedia.org/wikipedia/commons/e/e7/Java_Programming.pdf")
        val request = DownloadManager.Request(uri)
//        request.setTitle("Java_Programming.pdf")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        val fileName =
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        filePath = File(getOutputDirectory(), "$fileName.pdf")
        Log.e("fileese", "file ${Uri.parse(getOutputDirectory().absolutePath)}")
        request.setDestinationUri(Uri.fromFile(filePath))
        downloadmanager.enqueue(request)

        val galleryImage = findViewById<Button>(R.id.open_gallery)
        galleryImage.setOnClickListener {
            selectImageFromGallery()
        }
        val image = findViewById<Button>(R.id.capt_image)
        image.setOnClickListener {takeImage()
//            showImagesDialog()
        }
        val play = findViewById<Button>(R.id.playButton)
        record = findViewById<Button>(R.id.recordButton)
        setupPermissions()
        record.setOnClickListener {
            record.text = when (mStartRecording) {
                true -> "Stop recording"
                false -> "Start recording"
            }
            if (mStartRecording) {
                val fileName =
                    SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
                filePath = File(getOutputDirectory(), "$fileName.3gp")

                Log.e("fileName","fileName record $fileName" )
            }
            onRecord(mStartRecording)

            mStartRecording = !mStartRecording


        }
        play.setOnClickListener {
            Log.e("file", "path ${getExternalFilesDir("")} ")
            onPlay(mStartPlaying)
            play.text = when (mStartPlaying) {
                true -> "Stop playing"
                false -> "Start playing"
            }
            mStartPlaying = !mStartPlaying
        }
        // Record to the external cache directory for visibility
//        filePath = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

    }

    private fun takeImage() {
//        lifecycleScope.launchWhenStarted {

        val cameraFile = createFile( getOutputDirectory())
        val photoURI =
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID , cameraFile)
//            getTmpFileUri().let { uri ->
//                latestTmpUri = uri
        takeImageResult.launch(photoURI)
//            }
//        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

//    fun getOutputDirectory(): File {
//
//        val mediaDir = externalMediaDirs.firstOrNull()?.let {
//            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
//        }
//        return if (mediaDir != null && mediaDir.exists())
//            mediaDir else filesDir
//    }


    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(this@MainActivity.localClassName, "Permission to record denied")
            requestPermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        } else {
            record.isEnabled = true
            println("Permission Granted....")
        }
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        Log.e("filesBName", "${filePath.absolutePath} ")
        player = MediaPlayer().apply {
            try {
                setDataSource(filePath.absolutePath)

                prepare()
                start()
            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun saveBitmap(bitmap: Bitmap, callback: ((String) -> Unit)? = null) {
//        try {
//            val file = createImageFile(applicationContext)
//            val oStream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 18, oStream)
//            oStream.flush()
//            oStream.close()
//            callback?.invoke(file.absolutePath)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun showImagesDialog() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.type = "image/*"

        pickIntent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("image/png", "image/jpeg")
        )

        pickIntent.action = Intent.ACTION_GET_CONTENT


//        val pickIntent = Intent(
//            Intent.ACTION_PICK,
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        )
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,"image/*" )
        val chooserIntent = Intent.createChooser(pickIntent, "Choose")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(takePhotoIntent))
        if (SDK_INT > Build.VERSION_CODES.Q) {
            activityForResult.launch(pickIntent)
        } else {
            //support for older than android 11
            startActivityForResult(chooserIntent, 111)
        }

    }

    //    private fun createImageFile(activity: Context):File{
////        return File()
//    }
    private fun startRecording() {
        Log.e("MainActivity", "file name ${filePath.absolutePath}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = MediaRecorder(this)
        } else {
            recorder = MediaRecorder()
        }
        Log.e("uri", "uri ${Uri.fromFile(filePath)}")
        recorder.apply {

            this?.setAudioSource(MediaRecorder.AudioSource.MIC)
            this?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            this?.setOutputFile(filePath.absolutePath)
            this?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                this?.prepare()
                this?.start()
            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
            }
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }


    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }
}