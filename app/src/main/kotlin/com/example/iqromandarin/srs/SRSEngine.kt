package com.example.iqromandarin.srs

import java.util.concurrent.TimeUnit

/**
 * Leitner SRS (Spaced Repetition System) Engine
 *
 * Box intervals:
 * Box 0 = New / Wrong → review today
 * Box 1 = Correct once → review in 1 day
 * Box 2 = Correct twice → review in 3 days
 * Box 3 = Correct 3x → review in 7 days
 * Box 4 = Correct 4x → review in 14 days
 * Box 5 = Mastered → review in 30 days
 */
object SRSEngine {

    private val BOX_INTERVALS_DAYS = intArrayOf(
        0,  // Box 0: today
        1,  // Box 1: 1 day
        3,  // Box 2: 3 days
        7,  // Box 3: 1 week
        14, // Box 4: 2 weeks
        30  // Box 5: 1 month
    )

    /**
     * Calculate next review timestamp based on current SRS box.
     * Returns milliseconds timestamp.
     */
    fun calculateNextReview(currentBox: Int): Long {
        val safeBox = currentBox.coerceIn(0, BOX_INTERVALS_DAYS.size - 1)
        val daysUntilReview = BOX_INTERVALS_DAYS[safeBox]
        return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysUntilReview.toLong())
    }

    /**
     * Get next SRS box when item answered correctly
     */
    fun advanceBox(currentBox: Int): Int {
        return minOf(currentBox + 1, BOX_INTERVALS_DAYS.size - 1)
    }

    /**
     * Reset to box 0 when answered wrong (Leitner rule)
     */
    fun resetBox(): Int = 0

    /**
     * Check if item is due for review
     */
    fun isDue(nextReviewAt: Long): Boolean {
        return nextReviewAt > 0 && System.currentTimeMillis() >= nextReviewAt
    }

    /**
     * Get label for box number
     */
    fun getBoxLabel(box: Int): String {
        return when (box) {
            0 -> "Baru / Perlu diulang hari ini"
            1 -> "Review besok"
            2 -> "Review 3 hari lagi"
            3 -> "Review seminggu lagi"
            4 -> "Review 2 minggu lagi"
            5 -> "⭐ Hafal! Review sebulan lagi"
            else -> "Review"
        }
    }

    /**
     * Get days until next review
     */
    fun getDaysUntilReview(nextReviewAt: Long): Long {
        if (nextReviewAt == 0L) return 0
        val diff = nextReviewAt - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }
}
