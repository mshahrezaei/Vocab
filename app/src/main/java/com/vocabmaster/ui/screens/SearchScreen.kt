package com.vocabmaster.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vocabmaster.data.db.AppDatabase
import com.vocabmaster.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val db = AppDatabase.getInstance(LocalContext.current)
    var query by remember { mutableStateOf("") }
    val results by db.wordDao().searchWords(query).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                placeholder = { Text("Search word or meaning...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                items(results) { word ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(word.term, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Text(word.meaning, color = DarkText.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}
