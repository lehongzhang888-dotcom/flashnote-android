package com.flashnote.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.flashnote.app.adapter.NoteAdapter
import com.flashnote.app.databinding.ActivityMainBinding

/**
 * 主界面 - 笔记列表 + 控制悬浮球开关
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteStorage: NoteStorage
    private lateinit var adapter: NoteAdapter

    // 悬浮窗权限请求
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkOverlayPermissionAndStartService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteStorage = NoteStorage.getInstance(this)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        refreshNoteList()
    }

    override fun onResume() {
        super.onResume()
        refreshNoteList()
    }

    /**
     * 设置工具栏
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // 点击图标启动/关闭悬浮球
            if (isFloatingBallRunning()) {
                stopFloatingBallService()
            } else {
                requestOverlayPermissionAndStart()
            }
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                else -> false
            }
        }
    }

    /**
     * 设置 RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onItemClick = { note -> showNoteDetailDialog(note) },
            onItemLongClick = { note -> showDeleteConfirmDialog(note); true }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
    }

    /**
     * 设置 FAB 快速笔记按钮
     */
    private fun setupFab() {
        binding.fabQuickNote.setOnClickListener {
            openNoteInput()
        }
    }

    /**
     * 刷新笔记列表
     */
    private fun refreshNoteList() {
        val notes = noteStorage.loadNotes()
        adapter.submitList(notes)
        binding.tvEmptyHint.visibility = if (notes.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    /**
     * 打开笔记输入界面
     */
    private fun openNoteInput() {
        val intent = Intent(this, NoteInputActivity::class.java)
        startActivity(intent)
    }

    /**
     * 显示笔记详情对话框
     */
    private fun showNoteDetailDialog(note: Note) {
        val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault())
        val timeStr = dateFormat.format(java.util.Date(note.timestamp))

        AlertDialog.Builder(this)
            .setTitle(timeStr)
            .setMessage(note.content)
            .setPositiveButton("关闭", null)
            .show()
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog(note: Note) {
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

    // ─── 悬浮球控制 ──────────────────────────────────────

    /**
     * 检查并请求悬浮窗权限，然后启动服务
     */
    private fun requestOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingBallService()
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
        } else {
            startFloatingBallService()
        }
    }

    /**
     * 检查权限并启动服务（权限授予后的回调）
     */
    private fun checkOverlayPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingBallService()
            } else {
                Toast.makeText(this, R.string.floating_ball_permission_required, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 启动悬浮球服务
     */
    private fun startFloatingBallService() {
        val intent = Intent(this, FloatingBallService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, R.string.floating_ball_running, Toast.LENGTH_SHORT).show()
    }

    /**
     * 停止悬浮球服务
     */
    private fun stopFloatingBallService() {
        val intent = Intent(this, FloatingBallService::class.java)
        stopService(intent)
        Toast.makeText(this, R.string.floating_ball_stopped, Toast.LENGTH_SHORT).show()
    }

    /**
     * 检查悬浮球是否正在运行
     */
    private fun isFloatingBallRunning(): Boolean {
        return FloatingBallService.isRunning
    }
}
