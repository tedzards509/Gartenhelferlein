package com.ted.gartenhelferlein.ui

import androidx.annotation.Size
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ted.gartenhelferlein.task.*
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneOffset

// TODO: Backend
// TODO: Sort by urgency
// TODO: Add new tasks
// TODO: Edit tasks

@Composable
fun TasksScreen(tasks: MutableList<TaskData>, onComplete: (Int) -> Unit = {}) {
    println("Drawing tasks screen")
    Surface {
        Column {
            tasks.sortByDescending { data -> data.urgency() }
            for (task in tasks) {
                TaskItem(taskData = task, onComplete = { onComplete(task.id) })
            }
        }
    }
}

@Composable
fun TaskItem(modifier: Modifier = Modifier, taskData: TaskData, onComplete: () -> Unit) {
    Box (modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        val expanded = remember { mutableStateOf(false) }
        val lastCompletedText = remember { mutableStateOf(taskData.printLastCompleted()) }
        val frequencyText = remember { mutableStateOf(taskData.printFrequency()) }
        val urgencyProgress = remember { mutableStateOf(taskData.urgency()) }

        ExpandableCard(expanded = expanded,
            Title = {
                Box(
                    modifier = modifier
                        .padding(16.dp)
                    , contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                taskData.complete()
                                lastCompletedText.value = taskData.printLastCompleted()
                                urgencyProgress.value = taskData.urgency()
                                onComplete()
                            }) {
                                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Complete Task")
                            }
                            TaskProgressIndicator(taskData = taskData, progress = urgencyProgress)
                        }
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = taskData.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            Content = {
                LaunchedEffect(key1 = taskData) {
                    while (true) {
                        delay(1000)
                        lastCompletedText.value = taskData.printLastCompleted()
                    }
                }
                Box(
                    modifier = modifier
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .fillMaxWidth(), contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = taskData.description,
                            fontSize = 16.sp,
                            maxLines = if (expanded.value) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row (horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier
                                .padding(0.dp, 16.dp, 8.dp, 4.dp)) {
                                Text(
                                    text = frequencyText.value,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Box(modifier = Modifier
                                .padding(8.dp, 16.dp, 0.dp, 4.dp)) {
                                Text(
                                    text = lastCompletedText.value,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            onExpand = {
                expanded.value = !expanded.value
                lastCompletedText.value = taskData.printLastCompleted()
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    expanded: MutableState<Boolean>,
    Title: @Composable () -> Unit,
    Content: @Composable () -> Unit,
    onExpand: () -> Unit = {}){
    val expandButtonRotation = animateFloatAsState(targetValue = if (expanded.value) 180f else 0f)
    Card(
        onClick = { onExpand() },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Title()
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .rotate(expandButtonRotation.value),
                    onClick = {
                        onExpand()
                    }) {
                    Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = "Expand Arrow")
                }
            }
            if (expanded.value) {
                Content()
            }
        }
    }
}

@Composable
fun TaskProgressIndicator(taskData: TaskData, progress: MutableState<Float> = remember { mutableStateOf(taskData.urgency()) }){
    LaunchedEffect(key1 = taskData) {
        while (true) {
            progress.value = taskData.urgency()
            delay(1000)
        }
    }
    CircularProgressIndicator(
        strokeCap = StrokeCap.Round,
        modifier = Modifier
            .size(24.dp),
        progress = progress.value,
        color = urgencyColor(progress.value))
}

fun urgencyColor(urgency: Float): Color {
    val colors = listOf(
        Color(0xFF69BE69),
        Color(0xFFFFE176),
        Color(0xFF8F3C3C)
    )
    return when (urgency) {
        in 0f..0.5f -> {
            colors[0]
        }
        in 0.5f..0.8f -> {
            lerp(colors[0], colors[1], (urgency - 0.5f) / 0.3f)
        }
        in 0.8f..1.4f -> {
            lerp(colors[1], colors[2], (urgency - 0.8f) / 0.6f)
        }
        else -> {
            colors[2]
        }
    }
}

@Composable
fun MessageComposerTask(
    path: String,
    messageClient: MessageClient,
    modifier: Modifier = Modifier,
    cudStrings: List<String> = listOf("Create", "Update", "Delete"),
    typeString: String = "Type",
    defaultID: Int? = null,
    @Size(2) defaultInputs: List<String> = listOf("", ""),
    onSendMessage: () -> Unit = {},
) {
    val messageType = remember { mutableStateOf(cudStrings[1]) }
    val messageId = if (defaultID != null) remember { mutableStateOf( defaultID.toString() ) } else remember { mutableStateOf( "" ) }
    val userName = remember { mutableStateOf(defaultInputs[0]) }
    val taskDescription = remember { mutableStateOf(defaultInputs[1]) }
    val frequencyDays = remember { mutableStateOf("1") }
    val frequencyHours = remember { mutableStateOf("") }

    LazyColumn {
        item {

            Column(Modifier.padding(vertical = 16.dp)) {
                M3OutlinedExposedDropdownMenu(
                    modifier = modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    items = cudStrings,
                    selectedItem = messageType.value,
                    label = { Text(typeString) },
                    itemIcons = listOf(Icons.Rounded.Add, Icons.Rounded.Edit, Icons.Rounded.Delete),
                ) { messageType.value = it }

                OutlinedTextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    value = messageId.value,
                    onValueChange = { newValue: String -> messageId.value = newValue },
                    enabled = messageType.value != cudStrings[0] && defaultID == null,
                    label = { Text("ID") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    value = userName.value,
                    onValueChange = { newValue: String -> userName.value = newValue },
                    enabled = messageType.value != cudStrings[2],
                    label = { Text("Name") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    value = taskDescription.value,
                    onValueChange = { newValue: String -> taskDescription.value = newValue },
                    enabled = messageType.value != cudStrings[2],
                    label = { Text("Beschreibung") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = frequencyDays.value,
                            onValueChange = { newValue: String -> frequencyDays.value = newValue },
                            enabled = messageType.value != cudStrings[2],
                            label = { Text("Tage") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = frequencyHours.value,
                            onValueChange = { newValue: String -> frequencyHours.value = newValue },
                            enabled = messageType.value != cudStrings[2],
                            label = { Text("Stunden") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = {
                        onSendMessage()
                        var message = "{}"
                        val messageHours =
                            if (frequencyHours.value.toLongOrNull() == null) 0 else frequencyHours.value.toLong()
                        val messageDays =
                            if (frequencyDays.value.toLongOrNull() == null) 0 else frequencyDays.value.toLong()
                        val frequencyMinutes = (messageHours * 60) + (messageDays * 24 * 60)
                        when (cudStrings.indexOf(messageType.value)) {
                            0 -> { // Create
                                val task = Task(
                                    name = userName.value,
                                    description = taskDescription.value,
                                    frequency = frequencyMinutes,
                                    lastCompleted = java.time.LocalDateTime.now()
                                        .toEpochSecond(ZoneOffset.UTC)
                                )
                                message = Json.encodeToString(
                                    SendCreateMessage(
                                        type = "create",
                                        path = path,
                                        data = task
                                    )
                                )
                            }

                            1 -> { // Update
                                val task = Task(
                                    name = userName.value,
                                    description = taskDescription.value,
                                    frequency = frequencyMinutes,
                                    lastCompleted = 123456789
                                )
                                message =
                                    Json.encodeToString(messageId.value.toIntOrNull()?.let { id ->
                                        SendUpdateMessage(
                                            type = "update",
                                            path = path,
                                            id = id,
                                            data = task
                                        )
                                    })
                            }

                            2 -> { // Delete
                                message = Json.encodeToString(
                                    SendDeleteMessage(
                                        type = "delete",
                                        path = path,
                                        id = messageId.value.toInt()
                                    )
                                )
                            }
                        }
                        messageClient.send(message)
                    }
                ) {
                    Text("Send Message")
                }
            }
        }
    }
}

@Composable
fun M3OutlinedExposedDropdownMenu(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedItem: String,
    label: @Composable () -> Unit = {},
    itemIcons: List<ImageVector?> = emptyList(),
    onItemSelected: (String) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = label,
            trailingIcon = {
                IconButton(
                    onClick = { expanded.value = !expanded.value }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            },
            readOnly = true,
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    onClick = {
                        onItemSelected(item)
                        expanded.value = false
                    },
                    text = {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = { itemIcons.getOrNull(index)?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    }
                    }
                )
            }
        }
    }
}
