package com.github.lostfound

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.ConditionVariable
import android.widget.EditText
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.github.lostfound.api.FileService
import com.github.lostfound.api.PostService
import com.github.lostfound.api.UserService
import com.github.lostfound.entity.User
import com.github.lostfound.util.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

class MainApplication: Application(), PreferenceProvider {

    override val preferenceName: String
        get() = "preferences"
    override val context: Context
        get() = this

    private lateinit var cookieJar: ClearableCookieJar
    private lateinit var retrofit: Retrofit
    lateinit var userService: UserService
    lateinit var postService: PostService
    lateinit var fileService: FileService
    var loggedIn = ConditionVariable(false)
    var loggedInUser: User? = null

    var username by StringPreference()
    var password by StringPreference()
    private var upstreamServer by StringPreference("http://192.168.123.177:8080/")

    fun getFileUrl(key: String) = "${upstreamServer}file/download/$key"

    override fun onCreate() {
        super.onCreate()
        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(this))
        val client = OkHttpClient.Builder()
            .connectTimeout(3L, TimeUnit.SECONDS)
            .readTimeout(10L, TimeUnit.SECONDS)
            .writeTimeout(10L, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl(upstreamServer)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(client)
            .build()
        userService = retrofit.create()
        postService = retrofit.create()
        fileService = retrofit.create()
    }

    fun showServerDialog(context: Context) {
        val edit = EditText(context).apply {
            setText(upstreamServer)
        }
        AlertDialog.Builder(context)
            .setTitle("设置服务器")
            .setView(edit)
            .setNeutralButton(R.string.dialog_sure) { _, _ ->
                upstreamServer = edit.text.toString()
            }
            .create()
            .show()
    }
}