package com.example.giphy_api_chili_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GifAdapter(private val gifUrls: MutableList<String>) : RecyclerView.Adapter<GifAdapter.GifViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
        // Inflate the item layout and create a ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gif, parent, false)
        return GifViewHolder(view)
    }

    override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
        // Bind the GIF URL to the ImageView using Glide
        val gifUrl = gifUrls[position]
        Glide.with(holder.itemView.context)
            .asGif()
            .load(gifUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.gifImageView)
    }


    fun addGifs(newGifUrls: List<String>) {
        val startPosition = gifUrls.size
        gifUrls.addAll(newGifUrls)
        notifyItemRangeInserted(startPosition, newGifUrls.size)
    }

    override fun getItemCount(): Int {
        return gifUrls.size
    }

    fun clearGifs() {
        gifUrls.clear()
        notifyDataSetChanged()
    }

    inner class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gifImageView: ImageView = itemView.findViewById(R.id.gifImageView)
    }
}