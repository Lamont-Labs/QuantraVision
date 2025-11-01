package com.lamontlabs.quantravision.education

import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.lessons.LessonRegistry

object LessonRepository {
    fun getAllLessons(): List<Lesson> = LessonRegistry.getAllLessons()
    
    fun getLessonById(id: Int): Lesson? = getAllLessons().firstOrNull { it.id == id }
    
    fun getLessonsByCategory(category: String): List<Lesson> = 
        getAllLessons().filter { it.category == category }
}
