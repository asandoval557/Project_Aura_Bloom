package com.example.project_aura_bloom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

private var selectedPhotoResId: Int? = null

class PhotoPickerAdaptor(
    private val photoResIds: List<Int>,
    private val onPhotoSelected: (Int) -> Unit
) : RecyclerView.Adapter<PhotoPickerAdaptor.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoResId = photoResIds[position]
        holder.photoView.setImageResource(photoResId)

        holder.itemView.setOnClickListener {
            selectedPhotoResId = photoResId
            onPhotoSelected(photoResId)
        }
    }

    override fun getItemCount(): Int = photoResIds.size

    fun getSelectedPhoto(): Int? {
        return selectedPhotoResId
    }

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photoView: ImageView = view.findViewById(R.id.photo_view)
    }
}