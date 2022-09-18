package com.github.lostfound.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.lostfound.databinding.AdapterAppBinding
import com.github.lostfound.entity.App
import com.github.lostfound.util.imageClear
import com.github.lostfound.util.imageDisplay

class AppAdapter(private val context: Context, var apps: ArrayList<App>, var onAppClickListener: (App) -> Unit) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterAppBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        if (app.id == -1) {
            holder.binding.apply {
                ivIcon.visibility = View.GONE
                tvTitle.visibility = View.GONE
                tvTitleAlt.visibility = View.GONE
                viewArrow.visibility = View.GONE
                root.setBackgroundColor(Color.TRANSPARENT)
                root.isClickable = false
            }
        } else {
            if (app.icon != -1) {
                holder.binding.apply {
                    ivIcon.setImageResource(app.icon)
                    tvTitle.visibility = View.VISIBLE
                    tvTitleAlt.visibility = View.GONE
                    tvTitle.text = app.name
                    if (app.iconTint != -1)
                        ivIcon.setColorFilter(app.iconTint)
                    if (app.nameTint != -1)
                        tvTitle.setTextColor(app.nameTint)
                }

            } else {
                holder.binding.apply {
                    tvTitle.visibility = View.GONE
                    tvTitleAlt.visibility = View.VISIBLE
                    tvTitleAlt.text = app.name
                    if (app.nameTint != -1)
                        tvTitleAlt.setTextColor(app.nameTint)
                }
            }
            if (app.iconRight != null) {
                holder.binding.cardIcon.visibility = View.VISIBLE
                imageDisplay(context, app.iconRight, holder.binding.ivIconRight)
            }
            if (app.textRight != null) {
                holder.binding.tvTitleRight.apply {
                    visibility = View.VISIBLE
                    text = app.textRight
                }
            }
            holder.itemView.isClickable = true
        }
    }

    override fun onViewRecycled(holder: ViewHolder) = imageClear(context, holder.binding.ivIconRight)

    override fun getItemCount() = apps.size

    inner class ViewHolder(val binding: AdapterAppBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener { onAppClickListener(apps[adapterPosition]) }
        }
    }
}