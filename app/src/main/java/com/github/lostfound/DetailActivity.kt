package com.github.lostfound

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.lifecycleScope
import com.github.lostfound.databinding.ActivityDetailBinding
import com.github.lostfound.entity.Post
import com.github.lostfound.entity.PostType
import com.github.lostfound.util.imageClear
import com.github.lostfound.util.imageDisplay
import com.github.lostfound.util.toFormattedTime
import kotlinx.coroutines.launch

class DetailActivity: BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var post: Post

    private fun updateViewWithPost() {
        binding.apply {
            tvTitle.text = getString(R.string.detail)
            tvName.text = post.name
            tvDesc.text = post.description
            tvTime.text = post.time.toFormattedTime()
            when (post.type) {
                PostType.TYPE_LOST -> {
                    tvType.text = getString(R.string.lost)
                    tvType.setTextColor(getColor(R.color.icon_red))
                }
                PostType.TYPE_FOUND -> {
                    tvType.text = getString(R.string.found)
                    tvType.setTextColor(getColor(R.color.icon_green))
                }
            }
            if (post.resolved) {
                btnResolved.visibility = View.GONE
                btnUnresolved.visibility = View.VISIBLE
            } else {
                btnResolved.visibility = View.VISIBLE
                btnUnresolved.visibility = View.GONE
            }
            val url = app.getFileUrl(post.image)
            imageDisplay(this@DetailActivity, url, image)
        }
    }

    private fun putUpdatedResult() {
        setResult(RESULT_OK, Intent().putExtra("updated", true))
    }

    private fun setPostResolve(resolved: Boolean) {
        lifecycleScope.launch {
            launchIOToastException {
                val call = app.postService.resolvePost(post.id, resolved)
                val result = call.getOrThrow().checkException()
                if (result.code == 0) {
                    post = result.data!!
                    putUpdatedResult()
                    runOnUiThread { updateViewWithPost() }
                }
            }
        }
    }

    override fun onResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            post = data?.getParcelableExtra("post") ?: return
            putUpdatedResult()
            updateViewWithPost()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressedSupport() }

        post = intent.getParcelableExtra("post") ?: return finish()
        val isOwner = intent.getBooleanExtra("isOwner", false)

        updateViewWithPost()

        binding.apply {
            if (post.contactName.isBlank())
                post.contactName = getString(R.string.not_set)
            if (post.contactNumber.isBlank())
                post.contactNumber = getString(R.string.not_set)
            tvContact.text = getString(R.string.detail_contact_info, post.contactName, post.contactNumber)
            if (!post.contactNumber.isDigitsOnly()) {
                btnCall.isEnabled = false
            } else {
                btnCall.setOnClickListener {
                    startActivity(
                        Intent(Intent.ACTION_DIAL)
                            .setData(Uri.parse("tel:${post.contactNumber}"))
                    )
                }
            }
            layoutOwner.visibility = if (isOwner) {
                btnUpdate.setOnClickListener {
                    activityLauncher.launch(
                        SubmitActivity::class.intent
                            .putExtra("post", post)
                    )
                }
                btnResolved.setOnClickListener {
                    askDialog(
                        titleId = R.string.mark_as_resolved,
                        messageId = R.string.mark_as_resolved_sure,
                        onConfirm = { _, _ -> setPostResolve(true) }
                    )
                }
                btnUnresolved.setOnClickListener {
                    askDialog(
                        titleId = R.string.mark_as_unresolved,
                        messageId = R.string.mark_as_unresolved_sure,
                        onConfirm = { _, _ -> setPostResolve(false) }
                    )
                }
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onDestroy() {
        imageClear(this, binding.image)
        super.onDestroy()
    }
}