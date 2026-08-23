package com.vocabmaster.data.db

import androidx.room.*
import com.vocabmaster.data.db.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE lessonId = :lessonId")
    fun getWordsByLesson(lessonId: Long): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): Word?

    @Query("SELECT * FROM words WHERE isWeak = 1 ORDER BY wrongCount DESC")
    fun getWeakWords(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE (nextReviewDate <= :now AND isLearned = 0) OR repetitions = 0 ORDER BY nextReviewDate ASC LIMIT :limit")
    suspend fun getDueWords(now: Long, limit: Int = 20): List<Word>

    @Query("SELECT * FROM words WHERE term LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<Word>>

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1")
    fun countLearned(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 0 AND repetitions > 0")
    fun countLearning(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE repetitions = 0")
    fun countNew(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE isWeak = 1")
    fun countWeak(): Flow<Int>

    @Query("SELECT * FROM review_logs WHERE reviewDate >= :since ORDER BY reviewDate ASC")
    suspend fun getReviewsSince(since: Long): List<ReviewLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Insert
    suspend fun insertLog(log: ReviewLog)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY createdAt DESC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson): Long

    @Query("SELECT COUNT(*) FROM words WHERE lessonId = :lessonId AND isLearned = 1")
    fun countLearnedInLesson(lessonId: Long): Flow<Int>
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettings)
}
