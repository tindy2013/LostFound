package com.github.lostfound

import android.content.DialogInterface
import android.content.Intent
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.yokeyword.fragmentation.SupportFragment
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class BaseFragment: SupportFragment() {
    protected val app: MainApplication
        get() = requireActivity().application as MainApplication

    protected val KClass<*>.intent: Intent
        get() = Intent(requireActivity().applicationContext, this.java)

    protected val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { onResult(it.resultCode, it.data) }

    protected open fun onResult(resultCode: Int, data: Intent?) {}

    protected fun runOnUiThread(action: () -> Unit) = requireActivity().runOnUiThread(action)

    protected fun showToast(text: CharSequence, duration: Int) { runOnUiThread { Toast.makeText(requireActivity(), text, duration).show() } }
    protected fun showToast(@StringRes id: Int, duration: Int) { runOnUiThread { Toast.makeText(requireActivity(), id, duration).show() } }

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

    fun getColor(@ColorRes id: Int) = requireActivity().getColor(id)

    protected fun askDialog(@DrawableRes iconId: Int? = null, @StringRes titleId: Int, @StringRes messageId: Int, onConfirm: (DialogInterface, Int) -> Unit = { _, _ -> }, neutral: Boolean = false) {
        AlertDialog.Builder(requireActivity())
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