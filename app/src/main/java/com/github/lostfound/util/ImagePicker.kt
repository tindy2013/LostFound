package com.github.lostfound.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.github.lostfound.BuildConfig
import java.io.File
import java.util.*

private var lastCreatedFile: File? = null
private fun getTempImage(context: Context): File {
    val tempFile = File(context.cacheDir, "capture_${Date().toSimpleString()}.jpg")
    tempFile.parentFile?.mkdirs()
    lastCreatedFile = tempFile
    return tempFile
}

private fun getExternalUri(context: Context, file: File) =
    FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)

fun getPickImageIntent(context: Context): Intent? {
    var chooserIntent: Intent? = null
    var intentList = ArrayList<Intent>()
    val pickIntent = Intent(Intent.ACTION_PICK).setType("image/*")
    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        .putExtra("return-data", true)
        .putExtra(MediaStore.EXTRA_OUTPUT, getExternalUri(context, getTempImage(context)))
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intentList = addIntentsToList(context, intentList, pickIntent)
    intentList = addIntentsToList(context, intentList, takePhotoIntent)
    if (intentList.size > 0) {
        chooserIntent = Intent.createChooser(
            intentList.removeAt(intentList.size - 1),
            "Select image or capture"
        )
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            intentList.toTypedArray())
    }
    return chooserIntent
}

private fun addIntentsToList(context: Context, list: ArrayList<Intent>, intent: Intent): ArrayList<Intent> {
    val resInfo = context.packageManager.queryIntentActivities(intent, 0)
    for (resolveInfo in resInfo) {
        val packageName = resolveInfo.activityInfo.packageName
        val targetedIntent = Intent(intent)
        targetedIntent.setPackage(packageName)
        list.add(targetedIntent)
    }
    return list
}


fun getImageFromResult(resultCode: Int, imageReturnedIntent: Intent?): Uri? {
    val imageFile = lastCreatedFile
    if (resultCode == Activity.RESULT_OK) {
        val isCamera = imageReturnedIntent == null || imageReturnedIntent.data == null ||
                imageReturnedIntent.data.toString().contains(imageFile.toString())
        return if (isCamera) {
            /** CAMERA  */
            lastCreatedFile = null
            Uri.fromFile(imageFile)
        } else {
            /** ALBUM  */
            imageReturnedIntent!!.data
        }
    }
    return null
}