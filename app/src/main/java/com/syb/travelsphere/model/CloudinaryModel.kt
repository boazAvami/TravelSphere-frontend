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
import android.graphics.drawable.Drawable
import android.util.Log
import com.cloudinary.android.policy.UploadPolicy
import com.squareup.picasso.Picasso
import com.syb.travelsphere.BuildConfig
import com.syb.travelsphere.base.BitmapCallback
import com.syb.travelsphere.base.BooleanCallback
import com.syb.travelsphere.base.EmptyCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                    callback(error?.description ?: "Unknown error")
                    Log.e(TAG, "Upload rescheduled for request: $requestId due to ${error?.description}")
                }
            })
            .dispatch()
    }

    fun deleteImage(imageUrl: String, callback: BooleanCallback) {
        val publicId = extractPublicId(imageUrl) ?: return callback(false)

        // Run the deletion on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = MediaManager.get().cloudinary.uploader().destroy(publicId, emptyMap<String, Any>())
                val isSuccess = result["result"] == "ok"

                // Switch back to the main thread to invoke the callback
                CoroutineScope(Dispatchers.Main).launch {
                    callback(isSuccess)
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    callback(false)
                }
            }
        }
    }

    private fun extractPublicId(imageUrl: String): String {
        return imageUrl.substringAfter("upload/").substringBeforeLast(".")
    }

    private fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }

    fun getImageByUrl(imageUrl: String, callback: BitmapCallback) {
        if (imageUrl.isNullOrEmpty()) {
        } else {
            Picasso.get()
                .load(imageUrl)
                .config(Bitmap.Config.ARGB_8888) // Ensures high quality
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        callback(bitmap) // Successfully loaded
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        callback(null) // Failed to load
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        }

    }

    companion object {
        private const val TAG = "CloudinaryModel"
    }
}