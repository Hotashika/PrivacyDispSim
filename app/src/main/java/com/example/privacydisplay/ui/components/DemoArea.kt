package com.example.privacydisplay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DemoArea(
    grayScale: Float,
    cornerRadius: Dp = 16.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .aspectRatio(4f / 3f)
            .background(
                color = Color(1f - grayScale, 1f - grayScale, 1f - grayScale),
                shape = RoundedCornerShape(cornerRadius)
            )
    )
}


@Composable
@Preview(showBackground = true)
fun DemoAreaPreview() {
    DemoArea(0.2f)
}