package com.vocabmaster.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Assessment // اضافه شده برای اطمینان
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun HomeScreen(navController: NavController) {
    val db = AppDatabase.getInstance(LocalContext.current)
    val learned by db.wordDao().countLearned().collectAsState(initial = 0)
    val learning by db.wordDao().countLearning().collectAsState(initial = 0)
    val newWords by db.wordDao().countNew().collectAsState(initial = 0)
    val weak by db.wordDao().countWeak().collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VocabMaster", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DeepNavy)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Your Progress", color = SoftGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("Learned", learned.toString(), SoftGold)
                        StatItem("Learning", learning.toString(), Color(0xFF87CEEB))
                        StatItem("New", newWords.toString(), Color(0xFF98D8C8))
                        StatItem("Weak", weak.toString(), Color(0xFFFF9999))
                    }
                }
            }

            Button(
                onClick = { navController.navigate(Routes.REVIEW_TODAY) },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = DeepNavy)
                Spacer(Modifier.width(8.dp))
                Text("Start Today's Review", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuCard(Icons.Default.List, "Lessons", "Browse all", BurntBrown, Modifier.weight(1f)) {
                    navController.navigate(Routes.LESSONS)
                }
                MenuCard(Icons.Default.Search, "Search", "Find words", DeepNavy, Modifier.weight(1f)) {
                    navController.navigate(Routes.SEARCH)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MenuCard(Icons.Default.Warning, "Weak Words", "$weak to review", Color(0xFFC0392B), Modifier.weight(1f)) {
                    navController.navigate(Routes.WEAK_WORDS)
                }
                MenuCard(Icons.Filled.Assessment, "Statistics", "Your progress", Color(0xFF27AE60), Modifier.weight(1f)) {
                    navController.navigate(Routes.STATS)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun MenuCard(icon: ImageVector, title: String, subtitle: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(110.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}
