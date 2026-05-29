package com.flashnote.app

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flashnote.app.databinding.ActivityNoteInputBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 笔记输入界面 - 半透明背景，快速记录笔记
 *
 * 点击悬浮球 → 弹出此界面 → 输入内容 → 保存/取消
 */
class NoteInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteInputBinding
    private lateinit var noteStorage: NoteStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteStorage = NoteStorage.getInstance(this)

        setupTimeDisplay()
        setupButtons()
        setupAutoFocus()
    }

    /**
     * 显示当前时间
     */
    private fun setupTimeDisplay() {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        binding.tvCurrentTime.text = dateFormat.format(Date())

        // 每秒更新时间
        binding.tvCurrentTime.postDelayed(object : Runnable {
            override fun run() {
                binding.tvCurrentTime.text = dateFormat.format(Date())
                binding.tvCurrentTime.postDelayed(this, 1000)
            }
        }, 1000)
    }

    /**
     * 设置按钮事件
     */
    private fun setupButtons() {
        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveNote()
        }

        // 取消按钮
        binding.btnCancel.setOnClickListener {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    /**
     * 自动弹出键盘
     */
    private fun setupAutoFocus() {
        binding.etNoteContent.requestFocus()
        // 延迟弹出键盘
        binding.etNoteContent.postDelayed({
            val imm = getSystemService(android.view.inputmethod.InputMethodManager::class.java)
            imm.showSoftInput(binding.etNoteContent, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    /**
     * 保存笔记
     */
    private fun saveNote() {
        val content = binding.etNoteContent.text.toString().trim()

        if (content.isEmpty()) {
            Toast.makeText(this, R.string.note_empty, Toast.LENGTH_SHORT).show()
            return
        }

        val note = Note(content = content)
        val success = noteStorage.addNote(note)

        if (success) {
            Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show()
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        } else {
            Toast.makeText(this, R.string.note_save_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
