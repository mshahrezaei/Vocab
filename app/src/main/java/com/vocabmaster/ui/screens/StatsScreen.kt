package com.vocabmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val db = AppDatabase.getInstance(LocalContext.current)
    val learned by db.wordDao().countLearned().collectAsState(initial = 0)
    val learning by db.wordDao().countLearning().collectAsState(initial = 0)
    val newWords by db.wordDao().countNew().collectAsState(initial = 0)
    val weak by db.wordDao().countWeak().collectAsState(initial = 0)
    val total = learned + learning + newWords

    var todayReviews by remember { mutableIntStateOf(0) }
    var weekReviews by remember { mutableIntStateOf(0) }
    var monthReviews by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        todayReviews = db.wordDao().getReviewsSince(now - 24*60*60*1000).size
        weekReviews = db.wordDao().getReviewsSince(now - 7*24*60*60*1000).size
        monthReviews = db.wordDao().getReviewsSince(now - 30*24*60*60*1000).size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF27AE60), titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("📊 Overall Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            Card(colors = CardDefaults.cardColors(containerColor = DeepNavy)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatBox("Total", total.toString(), SoftGold)
                        StatBox("Learned", learned.toString(), Color(0xFF98D8C8))
                        StatBox("Learning", learning.toString(), Color(0xFF87CEEB))
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { if (total > 0) learned.toFloat() / total else 0f },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = SoftGold,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Text("${if (total > 0) (learned * 100 / total) else 0}% Complete", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
                }
            }

            Text("🔥 Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivityCard("Today", todayReviews, Color(0xFFE74C3C), Modifier.weight(1f))
                ActivityCard("Week", weekReviews, Color(0xFFF39C12), Modifier.weight(1f))
                ActivityCard("Month", monthReviews, Color(0xFF27AE60), Modifier.weight(1f))
            }

            Text("⚠️ Needs Attention", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Weak Words", fontWeight = FontWeight.Bold, color = Color(0xFFC0392B))
                        Text("Words you've struggled with", fontSize = 13.sp, color = DarkText.copy(alpha = 0.7f))
                    }
                    Text(weak.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC0392B))
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun ActivityCard(period: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(period, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
        }
    }
}
