package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Delete
    suspend fun deleteSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}

@Dao
interface CodeDao {
    @Query("SELECT * FROM code_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<CodeProjectEntity>>

    @Query("SELECT * FROM code_projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): CodeProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: CodeProjectEntity)

    @Delete
    suspend fun deleteProject(project: CodeProjectEntity)

    @Query("SELECT * FROM code_files WHERE projectId = :projectId ORDER BY filename ASC")
    fun getFilesForProject(projectId: String): Flow<List<CodeFileEntity>>

    @Query("SELECT * FROM code_files WHERE projectId = :projectId")
    suspend fun getFilesForProjectSync(projectId: String): List<CodeFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: CodeFileEntity)

    @Delete
    suspend fun deleteFile(file: CodeFileEntity)
}

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translation_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentTranslations(): Flow<List<TranslationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(log: TranslationLogEntity)

    @Query("DELETE FROM translation_logs")
    suspend fun clearTranslations()
}

@Dao
interface LearnedPatternDao {
    @Query("SELECT * FROM learned_patterns ORDER BY usageCount DESC")
    fun getAllPatterns(): Flow<List<LearnedPatternEntity>>

    @Query("SELECT * FROM learned_patterns WHERE patternKey = :key LIMIT 1")
    suspend fun getPatternByKey(key: String): LearnedPatternEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: LearnedPatternEntity)

    @Query("SELECT * FROM learned_patterns ORDER BY lastUpdated DESC LIMIT 10")
    suspend fun getRecentPatterns(): List<LearnedPatternEntity>
}

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        CodeProjectEntity::class,
        CodeFileEntity::class,
        TranslationLogEntity::class,
        LearnedPatternEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TrillAiDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun codeDao(): CodeDao
    abstract fun translationDao(): TranslationDao
    abstract fun learnedPatternDao(): LearnedPatternDao

    companion object {
        @Volatile
        private var INSTANCE: TrillAiDatabase? = null

        fun getDatabase(context: android.content.Context): TrillAiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrillAiDatabase::class.java,
                    "trill_ai_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
