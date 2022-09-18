package com.github.lostfound.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import java.io.InputStream

interface GlideCallbackListener {
    fun onLoadFailed(exception: GlideException?, isFirstResource: Boolean): Boolean {
        return false
    }
    fun onResourceReady(dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        return false
    }
}

@SuppressLint("CheckResult")
fun imageDisplay(context: Context, url: String?, view: ImageView, placeholder: Int? = null, referer: String? = null, transformation: Transformation<Bitmap>? = null, useCache: Boolean = true, cookieJar: CookieJar? = null, listener: GlideCallbackListener = object : GlideCallbackListener {}) {
    if ((context is Activity) && (context.isDestroyed || context.isFinishing)) { // don't load image to destroyed activity
        return
    }

    if (url == null || url.isEmpty()) {
        Glide.with(context)
            .load(placeholder)
            .into(view)
        return
    }

    val cache = if (useCache) DiskCacheStrategy.ALL else DiskCacheStrategy.NONE
    val glideUrl = GlideUrl(url, LazyHeaders.Builder().apply { if (referer != null) addHeader("Referer", referer) }.build())

    if (cookieJar != null) {
        val mOkHttpClient = OkHttpClient.Builder().cookieJar(cookieJar).build()
        Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(mOkHttpClient))
    }

    Glide.with(context)
        .load(glideUrl)
        .skipMemoryCache(!useCache)
        .diskCacheStrategy(cache)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                return listener.onLoadFailed(e, isFirstResource)
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                return listener.onResourceReady(dataSource, isFirstResource)
            }
        })
        .apply { if (placeholder != null) placeholder(placeholder) }
        .apply { if (transformation != null) apply(RequestOptions.bitmapTransform(transformation)) }
        .into(view)
}

fun imageClear(context: Context, view: ImageView) {
    if ((context is Activity) && (context.isDestroyed || context.isFinishing)) { // don't start load in destroyed activity
        return
    }
    Glide.with(context).clear(view)
}