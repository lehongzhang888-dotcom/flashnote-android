package com.flashnote.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * 笔记数据存储 - 使用 JSON 文件存储笔记
 *
 * 文件路径: [app私有目录]/notes.json
 * 格式: { "notes": [{"id": "...", "content": "...", "timestamp": 1234567890}] }
 */
class NoteStorage private constructor(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val notesFile: File get() = File(context.filesDir, FILE_NAME)

    /**
     * 读取所有笔记（按时间倒序）
     */
    fun loadNotes(): MutableList<Note> {
        return try {
            if (!notesFile.exists()) {
                return mutableListOf()
            }
            val json = notesFile.readText()
            val type = object : TypeToken<NotesData>() {}.type
            val data: NotesData = gson.fromJson(json, type) ?: NotesData()
            data.notes.sortedByDescending { it.timestamp }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    /**
     * 添加一条笔记
     */
    fun addNote(note: Note): Boolean {
        return try {
            val notes = loadNotes()
            notes.add(0, note)
            saveNotes(notes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除一条笔记
     */
    fun deleteNote(noteId: String): Boolean {
        return try {
            val notes = loadNotes()
            val iterator = notes.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().id == noteId) {
                    iterator.remove()
                    break
                }
            }
            saveNotes(notes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 将笔记列表写入 JSON 文件
     */
    private fun saveNotes(notes: List<Note>): Boolean {
        return try {
            val data = NotesData(notes = notes)
            val json = gson.toJson(data)
            notesFile.parentFile?.mkdirs()
            notesFile.writeText(json)
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val FILE_NAME = "notes.json"

        @Volatile
        private var instance: NoteStorage? = null

        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): NoteStorage {
            return instance ?: synchronized(this) {
                instance ?: NoteStorage(context.applicationContext).also { instance = it }
            }
        }
    }
}
