package com.lamontlabs.quantravision.achievements.model

enum class AchievementCategory {
    DETECTION,
    EDUCATION,
    STREAK,
    MASTERY,
    SPECIAL
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val iconEmoji: String,
    val totalRequired: Int = 1,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Int = 0
) {
    fun getProgressPercent(): Float {
        return if (totalRequired > 0) {
            (progress.toFloat() / totalRequired.toFloat()).coerceIn(0f, 1f)
        } else {
            if (isUnlocked) 1f else 0f
        }
    }
    
    fun isCompleted(): Boolean = isUnlocked
    
    companion object {
        fun fromJson(json: Map<String, Any>): Achievement {
            val categoryStr = json["category"] as? String ?: "SPECIAL"
            val category = try {
                AchievementCategory.valueOf(categoryStr)
            } catch (e: Exception) {
                AchievementCategory.SPECIAL
            }
            
            return Achievement(
                id = json["id"] as? String ?: "",
                title = json["title"] as? String ?: "",
                description = json["description"] as? String ?: "",
                category = category,
                iconEmoji = json["icon"] as? String ?: "⭐",
                totalRequired = (json["totalRequired"] as? Number)?.toInt() ?: 1,
                isUnlocked = false,
                unlockedAt = null,
                progress = 0
            )
        }
    }
}
