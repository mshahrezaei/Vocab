package com.vocabmaster.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vocabmaster.data.db.AppDatabase
import com.vocabmaster.ui.navigation.Routes
import com.vocabmaster.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(navController: NavController) {
    val db = AppDatabase.getInstance(LocalContext.current)
    val lessons by db.lessonDao().getAllLessons().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lessons") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BurntBrown, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            items(lessons) { lesson ->
                var learnedCount by remember { mutableIntStateOf(0) }
                LaunchedEffect(lesson.id) {
                    db.lessonDao().countLearnedInLesson(lesson.id).collect { learnedCount = it }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                        navController.navigate(Routes.flashcard(lesson.id))
                    }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(lesson.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepNavy)
                        Text("${lesson.sourceFile} • ${lesson.type}", fontSize = 12.sp, color = DarkText.copy(alpha = 0.7f))
                        Spacer(Modifier.height(8.dp))
                        Text("${lesson.totalWords} words • $learnedCount learned", fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { if (lesson.totalWords > 0) learnedCount.toFloat() / lesson.totalWords else 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = SoftGold
                        )
                    }
                }
            }
        }
    }
}
