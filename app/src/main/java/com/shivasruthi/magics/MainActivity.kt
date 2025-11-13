package com.shivasruthi.magics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.shivasruthi.magics.ui.theme.CreationNavGraph // Use your existing NavGraph
import com.shivasruthi.magics.ui.theme.MagicSTheme
import com.shivasruthi.magics.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagicSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This ensures your app uses the correct navigation file.
                    CreationNavGraph(sharedViewModel = sharedViewModel)
                }
            }
        }
    }
}