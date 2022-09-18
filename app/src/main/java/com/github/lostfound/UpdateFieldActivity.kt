package com.github.lostfound

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import com.github.lostfound.databinding.ActivityUpdateFieldBinding

class UpdateFieldActivity : BaseActivity() {
    private lateinit var binding: ActivityUpdateFieldBinding
    private var fieldValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateFieldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fieldName = intent.getStringExtra("fieldName") ?: "field"
        fieldValue = intent.getStringExtra("fieldValue") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        binding.btnFinish.setOnClickListener {
            val updatedValue = binding.editField.text.toString()
            if (updatedValue != fieldValue)
                setResult(RESULT_OK, Intent()
                    .putExtra("fieldName", fieldName)
                    .putExtra("fieldValue", updatedValue)
                    .putExtra("fieldUpdate", true)
                )
            finish()
        }

        binding.btnBack.setOnClickListener { onBackPressedSupport() }
        binding.tvTitle.text = title
        binding.tvDesc.text = description
        binding.editField.setText(fieldValue)

        binding.btnReset.setOnClickListener { binding.editField.setText(fieldValue) }
    }

    override fun onBackPressedSupport() {
        if (binding.editField.text.toString() != fieldValue) {
            AlertDialog.Builder(this)
                .setMessage(R.string.ask_save_changes)
                .setPositiveButton(R.string.save) { _, _ -> binding.btnFinish.callOnClick() }
                .setNegativeButton(R.string.do_not_save) { _, _ -> super.onBackPressedSupport() }
                .setCancelable(true)
                .create()
                .show()
        } else
            super.onBackPressedSupport()
        super.onBackPressedSupport()
    }
}