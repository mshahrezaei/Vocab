package com.vocabmaster.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vocabmaster.algorithm.SpacedRepetition
import com.vocabmaster.data.db.AppDatabase
import com.vocabmaster.data.db.entities.Word
import com.vocabmaster.ui.theme.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(lessonId: Long, navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val words = remember { mutableStateListOf<Word>() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var sessionCorrect by remember { mutableIntStateOf(0) }
    var sessionTotal by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(lessonId) {
        val list = if (lessonId == -1L) {
            db.wordDao().getDueWords(System.currentTimeMillis(), limit = 30)
        } else {
            var result = emptyList<Word>()
            db.wordDao().getWordsByLesson(lessonId).collect { allWords ->
                result = allWords.filter { !it.isLearned || it.nextReviewDate <= System.currentTimeMillis() }.take(30)
            }
            result
        }
        words.addAll(list)
        loaded = true
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DeepNavy)
        }
        return
    }

    if (isFinished) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎉 Session Complete!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            Spacer(Modifier.height(24.dp))
            Card(colors = CardDefaults.cardColors(containerColor = SoftGold)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$sessionCorrect / $sessionTotal", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    Text("Correct Answers", color = DeepNavy)
                    Spacer(Modifier.height(8.dp))
                    val percentage = if (sessionTotal > 0) (sessionCorrect * 100 / sessionTotal) else 0
                    Text("$percentage% Accuracy", fontSize = 18.sp, color = DeepNavy)
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(onClick = { navController.popBackStack() }) { 
                Text("Back to Home") 
            }
        }
        return
    }

    if (words.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No words due for review 🎊", fontSize = 18.sp, color = DeepNavy)
        }
        return
    }

    val currentWord = words.getOrNull(currentIndex) ?: run {
        isFinished = true
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${currentIndex + 1} / ${words.size}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).clickable { isFlipped = !isFlipped },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isFlipped) SoftGold else Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!isFlipped) {
                        Text(currentWord.term, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DeepNavy, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text("Tap to reveal meaning", fontSize = 14.sp, color = BurntBrown)
                    } else {
                        Text(currentWord.meaning, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepNavy, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        if (currentWord.example1.isNotBlank()) {
                            Text("\"${currentWord.example1}\"", fontSize = 15.sp, color = DarkText.copy(alpha = 0.8f), textAlign = TextAlign.Center, fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (isFlipped) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("How well did you know it?", fontWeight = FontWeight.Medium, color = DeepNavy)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RatingButton("Again", Color(0xFFE74C3C)) {
                            processRating(currentWord, 1, db, words, currentIndex, 
                                onCorrect = { sessionCorrect++ }, 
                                onTotal = { sessionTotal++ }, 
                                onNext = { isFlipped = false; currentIndex++ }, 
                                onFinish = { isFinished = true }
                            )
                        }
                        RatingButton("Hard", Color(0xFFF39C12)) {
                            processRating(currentWord, 3, db, words, currentIndex, 
                                onCorrect = { sessionCorrect++ }, 
                                onTotal = { sessionTotal++ }, 
                                onNext = { isFlipped = false; currentIndex++ }, 
                                onFinish = { isFinished = true }
                            )
                        }
                        RatingButton("Good", Color(0xFF27AE60)) {
                            processRating(currentWord, 4, db, words, currentIndex, 
                                onCorrect = { sessionCorrect++ }, 
                                onTotal = { sessionTotal++ }, 
                                onNext = { isFlipped = false; currentIndex++ }, 
                                onFinish = { isFinished = true }
                            )
                        }
                        RatingButton("Easy", Color(0xFF1E3A5F)) {
                            processRating(currentWord, 5, db, words, currentIndex, 
                                onCorrect = { sessionCorrect++ }, 
                                onTotal = { sessionTotal++ }, 
                                onNext = { isFlipped = false; currentIndex++ }, 
                                onFinish = { isFinished = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.RatingButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

private fun processRating(
    word: Word, 
    quality: Int, 
    db: AppDatabase, 
    words: MutableList<Word>,
    currentIndex: Int, 
    onCorrect: () -> Unit, 
    onTotal: () -> Unit, 
    onNext: () -> Unit, 
    onFinish: () -> Unit
) {
    GlobalScope.launch {
        onTotal()
        if (quality >= 3) onCorrect()
        
        val result = SpacedRepetition.processReview(word, quality)
        db.wordDao().updateWord(result.updatedWord)
        db.wordDao().insertLog(result.log)
        
        if (quality < 3) {
            words.add(result.updatedWord)
        }
    }
    
    if (currentIndex + 1 >= words.size - 1) {
        onFinish()
    } else {
        onNext()
    }
}
