package com.syb.travelsphere.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R

class PhotosGridAdapter(
    private val photos: MutableList<String>,
    private val onDeletePhoto: (Int) -> Unit
) : RecyclerView.Adapter<PhotosGridAdapter.PhotoViewHolder>() {

    // ViewHolder to represent each photo item
    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: ImageView = itemView.findViewById(R.id.photo_image)
        val deleteIcon: ImageView = itemView.findViewById(R.id.delete_icon)
    }

    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_grid, parent, false)
        return PhotoViewHolder(view)
    }

    // Binds the data to the ViewHolder at a given position.
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoData = photos[position]

        val bitmap = decodeImage(photoData)
        if (bitmap != null) {
            holder.photoImageView.setImageBitmap(bitmap)
        } else {
            holder.photoImageView.setImageResource(R.drawable.placeholder_image)
        }

        // Handle photo delete action
        holder.deleteIcon.setOnClickListener {
            onDeletePhoto(position)
        }
    }

    // Returns the total number of items in the data set.
    override fun getItemCount(): Int = photos.size

    // Decodes Base64 or file path to a Bitmap.
    private fun decodeImage(photoData: String): Bitmap? {
        return try {
            if (photoData.startsWith("data:image")) {
                val base64Data = photoData.substringAfter(",")
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                BitmapFactory.decodeFile(photoData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
