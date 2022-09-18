package com.github.lostfound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.lostfound.adapter.AppAdapter
import com.github.lostfound.databinding.FragmentMyBinding
import com.github.lostfound.entity.App
import com.github.lostfound.util.imageDisplay
import kotlinx.coroutines.launch

class MyselfFragment: BaseFragment() {
    private lateinit var binding: FragmentMyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyBinding.inflate(inflater, container, false)

        val url = app.getFileUrl(app.loggedInUser!!.avatar)
        imageDisplay(requireActivity(), url, binding.imgAvatar)

        val apps = ArrayList<App>()
        apps.add(App(-1, 0, ""))
        apps.add(App(appMySubmit, R.drawable.ic_baseline_folder_shared_24, getString(R.string.my_submits), getColor(R.color.icon_blue)))
        apps.add(App(-1, 0, ""))
        apps.add(App(appSetting, R.drawable.ic_baseline_account_circle_24, getString(R.string.personal_info), getColor(R.color.wechat_icon_green)))
        apps.add(App(-1, 0, ""))
        apps.add(App(appExit, R.drawable.ic_baseline_exit_to_app_24, getString(R.string.logout), getColor(R.color.icon_red)))
        val appAdapter = AppAdapter(requireActivity(), apps) {
            when (it.id) {
                appMySubmit -> startActivity(MySubmitsActivity::class.intent)
                appSetting -> startActivity(PersonalDetailActivity::class.intent)
                appExit -> {
                    askDialog(
                        iconId = R.drawable.ic_baseline_warning_24,
                        titleId = R.string.logout,
                        messageId = R.string.logout_sure,
                        onConfirm = { _, _ ->
                            lifecycleScope.launch {
                                launchIOToastException({
                                    app.userService.logout()
                                }) {
                                    app.username = ""
                                    app.password = ""
                                    app.loggedIn.close()
                                    requireActivity().finishAffinity()
                                    startActivity(RoutingActivity::class.intent)
                                }
                            }
                        }
                    )
                }
            }
        }
        binding.rvContent.apply {
            adapter = appAdapter
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.tvName.text = app.username
    }

    companion object {
        private const val appMySubmit = 1
        private const val appSetting = 2
        private const val appExit = 3
    }
}