package com.example.chatapplication

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun File.compress(): File {
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = false
        var bm: Bitmap = BitmapFactory.decodeFile(this.absolutePath, bmOptions)
        try {
            bm = ImageUtils.modifyOrientation(bm, this.absolutePath)
            val out = FileOutputStream(this)
            bm.compress(Bitmap.CompressFormat.JPEG,  80, out)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return this
    }

    fun Context.createFile(outputDirectory: File): File {
        val randomFileName: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
//        val storageDir =   this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val image = File.createTempFile(
            randomFileName,
            ".jpg",
            outputDirectory
        )
        return image
    }

fun ContextWrapper.getOutputDirectory(): File {

    val mediaDir = externalMediaDirs.firstOrNull()?.let {
        File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else filesDir
}