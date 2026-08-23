package com.vocabmaster.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sourceFile: String,
    val type: String,
    val totalWords: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonId: Long,
    val term: String,
    val meaning: String,
    val example1: String = "",
    val example2: String = "",
    val level: String = "",
    val topic: String = "",
    val priorityRank: Int = 0,
    val easeFactor: Float = 2.5f,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val masteryLevel: Int = 0,
    val nextReviewDate: Long = 0,
    val lastReviewDate: Long = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val isLearned: Boolean = false,
    val isWeak: Boolean = false
)

@Entity(tableName = "review_logs")
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val quality: Int,
    val wasCorrect: Boolean,
    val reviewDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val reminderDays: String = "1,2,3,4,5,6,7",
    val dailyGoal: Int = 20,
    val themeMode: Int = 0
)
