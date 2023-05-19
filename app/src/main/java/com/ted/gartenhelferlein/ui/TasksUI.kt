package com.ted.gartenhelferlein.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ted.gartenhelferlein.Task
import kotlinx.coroutines.delay
import kotlin.math.pow

// TODO: Backend
// TODO: Sort by urgency
// TODO: Add new tasks
// TODO: Edit tasks

var tasks = listOf(
    Task(
        id = 1,
        name = "Lipsum",
        description = "Lorem Ipsum Dolor Sit Amet Consectetur Adipiscing Elit Sed Do Eiusmod Tempor Incididunt Ut Labore Et Dolore Magna Aliqua Ut Enim Ad Minim Veniam Quis Nostrud Exercitation Ullamco",
        frequency = java.time.Duration.ofMinutes(2),
        lastCompletion = java.time.LocalDateTime.now().minusDays(1)
    ), Task(
        id = 2,
        name = "Rasen mähen",
        description = "Rasenmäher im Schuppen. Bei Fahrrädern, hinterm Haus und vorm Wintergarten richtung Zufahrt.",
        frequency = java.time.Duration.ofDays(7),
        lastCompletion = java.time.LocalDateTime.now().minusDays(6)
    ), Task(
        id = 3,
        name = "Rasensprenger",
        description = "1: Vorgarten (Automatisch)\n" +
                "2: Hinterm Haus (Automatisch)\n" +
                "3: Tröpfchen (Außer hinten, Automatisch)",
        frequency = java.time.Duration.ofDays(1),
        lastCompletion = java.time.LocalDateTime.now().minusDays(1)
    )
)

@Composable
fun TasksScreen() {
    Surface {
        Column {
            for (task in tasks) {
                TaskItem(task = task)
            }
        }
    }
}

@Composable
fun TaskItem(modifier: Modifier = Modifier, task: Task) {
    Box (modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        val expanded = remember { mutableStateOf(false) }
        val lastCompletedText = remember { mutableStateOf(task.printLastCompleted()) }
        val frequencyText = remember { mutableStateOf(task.printFrequency()) }

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
                        IconButton(onClick = {task.complete(); lastCompletedText.value = task.printLastCompleted()}) {
                            Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Complete Task")
                        }
                        TaskProgressIndicator(task = task)
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = task.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        Content = {
            LaunchedEffect(key1 = task) {
                while (true) {
                    delay(1000)
                    lastCompletedText.value = task.printLastCompleted()
                    println("updated lastCompletedText")
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
                        text = task.description,
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
            lastCompletedText.value = task.printLastCompleted()
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
fun TaskProgressIndicator(task: Task) {
    val progress = remember { mutableStateOf(task.urgency()) }
    LaunchedEffect(key1 = task) {
        while (true) {
            progress.value = task.urgency()
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
        Color(0xFF884A4A)
    )
    return when (urgency) {
        in 0.0f..0.7f -> {
            lerp(colors[0], colors[1], urgency*(0.7.pow(-1)).toFloat())
        }
        in 0.7f..1.3f -> {
            lerp(colors[1], colors[2], (urgency - 0.7f)*(0.6.pow(-1)).toFloat())
        }
        else -> {
            colors[2]
        }
    }
}