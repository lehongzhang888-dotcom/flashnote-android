package com.flashnote.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flashnote.app.Note
import com.flashnote.app.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 笔记列表适配器
 */
class NoteAdapter(
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note) -> Boolean
) : ListAdapter<Note, NoteAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            // 格式化时间
            binding.tvTimestamp.text = dateFormat.format(Date(note.timestamp))

            // 内容预览（前 50 字）
            val preview = if (note.content.length > 50) {
                note.content.take(50) + "…"
            } else if (note.content.isBlank()) {
                binding.root.context.getString(com.flashnote.app.R.string.empty_content_preview)
            } else {
                note.content
            }
            binding.tvContent.text = preview

            // 点击事件
            binding.root.setOnClickListener {
                onItemClick(note)
            }
            binding.root.setOnLongClickListener {
                onItemLongClick(note)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
