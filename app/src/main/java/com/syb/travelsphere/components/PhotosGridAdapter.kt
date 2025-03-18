package com.syb.travelsphere.components

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.databinding.ItemPhotoGridBinding

class PhotosGridAdapter(
    private val photos: MutableList<Bitmap>,
    private val onDeletePhoto: (Int) -> Unit
) : RecyclerView.Adapter<PhotoGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoGridViewHolder {
        val binding = ItemPhotoGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoGridViewHolder, position: Int) {
        val bitmap = photos[position]
        holder.bind(bitmap) { pos ->
            onDeletePhoto(pos)
            removePhoto(pos)
        }
    }

    override fun getItemCount(): Int = photos.size

    // Add a new photo to the list
    fun addPhoto(newPhoto: Bitmap) {
        photos.add(newPhoto)
        notifyItemInserted(photos.size - 1)
    }

    // Remove a photo and update the RecyclerView efficiently
    private fun removePhoto(position: Int) {
        if (position in photos.indices) {
            photos.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}