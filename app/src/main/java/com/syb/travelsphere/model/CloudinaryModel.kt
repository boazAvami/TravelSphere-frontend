package com.syb.travelsphere.model

import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.syb.travelsphere.base.ImageCallback
import com.syb.travelsphere.base.MyApplication
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import com.cloudinary.android.policy.UploadPolicy
import com.syb.travelsphere.BuildConfig
import java.lang.Error

class CloudinaryModel {
    init {
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )

        MyApplication.Globals.context?.let{
            MediaManager.init(it, config)
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.Builder()
                .maxConcurrentRequests(3)
                .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                .build()
        }
    }

    fun uploadImage(
        bitmap: Bitmap,
        callback: ImageCallback) {
        val context = MyApplication.Globals.context ?: return
        val file: File = bitmapToFile(bitmap, context)

        MediaManager.get().upload(file.path)
            .option("folder", "images")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Called when upload starts
                }
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Called during upload progress
                }
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val publicUrl = resultData["secure_url"] as? String ?: ""
                    callback(publicUrl) // Return the URL of the uploaded image
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    callback(error?.description ?: "Unknown error")
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    TODO("Not yet implemented")
                }
            })
            .dispatch()
    }

    fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }
}