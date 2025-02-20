package com.example.traveldiary

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView

interface onLocationCLickListener {
    fun onLocationClick(locations: Locations)
}

class LocationAdapter(private val listener: onLocationCLickListener) : RecyclerView.Adapter<LocationViewHolder>() {
    var items: MutableList<Locations> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent, false)
        return LocationViewHolder(view,listener, this) // Pass the adapter instance
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int =items.size

    fun submitList(newItems: List<Locations>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }


    fun updateData(newItems: List<Locations>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}