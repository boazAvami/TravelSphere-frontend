package com.syb.travelsphere.components

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.databinding.ItemPhotoGridBinding

class PhotoGridViewHolder(
    binding: ItemPhotoGridBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var photoImageView: ImageView? = null
    private var deleteIcon: ImageView? = null

    init {
        photoImageView = binding.photoImage
        deleteIcon = binding.deleteIcon
    }

    fun bind(bitmap: Bitmap, onDeleteClick: (Int) -> Unit) {
        photoImageView?.setImageBitmap(bitmap)

        deleteIcon?.setOnClickListener {
            onDeleteClick(adapterPosition)
        }
    }
}