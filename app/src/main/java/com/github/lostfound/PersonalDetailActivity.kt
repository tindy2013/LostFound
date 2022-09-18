package com.github.lostfound

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.lostfound.adapter.AppAdapter
import com.github.lostfound.databinding.ActivitySimpleListBinding
import com.github.lostfound.entity.App
import kotlinx.coroutines.launch


class PersonalDetailActivity : BaseActivity() {
    private lateinit var binding: ActivitySimpleListBinding
    private lateinit var appAdapter: AppAdapter
    private val apps = ArrayList<App>()

    private fun updateStringView(id: Int, text: String) {
        val index = apps.indexOfFirst { it.id == id }
        val item = apps[index]
        item.textRight = text
        apps[index] = item
        appAdapter.notifyItemChanged(index)
    }

    private fun showUpdateSuccess() {
        showToast(R.string.update_info_success, Toast.LENGTH_SHORT)
    }

    override fun onResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (data?.getBooleanExtra("fieldUpdate", false) == true) {
                lifecycleScope.launch {
                    launchIOToastException {
                        val newValue = data.getStringExtra("fieldValue") ?: return@launchIOToastException
                        when (data.getStringExtra("fieldName")) {
                            "username" -> {
                                val call = app.userService.updateInfo(username = newValue)
                                val result = call.getOrThrow().checkException()
                                if (result.code == 0) {
                                    app.loggedInUser!!.username = newValue
                                    runOnUiThread {
                                        showUpdateSuccess()
                                        updateStringView(appUsername, newValue)
                                    }
                                }
                            }
                            "contact_name" -> launchIOToastException {
                                val call = app.userService.updateInfo(contactName = newValue)
                                val result = call.getOrThrow().checkException()
                                if (result.code == 0) {
                                    app.loggedInUser!!.name = newValue
                                    runOnUiThread {
                                        showUpdateSuccess()
                                        updateStringView(appContactName, newValue)
                                    }
                                }
                            }
                            "contact_number" -> launchIOToastException {
                                val call = app.userService.updateInfo(contactNumber = newValue)
                                val result = call.getOrThrow().checkException()
                                if (result.code == 0) {
                                    app.loggedInUser!!.number = newValue
                                    runOnUiThread {
                                        showUpdateSuccess()
                                        updateStringView(appContactNumber, newValue)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                data ?: return
                val oldPass = data.getStringExtra("oldPass") ?: return
                val newPass = data.getStringExtra("newPass") ?: return
                lifecycleScope.launch {
                    launchIOToastException {
                        val call = app.userService.resetPassword(oldPass, newPass)
                        val result = call.getOrThrow().checkException()
                        if (result.code == 0) {
                            showUpdateSuccess()
                            app.password = newPass
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = getString(R.string.personal_info)
        binding.swipe.isEnabled = false
        binding.btnBack.setOnClickListener { onBackPressedSupport() }

        val user = app.loggedInUser!!

        apps.add(App(appUsername, -1, getString(R.string.username), textRight = user.username))
        apps.add(App(appPassword, -1, getString(R.string.update_password)))
        apps.add(App(-1, -1, ""))
        apps.add(App(appContactName, -1, getString(R.string.contact_name), textRight = user.name))
        apps.add(App(appContactNumber, -1, getString(R.string.contact_number), textRight = user.number))

        appAdapter = AppAdapter(this, apps) {
            when (it.id) {
                appUsername -> launchFieldUpdater(getString(R.string.set_username), "username", app.loggedInUser!!.username)
                appPassword -> activityLauncher.launch(UpdatePasswordActivity::class.intent)
                appContactName -> launchFieldUpdater(getString(R.string.set_contact_name), "contact_name", app.loggedInUser!!.name)
                appContactNumber -> launchFieldUpdater(getString(R.string.set_contact_number), "contact_number", app.loggedInUser!!.number)
            }
        }
        binding.rvContent.apply {
            adapter = appAdapter
            layoutManager = LinearLayoutManager(this@PersonalDetailActivity, LinearLayoutManager.VERTICAL, false)
        }
    }

    companion object {
        private const val appUsername = 1
        private const val appPassword = 2
        private const val appContactName = 3
        private const val appContactNumber = 4
    }
}