package com.ted.gartenhelferlein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ted.gartenhelferlein.ui.MapScreen
import com.ted.gartenhelferlein.ui.TasksScreen
import com.ted.gartenhelferlein.ui.theme.GartenhelferleinTheme
import kotlinx.coroutines.launch

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
    GartenhelferleinTheme () {
        Surface(color = MaterialTheme.colorScheme.background) {
            BottomSheetWithMap()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetWithMap() {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 128.dp,
            sheetContent = {
                LazyColumn {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(80.dp) // 128 - (2 * 22 + 4)
                                .padding(16.dp, 0.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("Aufgaben:", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    item {
                        TasksScreen()
                    }
                    item {
                        Spacer(Modifier.height(20.dp))
                        FilledTonalButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            onClick = {
                                scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                            }
                        ) {
                            Text("Zurück zur Karte")
                        }
                    }
                }
            },
        ) {
            MapScreen()
        }
    }
}