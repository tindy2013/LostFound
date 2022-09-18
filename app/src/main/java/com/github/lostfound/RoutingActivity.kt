package com.github.lostfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.github.lostfound.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

class RoutingActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var login: Button
    private lateinit var register: Button
    private var showSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        login = binding.btnLogin
        register = binding.btnRegister
        login.setOnClickListener {
            activityLauncher.launch(LoginActivity::class.intent)
        }
        register.setOnClickListener {
            activityLauncher.launch(RegisterActivity::class.intent)
        }
        binding.btnServer.setOnClickListener { app.showServerDialog(this) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val showButtons = {
            runOnUiThread {
                showSplash = false
                login.visibility = View.VISIBLE
                register.visibility = View.VISIBLE
            }
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val username = app.username
                val password = app.password
                try {
                    if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                        val call = app.userService.login(username, password)
                        val result = call.getOrThrow().checkException()
                        if (result.code == 0) {
                            app.loggedIn.open()
                            app.loggedInUser = result.data
                            thread {
                                Thread.sleep(500)
                                this@RoutingActivity.startActivity(MainActivity::class.intent)
                                finish()
                            }
                            return@withContext
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread { showToast(e.message!!, Toast.LENGTH_SHORT) }
                }
                showButtons()
            }
        }
    }

    override fun onResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (data?.getStringExtra("action")) {
                "login" -> {
                    if (app.loggedIn.block(0)) {
                        startActivity(MainActivity::class.intent)
                        finish()
                    }
                }
            }
        }
    }
}