package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String = "Creative Writing",
    val deepThinkingEnabled: Boolean = false,
    val webSearchEnabled: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thinkingText: String? = null,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val imageUrl: String? = null,
    val isStreaming: Boolean = false
)

@Entity(tableName = "code_projects")
data class CodeProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val defaultLanguage: String = "javascript"
)

@Entity(tableName = "code_files")
data class CodeFileEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val filename: String,
    val language: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "translation_logs")
data class TranslationLogEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceLang: String,
    val targetLang: String,
    val originalText: String,
    val translatedText: String,
    val isBackground: Boolean = false
)

@Entity(tableName = "learned_patterns")
data class LearnedPatternEntity(
    @PrimaryKey
    val id: String,
    val patternKey: String,
    val patternCategory: String, // "tone", "topic", "vocabulary", "coding_preference"
    val value: String,
    val confidence: Float = 1.0f,
    val usageCount: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)
