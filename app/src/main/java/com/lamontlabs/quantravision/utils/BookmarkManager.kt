package com.lamontlabs.quantravision.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages bookmark positions for the trading book
 * Saves and restores reading progress using SharedPreferences
 */
object BookmarkManager {
    private const val PREFS_NAME = "quantravision_bookmarks"
    private const val KEY_BOOK_SCROLL_POSITION = "book_scroll_position"
    private const val KEY_BOOK_LAST_READ_TIME = "book_last_read_time"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save the current scroll position in the book
     * @param context Application context
     * @param scrollPosition Current scroll position (0 = top)
     */
    fun saveBookmark(context: Context, scrollPosition: Int) {
        getPrefs(context).edit().apply {
            putInt(KEY_BOOK_SCROLL_POSITION, scrollPosition)
            putLong(KEY_BOOK_LAST_READ_TIME, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * Get the saved scroll position for the book
     * @param context Application context
     * @return Saved scroll position, or 0 if no bookmark exists
     */
    fun getBookmark(context: Context): Int {
        return getPrefs(context).getInt(KEY_BOOK_SCROLL_POSITION, 0)
    }
    
    /**
     * Check if a bookmark exists
     * @param context Application context
     * @return True if user has saved a reading position
     */
    fun hasBookmark(context: Context): Boolean {
        return getPrefs(context).contains(KEY_BOOK_SCROLL_POSITION)
    }
    
    /**
     * Clear the bookmark (reset to beginning)
     * @param context Application context
     */
    fun clearBookmark(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_BOOK_SCROLL_POSITION)
            remove(KEY_BOOK_LAST_READ_TIME)
            apply()
        }
    }
    
    /**
     * Get when the user last read the book
     * @param context Application context
     * @return Timestamp in milliseconds, or 0 if never read
     */
    fun getLastReadTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_BOOK_LAST_READ_TIME, 0L)
    }
    
    /**
     * Get progress percentage through the book
     * @param scrollPosition Current scroll position
     * @param maxScroll Maximum scroll value
     * @return Progress percentage (0-100)
     */
    fun getProgressPercentage(scrollPosition: Int, maxScroll: Int): Int {
        if (maxScroll <= 0) return 0
        return ((scrollPosition.toFloat() / maxScroll.toFloat()) * 100).toInt().coerceIn(0, 100)
    }
}
