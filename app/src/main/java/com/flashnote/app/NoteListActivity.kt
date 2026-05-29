package com.flashnote.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flashnote.app.adapter.NoteAdapter
import com.flashnote.app.databinding.ActivityNoteListBinding

/**
 * 笔记列表界面 - 查看所有笔记
 *
 * 点击笔记查看全文，长按删除笔记。
 */
class NoteListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteListBinding
    private lateinit var noteStorage: NoteStorage
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteStorage = NoteStorage.getInstance(this)

        setupToolbar()
        setupRecyclerView()
        refreshNoteList()
    }

    override fun onResume() {
        super.onResume()
        refreshNoteList()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onItemClick = { note -> showNoteDetail(note) },
            onItemLongClick = { note -> showDeleteConfirm(note); true }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
    }

    private fun refreshNoteList() {
        val notes = noteStorage.loadNotes()
        adapter.submitList(notes)
        binding.tvEmptyHint.visibility = if (notes.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    /**
     * 弹窗显示笔记全文
     */
    private fun showNoteDetail(note: Note) {
        val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault())
        val timeStr = dateFormat.format(java.util.Date(note.timestamp))

        AlertDialog.Builder(this)
            .setTitle(timeStr)
            .setMessage(note.content)
            .setPositiveButton("关闭", null)
            .show()
    }

    /**
     * 删除确认
     */
    private fun showDeleteConfirm(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("删除笔记")
            .setMessage(getString(R.string.delete_note_confirm))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                noteStorage.deleteNote(note.id)
                refreshNoteList()
                Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
