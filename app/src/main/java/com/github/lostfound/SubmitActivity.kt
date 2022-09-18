package com.github.lostfound

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.github.lostfound.databinding.ActivitySubmitBinding
import com.github.lostfound.entity.Post
import com.github.lostfound.entity.PostType
import com.github.lostfound.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.io.FileNotFoundException


class SubmitActivity: BaseActivity() {
    private lateinit var binding: ActivitySubmitBinding
    private var selectedImageKey = ""
    private var post: Post? = null

    private fun putUpdatedResult(post: Post?) {
        runOnUiThread { showToast(if (post == null) R.string.submit_success else R.string.update_info_success, Toast.LENGTH_SHORT) }
        setResult(RESULT_OK, Intent().putExtra("updated", true).putExtra("post", post))
    }

    private fun setUploadButtonImage(uploaded: Boolean) {
        binding.btnUpload.setImageResource(
            if (uploaded) R.drawable.ic_baseline_close_24 else R.drawable.ic_baseline_add_24
        )
    }

    private suspend fun uploadImage(selectedImage: Uri, name: String) {
        launchIOToastException {
            val body = MultipartBody.Builder()
                .addFormDataPart("file", name, ContentUriRequestBody(contentResolver, selectedImage))
                .build()
            val call = app.fileService.upload(body)
            val result = call.getOrThrow().checkException()
            if (result.code == 0) {
                selectedImageKey = result.data ?: ""
                runOnUiThread {
                    binding.imageSelected.setImageURI(selectedImage)
                    setUploadButtonImage(true)
                }
            }
        }
    }

    private suspend fun submitForm() {
        withContext(Dispatchers.IO) {
            launchToastException {
                val name = binding.etName.text?.toString() ?: return@launchToastException
                val desc = binding.etDesc.text?.toString() ?: return@launchToastException
                val type = if (binding.toggleLostFound.isChecked) 1 else 0
                if (post != null) {
                    val call = app.postService.updatePost(post!!.id, name, desc, type, selectedImageKey)
                    val result = call.getOrThrow().checkException()
                    if (result.code == 0) {
                        putUpdatedResult(result.data)
                        finish()
                    }
                } else {
                    val call = app.postService.createPost(name, desc, type, selectedImageKey)
                    val result = call.getOrThrow().checkException()
                    if (result.code == 0) {
                        putUpdatedResult(null)
                        finish()
                    }
                }
            }
        }
    }

    private fun checkSelectedImage(): Boolean {
        return if (selectedImageKey.isBlank()) {
            true
        } else {
            binding.imageSelected.setImageDrawable(null)
            setUploadButtonImage(false)
            selectedImageKey = ""
            false
        }
    }

    override fun onResult(resultCode: Int, data: Intent?) {
        //val selectedImage = data?.data ?: return
        val selectedImage = getImageFromResult(resultCode, data) ?: return
        val filePath = selectedImage.path ?: "/"
        val name = filePath.substringAfterLast("/")
        val ext = if (filePath.contains('.')) filePath.substringAfterLast('.') else "jpg"
        try {
            when (ext) {
                "jpg", "jpeg", "gif", "png" -> lifecycleScope.launch { uploadImage(selectedImage, name) }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun updateViewFromPost(post: Post) {
        binding.etName.setText(post.name)
        binding.etDesc.setText(post.description)
        binding.toggleLostFound.isChecked = (post.type == PostType.TYPE_FOUND)
        selectedImageKey = post.image
        if (post.image != "null" && post.image.isNotBlank()) {
            val url = app.getFileUrl(post.image)
            imageDisplay(this, url, binding.imageSelected)
            setUploadButtonImage(true)
        } else {
            setUploadButtonImage(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        post = intent.getParcelableExtra("post")
        if (post != null) {
            binding.tvTitle.text = getString(R.string.update_info)
            updateViewFromPost(post!!)
        }

        binding.btnBack.setOnClickListener { onBackPressedSupport() }

        binding.btnUpload.setOnClickListener {
            if (checkSelectedImage())
                //activityLauncher.launch(Intent(Intent.ACTION_PICK).setType("image/*"))
                activityLauncher.launch(getPickImageIntent(this))
        }

        binding.btnReset.setOnClickListener {
            checkSelectedImage()
            if (post != null) {
                updateViewFromPost(post!!)
            } else {
                binding.etName.text?.clear()
                binding.etDesc.text?.clear()
                binding.toggleLostFound.isChecked = false
            }
        }

        val changedAction = { _: CharSequence?, _: Int, _: Int, _ : Int ->
            binding.btnFinish.isEnabled =
                (binding.etName.text!!.isNotBlank() && binding.etDesc.text!!.isNotBlank())
        }
        if (post == null)
            binding.btnFinish.isEnabled = false
        binding.etName.doOnTextChanged(changedAction)
        binding.etDesc.doOnTextChanged(changedAction)

        binding.btnFinish.setOnClickListener {
            if (selectedImageKey.isBlank()) {
                askDialog(
                    iconId = R.drawable.ic_baseline_warning_24,
                    titleId = R.string.no_image,
                    messageId = R.string.no_image_confirm,
                    onConfirm = { _, _ ->
                        lifecycleScope.launch { submitForm() }
                    }
                )
            } else {
                lifecycleScope.launch { submitForm() }
            }
        }
    }

    override fun onDestroy() {
        imageClear(this, binding.imageSelected)
        super.onDestroy()
    }
}