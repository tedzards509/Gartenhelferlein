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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ted.gartenhelferlein.task.*
import com.ted.gartenhelferlein.ui.*
import com.ted.gartenhelferlein.ui.theme.GartenhelferleinTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity(), MessageClient.MessageListener {
    private lateinit var messageClient: MessageClient
    private val tasks = MutableStateFlow<List<TaskData>>(emptyList())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyUI()
        }
    }

    override fun onResume() {
        super.onResume()
        messageClient = MessageClient(url = getString(R.string.server_url)) // No snooping
        messageClient.setMessageListener(this)
        messageClient.connect()
    }


    override fun onMessage(message: String) {
        try {
            val receivedMessage = Json.decodeFromString<ReceivedMessage>(message)
            if (receivedMessage.type == "read" && receivedMessage.data != null) {
                tasks.value = receivedMessage.data.map { it.toTaskData() }.sortedBy { it.urgency() }
            } else {
                messageClient.send(Json.encodeToString(SendReadMessage(type = "read", path = "tasks")))
            }
        } catch (e: Exception) {
            messageClient.send(Json.encodeToString(SendReadMessage(type = "read", path = "tasks")))
        }
    }

    override fun onConnect() {
        messageClient.send(Json.encodeToString(SendReadMessage(type = "read", path = "tasks")))
    }

    override fun onFailToConnect() {
        TODO("Not yet implemented")
    }

    override fun onPause() {
        super.onPause()
        messageClient.removeMessageListener()
        messageClient.disconnect()
        super.onPause()
    }

    // UI Stuff
    @Composable
    fun MyUI() {
        GartenhelferleinTheme {
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
        val isNewTaskDialogOpen = remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 128.dp,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        title = { Text(getString(R.string.app_name)) },
                        actions = {
                            IconButton(onClick = {
                                // isNewTaskDialogOpen.value = true
                                messageClient.send(Json.encodeToString(SendReadMessage(type = "read", path = "tasks")))
                            }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Menu")
                            }
                        }
                    )
                },
                sheetShadowElevation = 4.dp,
                sheetContent = {SheetTasksContent(scope, scaffoldState)},
            ) {
                Box(modifier = Modifier.padding(it)) {
                    MapScreen()
                }
            }
            EditTaskDialog(showDialog = isNewTaskDialogOpen)
        }
    }

    @Composable fun EditTaskDialog(
        showDialog: MutableState<Boolean>,
        // task: Task? = null // TODO: Long Press with Autofill (-> remove edit and delete from MessageComposerTask)
    ) {
        val dialogOpen = remember { showDialog }
        if (dialogOpen.value) {
            Dialog(
                onDismissRequest = { dialogOpen.value = false }
            ) {
                Surface(shape = MaterialTheme.shapes.medium) {
                    MessageComposerTask(
                        path = "tasks",
                        messageClient = messageClient,
                        cudStrings = listOf("Neuer Eintrag", "Bearbeiten", "Löschen"),
                        typeString = "Art der Änderung",
                        onSendMessage = { dialogOpen.value = false }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SheetTasksContent(
        scope: CoroutineScope,
        scaffoldState: BottomSheetScaffoldState
    ) {
        println("Updated UI Task List")
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
                TasksScreen(tasks = tasks, onComplete = { id ->
                    val sendTask = tasks.value.first { it.id == id }.toTask()
                    messageClient.send(
                        Json.encodeToString(
                            SendUpdateMessage(
                                type = "update",
                                path = "tasks",
                                id = id,
                                data = sendTask
                            )
                        )
                    )
                })
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
    }
}