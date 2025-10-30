package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.education.Lesson
import com.lamontlabs.quantravision.education.LessonRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationHubScreen(
    context: Context,
    onBack: () -> Unit
) {
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    
    if (selectedLesson != null) {
        LessonDetailScreen(
            lesson = selectedLesson!!,
            onBack = { selectedLesson = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Education Hub") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A1218),
                        titleContentColor = Color(0xFF00E5FF)
                    )
                )
            },
            containerColor = Color(0xFF0A1218)
        ) { padding ->
            LessonListContent(
                modifier = Modifier.padding(padding),
                lessons = LessonRepository.getAllLessons(),
                onLessonClick = { selectedLesson = it }
            )
        }
    }
}

@Composable
private fun LessonListContent(
    modifier: Modifier = Modifier,
    lessons: List<Lesson>,
    onLessonClick: (Lesson) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Master Chart Pattern Recognition",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Complete 25 interactive lessons to earn your certification",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        val categories = lessons.groupBy { it.category }
        categories.forEach { (category, categoryLessons) ->
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(categoryLessons) { lesson ->
                LessonCard(
                    lesson = lesson,
                    onClick = { onLessonClick(lesson) }
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2530)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF)
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${lesson.duration} • ${lesson.quiz.questions.size} quiz questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonDetailScreen(
    lesson: Lesson,
    onBack: () -> Unit
) {
    var showQuiz by remember { mutableStateOf(false) }
    
    if (showQuiz) {
        QuizScreen(
            quiz = lesson.quiz,
            lessonTitle = lesson.title,
            onBack = { showQuiz = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(lesson.title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A1218),
                        titleContentColor = Color(0xFF00E5FF)
                    )
                )
            },
            containerColor = Color(0xFF0A1218)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = lesson.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
                
                Button(
                    onClick = { showQuiz = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF)
                    )
                ) {
                    Text("Take Quiz", color = Color.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizScreen(
    quiz: com.lamontlabs.quantravision.education.Quiz,
    lessonTitle: String,
    onBack: () -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    
    val currentQuestion = quiz.questions[currentQuestionIndex]
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz: $lessonTitle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1218),
                    titleContentColor = Color(0xFF00E5FF)
                )
            )
        },
        containerColor = Color(0xFF0A1218)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1) / quiz.questions.size.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF00E5FF)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            currentQuestion.options.forEachIndexed { index, option ->
                OptionCard(
                    option = option,
                    isSelected = selectedAnswer == index,
                    isCorrect = index == currentQuestion.correctAnswer,
                    showResult = showExplanation,
                    onClick = {
                        if (!showExplanation) {
                            selectedAnswer = index
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (showExplanation) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A2530)
                    )
                ) {
                    Text(
                        text = currentQuestion.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (!showExplanation) {
                        showExplanation = true
                        if (selectedAnswer == currentQuestion.correctAnswer) {
                            score++
                        }
                    } else {
                        if (currentQuestionIndex < quiz.questions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswer = null
                            showExplanation = false
                        } else {
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF)
                )
            ) {
                Text(
                    if (!showExplanation) "Check Answer" 
                    else if (currentQuestionIndex < quiz.questions.size - 1) "Next Question"
                    else "Finish Quiz (Score: $score/${quiz.questions.size})",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    option: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showResult: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showResult && isCorrect -> Color(0xFF00E5FF).copy(alpha = 0.2f)
        showResult && isSelected && !isCorrect -> Color.Red.copy(alpha = 0.2f)
        isSelected -> Color(0xFF00E5FF).copy(alpha = 0.1f)
        else -> Color(0xFF1A2530)
    }
    
    val borderColor = when {
        showResult && isCorrect -> Color(0xFF00E5FF)
        showResult && isSelected && !isCorrect -> Color.Red
        isSelected -> Color(0xFF00E5FF)
        else -> Color.Transparent
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            if (showResult && isCorrect) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Correct",
                    tint = Color(0xFF00E5FF)
                )
            }
        }
    }
}
