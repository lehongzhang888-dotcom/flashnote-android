package com.flashnote.app

import java.util.UUID

/**
 * 笔记数据模型
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * JSON 存储的根数据结构
 */
data class NotesData(
    val notes: List<Note> = emptyList()
)
