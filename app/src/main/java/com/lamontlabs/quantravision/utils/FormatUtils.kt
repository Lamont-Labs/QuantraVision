package com.lamontlabs.quantravision.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object FormatUtils {
    
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000} min ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
            diff < 172_800_000 -> "Yesterday"
            diff < 604_800_000 -> "${diff / 86_400_000} days ago"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
    
    fun formatDateTime(timestamp: Long, includeTime: Boolean = true): String {
        val pattern = if (includeTime) {
            "MMM dd, yyyy hh:mm a"
        } else {
            "MMM dd, yyyy"
        }
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    fun formatCurrency(value: Double, currencyCode: String = "USD"): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance(currencyCode)
        return format.format(value)
    }
    
    fun formatPercentage(value: Float, decimalPlaces: Int = 2): String {
        return String.format(Locale.US, "%.${decimalPlaces}f%%", value)
    }
    
    fun formatConfidence(confidence: Double): String {
        return "${(confidence * 100.0).toInt()}%"
    }
    
    fun formatNumber(value: Long): String {
        return when {
            abs(value) < 1_000 -> value.toString()
            abs(value) < 1_000_000 -> String.format(Locale.US, "%.1fK", value / 1_000.0)
            abs(value) < 1_000_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
            else -> String.format(Locale.US, "%.1fB", value / 1_000_000_000.0)
        }
    }
    
    fun formatPriceChange(change: Double, decimalPlaces: Int = 2): String {
        val prefix = if (change >= 0) "+" else ""
        return prefix + String.format(Locale.US, "%.${decimalPlaces}f", change)
    }
    
    fun formatDecimal(value: Double, decimalPlaces: Int = 2): String {
        return String.format(Locale.US, "%.${decimalPlaces}f", value)
    }
    
    fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""}"
            else -> "$seconds sec${if (seconds > 1) "s" else ""}"
        }
    }
    
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.take(maxLength - 3) + "..."
        } else {
            text
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
