package com.github.lostfound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.lostfound.adapter.PostAdapter
import com.github.lostfound.databinding.FragmentSearchBinding
import com.github.lostfound.util.RecyclerViewLoadMoreScrollListener
import com.github.lostfound.util.imageClear
import com.github.lostfound.util.imageDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment: BaseFragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var listener: RecyclerViewLoadMoreScrollListener
    private var keyword = ""
    private var currentPage = 0

    private var loading = false
    private var loadedAll = false

    private fun setLoading(loading: Boolean) {
        this.loading = loading
        binding.swipe.isRefreshing = loading
        if (!loading)
            listener.setLoaded()
    }

    private fun clearContent() {
        postAdapter.notifyItemRangeRemoved(0, postAdapter.posts.size)
        postAdapter.posts.clear()
        binding.rvContent.setIsEmpty(true)
        binding.swipe.isEnabled = false
    }

    private suspend fun loadPosts() {
        setLoading(true)
        withContext(Dispatchers.IO) {
            launchToastException({
                app.loggedIn.block()
                if (!loadedAll) {
                    val call = app.postService.listPosts(keyword = keyword, page = ++currentPage, count = POSTS_PER_PAGE)
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
                runOnUiThread {
                    binding.swipe.isEnabled = true
                    setLoading(false)
                }
            })
        }
    }

    private fun refresh() {
        if (loading || keyword.isBlank()) {
            binding.swipe.isEnabled = false
            return
        }
        loadedAll = false
        clearContent()
        currentPage = 0
        lifecycleScope.launch {
            loadPosts()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        postAdapter = PostAdapter(
            context = requireActivity(),
            onPostClickListener = { post ->
                startActivity(
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
            layoutManager = this@SearchFragment.layoutManager
            addOnScrollListener(listener)
        }

        binding.swipe.setOnRefreshListener {
            refresh()
        }

        binding.rvContent.emptyView = binding.tvEmpty
        binding.editKeyword.setOnEditorActionListener { tv, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                keyword = tv.text.toString()
                if (keyword.isNotBlank())
                    refresh()
                else
                    clearContent()
                return@setOnEditorActionListener true
            }
            false
        }

        return binding.root
    }

    companion object {
        private const val POSTS_PER_PAGE = 15
    }
}