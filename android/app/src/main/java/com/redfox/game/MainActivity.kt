package com.redfox.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.redfox.game.ui.navigation.RedFoxNavGraph
import com.redfox.game.ui.theme.RedFoxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedFoxTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RedFoxNavGraph()
                }
            }
        }
    }
}
