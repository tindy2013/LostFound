package com.github.lostfound

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.lostfound.adapter.PostAdapter
import com.github.lostfound.databinding.ActivitySimpleListBinding
import com.github.lostfound.util.RecyclerViewLoadMoreScrollListener
import com.github.lostfound.util.imageClear
import com.github.lostfound.util.imageDisplay
import kotlinx.coroutines.launch

class MySubmitsActivity: BaseActivity() {
    private lateinit var binding: ActivitySimpleListBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var listener: RecyclerViewLoadMoreScrollListener
    private var currentPage = 0

    private var loading = false
    private var loadedAll = false

    private fun setLoading(loading: Boolean) {
        this.loading = loading
        binding.swipe.isRefreshing = loading
        if (!loading)
            listener.setLoaded()
    }

    private suspend fun loadPosts() {
        setLoading(true)
        launchIOToastException({
            app.loggedIn.block()
            if (!loadedAll) {
                val call = app.postService.listPosts(self = true, page = ++currentPage, count = POSTS_PER_PAGE)
                val result = call.getOrThrow()
                val newCount = result.data.size
                if (postAdapter.posts.size + newCount == result.count)
                    loadedAll = true
                runOnUiThread {
                    postAdapter.posts.addAll(result.data)
                    postAdapter.notifyItemRangeInserted(postAdapter.posts.size - newCount, newCount)
                }
            }
        }, {
            runOnUiThread { setLoading(false) }
        })
    }
    private fun refresh() {
        if (loading) return
        loadedAll = false
        postAdapter.notifyItemRangeRemoved(0, postAdapter.posts.size)
        postAdapter.posts.clear()
        currentPage = 0
        lifecycleScope.launch {
            loadPosts()
        }
    }

    override fun onResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data?.getBooleanExtra("updated", false) == true)
            refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = getString(R.string.my_submits)
        binding.btnBack.setOnClickListener { onBackPressedSupport() }

        postAdapter = PostAdapter(
            context = this,
            onPostClickListener = { post ->
                activityLauncher.launch(
                    DetailActivity::class.intent
                        .putExtra("post", post)
                        .putExtra("isOwner", post.owner == app.loggedInUser?.id)
                )
            },
            imageLoader = { img, key ->
                val url = app.getFileUrl(key)
                lifecycleScope.launch {
                    imageDisplay(this@MySubmitsActivity, url, img)
                }
                true
            },
            imageCleaner = {
                imageClear(this@MySubmitsActivity, it)
            }
        )
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        listener = RecyclerViewLoadMoreScrollListener(layoutManager).apply {
            setThreshold((POSTS_PER_PAGE * 0.3).toInt())
            setOnLoadMoreListener {
                lifecycleScope.launch { if (!loadedAll) loadPosts() }
            }
        }
        binding.rvContent.apply {
            adapter = postAdapter
            layoutManager = this@MySubmitsActivity.layoutManager
            addOnScrollListener(listener)
        }

        lifecycleScope.launch {
            if (!loadedAll)
                loadPosts()
        }

        binding.swipe.setOnRefreshListener {
            refresh()
        }
    }

    companion object {
        private const val POSTS_PER_PAGE = 15
    }
}