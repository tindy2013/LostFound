package com.github.lostfound

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import com.github.lostfound.databinding.ActivityUpdatePasswordBinding

class UpdatePasswordActivity: BaseActivity() {
    private lateinit var binding: ActivityUpdatePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = getString(R.string.update_password)

        val editOldPass = binding.editOldPass
        val editNewPass = binding.editNewPass
        val editRepeatPass = binding.editRepeatPass
        val btnFinish = binding.btnFinish

        binding.btnReset.setOnClickListener {
            editOldPass.text?.clear()
            editNewPass.text?.clear()
            editRepeatPass.text?.clear()
        }

        val changedAction = { _: CharSequence?, _: Int, _: Int, _ : Int ->
            btnFinish.isEnabled =
                (editOldPass.text!!.isNotBlank() && editNewPass.text!!.isNotBlank() && editRepeatPass.text!!.isNotBlank() && editNewPass.text.toString() == editRepeatPass.text.toString())
        }

        btnFinish.isEnabled = false
        editOldPass.doOnTextChanged(changedAction)
        editNewPass.doOnTextChanged(changedAction)
        editRepeatPass.doOnTextChanged(changedAction)

        btnFinish.setOnClickListener {


            val oldPassword = editOldPass.text.toString()
            val newPassword = editNewPass.text.toString()
            setResult(RESULT_OK, Intent()
                .putExtra("oldPass", oldPassword)
                .putExtra("newPass", newPassword)
            )
            finish()
        }
    }
}