package com.vocabmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vocabmaster.data.db.AppDatabase
import com.vocabmaster.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeakWordsScreen(navController: NavController) {
    val db = AppDatabase.getInstance(LocalContext.current)
    val weakWords by db.wordDao().getWeakWords().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weak Words (${weakWords.size})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC0392B), titleContentColor = Color.White)
            )
        }
    ) { padding ->
        if (weakWords.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("🎉 No weak words! Great job!", fontSize = 18.sp, color = DeepNavy)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weakWords) { word ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFC0392B), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(word.term, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepNavy)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(word.meaning, color = DarkText.copy(alpha = 0.8f))
                            Spacer(Modifier.height(6.dp))
                            Text("❌ Wrong: ${word.wrongCount} times", fontSize = 12.sp, color = Color(0xFFC0392B), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
