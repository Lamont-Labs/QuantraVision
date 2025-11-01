package com.lamontlabs.quantravision.education.course

import com.lamontlabs.quantravision.education.EducationCourse.Lesson

/**
 * Central registry aggregating all course lessons
 * Follows same pattern as LessonRegistry for education/lessons
 */
object CourseRegistry {
    
    fun getAllCourseLessons(): List<Lesson> = listOf(
        courseLesson01,
        courseLesson02,
        courseLesson03,
        courseLesson04,
        courseLesson05,
        courseLesson06,
        courseLesson07,
        courseLesson08,
        courseLesson09,
        courseLesson10,
        courseLesson11,
        courseLesson12,
        courseLesson13,
        courseLesson14,
        courseLesson15,
        courseLesson16,
        courseLesson17,
        courseLesson18,
        courseLesson19,
        courseLesson20,
        courseLesson21,
        courseLesson22,
        courseLesson23,
        courseLesson24,
        courseLesson25
    )
    
    fun getCourseLesson(id: Int): Lesson? {
        return getAllCourseLessons().find { it.id == id }
    }
}
