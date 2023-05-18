package com.ted.gartenhelferlein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.ted.gartenhelferlein.ui.MapScreen
import com.ted.gartenhelferlein.ui.theme.GartenhelferleinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyUI()
        }
    }
}

@Composable
fun MyUI() {
    GartenhelferleinTheme {
        MapScreen()
    }
}