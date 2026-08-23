package com.vocabmaster.algorithm

import com.vocabmaster.data.db.entities.ReviewLog
import com.vocabmaster.data.db.entities.Word

object SpacedRepetition {

    data class ReviewResult(
        val updatedWord: Word,
        val log: ReviewLog
    )

    fun processReview(word: Word, quality: Int): ReviewResult {
        val now = System.currentTimeMillis()
        val wasCorrect = quality >= 3

        var ef = word.easeFactor
        var interval = word.interval
        var reps = word.repetitions
        var mastery = word.masteryLevel
        var correct = word.correctCount
        var wrong = word.wrongCount
        var isWeak = word.isWeak
        var isLearned = word.isLearned

        if (quality < 3) {
            reps = 0
            interval = 1
            mastery = maxOf(0, mastery - 2)
            wrong++
            isLearned = false
            if (wrong >= 2) isWeak = true
        } else {
            correct++
            when (reps) {
                0 -> interval = 1
                1 -> interval = 6
                else -> interval = (interval * ef).toInt()
            }
            reps++
            mastery = minOf(5, mastery + 1)
            if (mastery >= 5) isLearned = true
        }

        ef = (ef + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))).coerceAtLeast(1.3f)

        val nextReview = now + interval * 24L * 60 * 60 * 1000

        val updatedWord = word.copy(
            easeFactor = ef,
            interval = interval,
            repetitions = reps,
            masteryLevel = mastery,
            nextReviewDate = nextReview,
            lastReviewDate = now,
            correctCount = correct,
            wrongCount = wrong,
            isWeak = isWeak,
            isLearned = isLearned
        )

        val log = ReviewLog(
            wordId = word.id,
            quality = quality,
            wasCorrect = wasCorrect
        )

        return ReviewResult(updatedWord, log)
    }
}
