package com.github.lostfound

import android.content.DialogInterface
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.yokeyword.fragmentation.SupportActivity
import kotlin.reflect.KClass


@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class BaseActivity: SupportActivity() {
    protected val KClass<*>.intent: Intent
        get() = Intent(applicationContext, this.java)

    protected val app: MainApplication
        get() = application as MainApplication

    protected val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { onResult(it.resultCode, it.data) }

    protected open fun onResult(resultCode: Int, data: Intent?) {}

    protected fun launchFieldUpdater(title: String, fieldName: String, fieldValue: String, description: String = "") {
        activityLauncher.launch(UpdateFieldActivity::class.intent
            .putExtra("title", title)
            .putExtra("fieldName", fieldName)
            .putExtra("fieldValue", fieldValue)
            .putExtra("description", description)
        )
    }

    protected fun showToast(text: CharSequence, duration: Int) { runOnUiThread { Toast.makeText(this, text, duration).show() } }
    protected fun showToast(@StringRes id: Int, duration: Int) { runOnUiThread { Toast.makeText(this, id, duration).show() } }

    protected suspend fun launchToastException(action: suspend () -> Unit, finally: suspend () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("${e.message}", Toast.LENGTH_SHORT)
        } finally {
            finally()
        }
    }

    protected suspend fun launchIOToastException(action: suspend () -> Unit, finally: suspend () -> Unit) =
        launchToastException({ withContext(Dispatchers.IO) { action() } }, finally)

    protected suspend fun launchToastException(action: suspend () -> Unit) = launchToastException(action) {}

    protected suspend fun launchIOToastException(action: suspend () -> Unit) = launchIOToastException(action) {}

    protected fun runToastException(action: () -> Unit, finally: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("${e.message}", Toast.LENGTH_SHORT)
        } finally {
            finally()
        }
    }

    protected fun askDialog(@DrawableRes iconId: Int? = null, @StringRes titleId: Int, @StringRes messageId: Int, onConfirm: (DialogInterface, Int) -> Unit = { _, _ -> }, neutral: Boolean = false) {
        AlertDialog.Builder(this)
            .apply { if (iconId != null) setIcon(iconId) }
            .setTitle(titleId)
            .setMessage(messageId)
            .apply {
                if (neutral)
                    setNeutralButton(R.string.dialog_ok, onConfirm)
                else {
                    setPositiveButton(R.string.dialog_sure, onConfirm)
                    setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                }
            }
            .create()
            .show()
    }
}