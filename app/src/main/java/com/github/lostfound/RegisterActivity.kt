package com.github.lostfound

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.github.lostfound.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener { onBackPressedSupport() }

        val editPhone = binding.editPhone
        val editPassword = binding.editPassword
        val checkAccept = binding.checkAccept
        val btnRegister = binding.btnRegister
        val loading = binding.loading

        val changedAction = { _: CharSequence?, _: Int, _: Int, _ : Int ->
            btnRegister.isEnabled = (editPhone.text!!.isNotBlank() && editPassword.text!!.isNotBlank())
        }
        btnRegister.isEnabled = false
        editPhone.doOnTextChanged(changedAction)
        editPassword.doOnTextChanged(changedAction)

        btnRegister.setOnClickListener {
            if (!checkAccept.isChecked) {
                showToast(R.string.accept_eula_first, Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            lifecycleScope.launch {
                runOnUiThread {
                    editPhone.isEnabled = false
                    editPassword.isEnabled = false
                    btnRegister.isEnabled = false
                    loading.visibility = View.VISIBLE
                }
                withContext(Dispatchers.IO) {
                    val phone = editPhone.text.toString()
                    val password = editPassword.text.toString()
                    try {
                        val result = app.userService.register(phone, password)
                        result.getOrThrow().checkException()
                        showToast(R.string.register_success, Toast.LENGTH_SHORT)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("${e.message}", Toast.LENGTH_LONG)
                        runOnUiThread {
                            editPhone.isEnabled = true
                            editPassword.isEnabled = true
                            btnRegister.isEnabled = true
                            loading.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}