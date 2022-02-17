package com.example.storagemultisearch.recyclerview

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storagemultisearch.R

class IconRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<IconRecyclerViewAdapter.ViewHolder>() {
    private val iconItems by lazy { ArrayList<IconRecyclerViewItem>() }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_icon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = iconItems[position]
        Glide.with(context)
            .load(item.uri)
            .into(holder.imageView)
    }

    override fun getItemCount() = iconItems.size

    fun addItem(uri: Uri) {
        iconItems.add(IconRecyclerViewItem(uri))
        notifyItemInserted(iconItems.size-1)
    }

    fun clearItem() {
        iconItems.clear()
        notifyDataSetChanged()
    }
}