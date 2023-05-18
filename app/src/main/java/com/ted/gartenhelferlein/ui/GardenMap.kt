package com.ted.gartenhelferlein.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import com.ted.gartenhelferlein.R

@Composable
fun MapScreen() {
    val scale = remember { mutableStateOf(1f) }
    val translation = remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale.value *= zoom
                    translation.value += pan
                }
            }
    ) {
        Image(
            painter = rememberVectorPainter(
                image = ImageVector.vectorResource(id = R.drawable.garden_map)
            ),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = translation.value.x,
                    translationY = translation.value.y
                )
                .fillMaxSize()
        )
    }
}