package com.github.lostfound.util

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

interface OnLoadMoreListener {
    fun onLoadMore()
}

@Suppress("unused")
open class RecyclerViewLoadMoreScrollListener : RecyclerView.OnScrollListener {
    private var visibleThreshold = 5
    private var mOnLoadMoreListener: OnLoadMoreListener? = null
    private var isLoading: Boolean = false
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var mLayoutManager: RecyclerView.LayoutManager

    constructor(layoutManager: LinearLayoutManager) {
        this.mLayoutManager = layoutManager
    }

    constructor(layoutManager: GridLayoutManager) {
        this.mLayoutManager = layoutManager
        visibleThreshold *= layoutManager.spanCount
    }

    constructor(layoutManager: StaggeredGridLayoutManager) {
        this.mLayoutManager = layoutManager
        visibleThreshold *= layoutManager.spanCount
    }

    fun setLoaded() {
        isLoading = false
    }

    fun getLoaded(): Boolean {
        return isLoading
    }

    fun setThreshold(threshold: Int) {
        visibleThreshold = threshold
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener?) {
        mOnLoadMoreListener = onLoadMoreListener
    }

    fun setOnLoadMoreListener(action: () -> Unit) {
        mOnLoadMoreListener = object : OnLoadMoreListener {
            override fun onLoadMore() {
                action()
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (dy <= 0) return

        totalItemCount = mLayoutManager.itemCount

        lastVisibleItem = when (mLayoutManager) {
            is StaggeredGridLayoutManager -> {
                val lastVisibleItemPositions =
                    (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
                // get maximum element within the list
                lastVisibleItemPositions.maxOrNull() ?: 0
            }
            is GridLayoutManager -> (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
            is LinearLayoutManager -> (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            else -> return
        }

        if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
            mOnLoadMoreListener?.onLoadMore()
            isLoading = true
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class EmptySupportRecyclerView : RecyclerView {

    var emptyView: View? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onChanged() {
            checkEmptyView()
        }
    }

    fun checkEmptyView() {
        if (adapter != null && emptyView != null) {
            setIsEmpty(adapter!!.itemCount == 0)
        }
    }

    fun setIsEmpty(value: Boolean) {
        if (value) {
            emptyView?.visibility = View.VISIBLE
            visibility = View.GONE
        } else {
            emptyView?.visibility = View.GONE
            visibility = View.VISIBLE
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }
}

fun TextView.showStrikeThrough(show: Boolean) {
    paintFlags =
        if (show) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

