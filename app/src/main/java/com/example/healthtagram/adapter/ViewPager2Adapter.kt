package com.example.healthtagram.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.healthtagram.R
import com.example.healthtagram.activity.MainActivity.context
import java.util.ArrayList

class ViewPager2Adapter (private val context: Context, private val images:ArrayList<String>): RecyclerView.Adapter<ViewPager2Adapter.viewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder =
            viewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_page_detail, parent, false))

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        Glide.with(context).load(Uri.parse(images.get(position))).into(holder.imageView);
    }

    class viewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.detail_view_item_image)
    }
}