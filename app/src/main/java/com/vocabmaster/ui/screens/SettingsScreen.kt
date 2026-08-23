package com.vocabmaster.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Upload // اضافه شده برای اطمینان
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
import com.vocabmaster.data.db.entities.AppSettings
import com.vocabmaster.data.parser.ExcelParser
import com.vocabmaster.notification.ReminderManager
import com.vocabmaster.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance(context)
    val reminderManager = remember { ReminderManager(context) }

    var settings by remember { mutableStateOf(AppSettings()) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isLoading = true
                try {
                    val parsed = withContext(Dispatchers.IO) { ExcelParser(context).parse(it) }
                    for (pl in parsed) {
                        val lessonId = db.lessonDao().insertLesson(pl.lesson)
                        val wordsWithLesson = pl.words.map { w -> w.copy(lessonId = lessonId) }
                        db.wordDao().insertWords(wordsWithLesson)
                    }
                    message = "✅ Imported ${parsed.sumOf { it.words.size }} words from ${parsed.size} sheet(s)"
                } catch (e: Exception) {
                    message = "❌ Error: ${e.message}"
                }
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        settings = db.settingsDao().getSettings() ?: AppSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("📥 Import Vocabulary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select an Excel file (.xlsx) to import")
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { filePicker.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                        else {
                            Icon(Icons.Filled.Upload, null) // اصلاح شد
                            Spacer(Modifier.width(8.dp))
                            Text("Choose Excel File")
                        }
                    }
                    message?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = if (it.startsWith("✅")) Color(0xFF27AE60) else Color(0xFFE74C3C))
                    }
                }
            }

            Text("⏰ Daily Reminder", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Reminder")
                        Switch(
                            checked = settings.reminderEnabled,
                            onCheckedChange = {
                                settings = settings.copy(reminderEnabled = it)
                                scope.launch { db.settingsDao().saveSettings(settings) }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = DeepNavy)
                        )
                    }

                    if (settings.reminderEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Text("Time: ${String.format("%02d:%02d", settings.reminderHour, settings.reminderMinute)}")
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    settings = settings.copy(reminderHour = (settings.reminderHour + 1) % 24)
                                    scope.launch { db.settingsDao().saveSettings(settings) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGold)
                            ) { Text("Hour +") }
                            Button(
                                onClick = {
                                    settings = settings.copy(reminderMinute = (settings.reminderMinute + 15) % 60)
                                    scope.launch { db.settingsDao().saveSettings(settings) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGold)
                            ) { Text("Minute +") }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text("Days of Week:", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        val days = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
                        val selectedDays = settings.reminderDays.split(",").map { it.toInt() }.toSet()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            days.forEachIndexed { index, day ->
                                val dayNum = index + 1
                                FilterChip(
                                    selected = dayNum in selectedDays,
                                    onClick = {
                                        val newDays = if (dayNum in selectedDays) selectedDays - dayNum else selectedDays + dayNum
                                        settings = settings.copy(reminderDays = newDays.sorted().joinToString(","))
                                        scope.launch { db.settingsDao().saveSettings(settings) }
                                    },
                                    label = { Text(day, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val daysInt = settings.reminderDays.split(",").map { it.toInt() }
                                reminderManager.scheduleReminder(settings.reminderHour, settings.reminderMinute, daysInt)
                                message = "✅ Reminder scheduled!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                        ) { Text("Save Reminder") }
                    }
                }
            }
        }
    }
}
