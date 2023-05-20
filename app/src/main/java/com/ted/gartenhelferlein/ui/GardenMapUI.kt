package com.ted.gartenhelferlein.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ted.gartenhelferlein.R


const val startZoom = 2f
val zoomRange = (0.8f..5f)
val startTranslate = Offset(350f, -120f)

@Composable
fun MapScreen() {
    val scale = remember { mutableStateOf(startZoom) }
    val translation = remember { mutableStateOf(startTranslate) }
    Box{
        DrawMap(scale, translation)
        ResetButton(modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp),
            setTranslation = translation::value::set,
            setScale = scale::value::set)
    }
}

@Composable
fun ResetButton(modifier: Modifier, setTranslation: (Offset) -> Unit, setScale: (Float) -> Unit) {
    Button(onClick = {setTranslation(startTranslate); setScale(startZoom)}, modifier = modifier) {
        Text(text = "Reset Map")
    }
}

@Composable
fun DrawMap(scale: MutableState<Float>, translation: MutableState<Offset>) {
    val middle = remember {
        mutableStateOf(Offset.Zero)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged {
            middle.value = Offset(it.width / 2f, it.height / 2f)
        }
        .pointerInput(Unit) {
            detectTransformGestures { centroid, pan, zoom, _ ->
                val adjust = (centroid - (middle.value + translation.value)) * (1f - zoom)
                if (zoom * scale.value > zoomRange.endInclusive) {
                    scale.value = zoomRange.endInclusive
                    translation.value += pan
                } else if (zoom * scale.value < zoomRange.start) {
                    scale.value = zoomRange.start
                    translation.value += pan
                } else {
                    scale.value *= zoom
                    translation.value += pan + adjust
                }
            }
        }) {
        Image(imageVector = ImageVector.vectorResource(id = R.drawable.garten_map),
            contentDescription = "Garden Map",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = translation.value.x,
                    translationY = translation.value.y
                )
                .clipToBounds()
        )
    }
}
