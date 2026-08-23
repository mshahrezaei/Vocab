package com.vocabmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vocabmaster.ui.navigation.NavGraph
import com.vocabmaster.ui.theme.VocabMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VocabMasterTheme {
                NavGraph()
            }
        }
    }
}
