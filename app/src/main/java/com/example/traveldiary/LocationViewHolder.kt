package com.example.traveldiary

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.util.Base64
import android.view.animation.Animation
import android.view.animation.AnimationUtils


class LocationViewHolder(itemView: View,private val listener: onLocationCLickListener,private val adapter: LocationAdapter) : RecyclerView.ViewHolder(itemView) {
    private val imgView : ImageView = itemView.findViewById(R.id.thumbnailImageView)

    init {
        itemView.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onLocationClick(adapter.items[position])
            }
        }
    }
    fun animation(view: View) {
        val animation = AnimationUtils.loadAnimation(view.context, android.R.anim.slide_in_left)
        animation.duration = 450 // Adjust duration as needed
        view.startAnimation(animation)
    }

    fun bind(item: Locations) {
        itemView.findViewById<TextView>(R.id.locationNameTextView).text = item.name
        itemView.findViewById<TextView>(R.id.locationAddressTextView).text = "Address : ${item.address}"
        itemView.findViewById<TextView>(R.id.locationDateTextView).text = "Date : ${item.date}"
        itemView.findViewById<TextView>(R.id.locationNotesTextView).text ="Notes : ${item.notes}"
        if (item.imageUrl.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(item.imageUrl) // Load directly from Base64 string
                .into(imgView)
        } else {
            imgView.setImageResource(R.mipmap.logo)
        }
    }

}
