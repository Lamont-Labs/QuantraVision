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
import com.lamontlabs.quantravision.education.LessonRepository
import com.lamontlabs.quantravision.education.model.Lesson
import com.lamontlabs.quantravision.education.model.Quiz

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
                    title = { 
                        Text(
                            "Education Hub",
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = CyanGlowShadow
                            )
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = ElectricCyan
                    )
                )
            },
            containerColor = DeepNavyBackground
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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Master Chart Pattern Recognition",
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = CyanGlowShadow
                ),
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Complete 25 interactive lessons to earn your certification",
                style = MaterialTheme.typography.bodyMedium,
                color = MetallicSilver
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        val categories = lessons.groupBy { it.category }
        categories.forEach { (category, categoryLessons) ->
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricCyan,
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
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ElectricCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ElectricCyan
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
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${lesson.duration} • ${lesson.quiz.questions.size} quiz questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetallicSilver
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
                    title = { 
                        Text(
                            lesson.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = CyanGlowShadow
                            )
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = ElectricCyan
                    )
                )
            },
            containerColor = DeepNavyBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp)
                ) {
                    item {
                        Text(
                            text = lesson.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = CrispWhite
                        )
                    }
                }
                
                Button(
                    onClick = { showQuiz = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan
                    )
                ) {
                    Text("Take Quiz", color = DeepNavyBackground)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizScreen(
    quiz: Quiz,
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
                title = { 
                    Text(
                        "Quiz: $lessonTitle",
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = CyanGlowShadow
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = ElectricCyan
                )
            )
        },
        containerColor = DeepNavyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1) / quiz.questions.size.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = ElectricCyan
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MetallicSilver
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.titleLarge,
                color = CrispWhite,
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
                        containerColor = DarkSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = currentQuestion.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrispWhite,
                        modifier = Modifier.padding(24.dp)
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
                    containerColor = ElectricCyan
                )
            ) {
                Text(
                    if (!showExplanation) "Check Answer" 
                    else if (currentQuestionIndex < quiz.questions.size - 1) "Next Question"
                    else "Finish Quiz (Score: $score/${quiz.questions.size})",
                    color = DeepNavyBackground
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
        showResult && isCorrect -> ElectricCyan.copy(alpha = 0.2f)
        showResult && isSelected && !isCorrect -> NeonRed.copy(alpha = 0.2f)
        isSelected -> ElectricCyan.copy(alpha = 0.1f)
        else -> DarkSurface
    }
    
    val borderColor = when {
        showResult && isCorrect -> ElectricCyan
        showResult && isSelected && !isCorrect -> NeonRed
        isSelected -> ElectricCyan
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
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                color = CrispWhite,
                modifier = Modifier.weight(1f)
            )
            
            if (showResult && isCorrect) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Correct",
                    tint = ElectricCyan
                )
            }
        }
    }
}
