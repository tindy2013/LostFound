package com.github.lostfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.github.lostfound.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener { onBackPressedSupport() }

        val editUsername = binding.editPhone
        val editPassword = binding.editPassword
        val btnLogin = binding.btnLogin

        val changedAction = { _: CharSequence?, _: Int, _: Int, _ : Int ->
            btnLogin.isEnabled = (editUsername.text!!.isNotBlank() && editPassword.text!!.isNotBlank())
        }
        editUsername.doOnTextChanged(changedAction)
        editPassword.doOnTextChanged(changedAction)

        val loading = binding.loading

        btnLogin.setOnClickListener {
            runOnUiThread {
                editUsername.isEnabled = false
                editPassword.isEnabled = false
                btnLogin.isEnabled = false
                loading.visibility = View.VISIBLE
            }
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val username = editUsername.text.toString()
                    val password = editPassword.text.toString()
                    try {
                        val call = app.userService.login(username, password)
                        val result = call.getOrThrow().checkException()
                        showToast(R.string.login_success, Toast.LENGTH_SHORT)
                        app.loggedInUser = result.data
                        app.loggedIn.open()
                        app.username = username
                        app.password = password
                        val intent = Intent().putExtra("action", "login")
                        setResult(RESULT_OK, intent)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("${e.message}", Toast.LENGTH_SHORT)
                        runOnUiThread {
                            editUsername.isEnabled = true
                            editPassword.isEnabled = true
                            btnLogin.isEnabled = true
                            loading.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}