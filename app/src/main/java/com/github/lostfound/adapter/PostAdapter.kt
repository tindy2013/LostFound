package com.github.lostfound.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.github.lostfound.R
import com.github.lostfound.databinding.AdapterItemBinding
import com.github.lostfound.entity.Post
import com.github.lostfound.entity.PostType
import com.github.lostfound.util.showStrikeThrough
import com.github.lostfound.util.toFormattedTime

class PostAdapter(
    private val context: Context,
    var posts: ArrayList<Post> = arrayListOf(),
    var onPostClickListener: (Post) -> Unit = { _ -> },
    var imageLoader: (ImageView, String) -> Boolean = { _, _ -> false },
    var imageCleaner: (ImageView) -> Unit = {}
): RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var hasLoadedImage = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = posts[position]
        holder.binding.apply {
            tvName.text = item.name
            tvDesc.text = item.description
            tvTime.text = item.time.toFormattedTime()
            hasLoadedImage = imageLoader(imgIcon, item.image)
            when (item.type) {
                PostType.TYPE_LOST -> {
                    tvType.text = context.getString(R.string.lost)
                    tvType.setTextColor(context.getColor(R.color.icon_red))
                }
                PostType.TYPE_FOUND -> {
                    tvType.text = context.getString(R.string.found)
                    tvType.setTextColor(context.getColor(R.color.icon_green))
                }
            }
            tvName.showStrikeThrough(item.resolved)
            tvDesc.showStrikeThrough(item.resolved)
            viewShadow.visibility = if (item.resolved) View.VISIBLE else View.GONE
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        if (hasLoadedImage)
            imageCleaner(holder.binding.imgIcon)
    }

    override fun getItemCount() = posts.size

    inner class ViewHolder(val binding: AdapterItemBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onPostClickListener(posts[adapterPosition]) }
        }
    }
}