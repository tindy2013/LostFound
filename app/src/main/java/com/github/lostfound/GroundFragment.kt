package com.github.lostfound

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.lostfound.adapter.PostAdapter
import com.github.lostfound.databinding.FragmentGroundBinding
import com.github.lostfound.entity.PostType
import com.github.lostfound.util.RecyclerViewLoadMoreScrollListener
import com.github.lostfound.util.imageClear
import com.github.lostfound.util.imageDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroundFragment: BaseFragment() {
    private lateinit var binding: FragmentGroundBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var listener: RecyclerViewLoadMoreScrollListener
    private var filterType: PostType? = null
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
        withContext(Dispatchers.IO) {
            launchToastException({
                app.loggedIn.block()
                if (!loadedAll) {
                    val call = app.postService.listPosts(type = filterType?.ordinal, page = ++currentPage, count = POSTS_PER_PAGE)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroundBinding.inflate(inflater, container, false)

        postAdapter = PostAdapter(
            context = requireActivity(),
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
                    imageDisplay(requireActivity(), url, img)
                }
                true
            },
            imageCleaner = {
                imageClear(requireActivity(), it)
            }
        )
        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        listener = RecyclerViewLoadMoreScrollListener(layoutManager).apply {
            setThreshold((POSTS_PER_PAGE * 0.3).toInt())
            setOnLoadMoreListener {
                lifecycleScope.launch { if (!loadedAll) loadPosts() }
            }
        }
        binding.rvContent.apply {
            adapter = postAdapter
            layoutManager = this@GroundFragment.layoutManager
            addOnScrollListener(listener)
        }

        lifecycleScope.launch {
            if (!loadedAll)
                loadPosts()
        }

        binding.swipe.setOnRefreshListener {
            refresh()
        }

        binding.fabSubmit.setOnClickListener {
            if (app.loggedInUser!!.name.isBlank() || app.loggedInUser!!.number.isBlank()) {
                askDialog(
                    iconId = R.drawable.ic_baseline_warning_24,
                    titleId = R.string.cannot_submit,
                    messageId = R.string.cannot_submit_desc,
                    neutral = true
                )
            } else {
                activityLauncher.launch(SubmitActivity::class.intent)
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                filterType = when (id) {
                    1L -> PostType.TYPE_LOST
                    2L -> PostType.TYPE_FOUND
                    else -> null
                }
                refresh()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                filterType = null
            }
        }

        return binding.root
    }

    companion object {
        private const val POSTS_PER_PAGE = 15
    }
}