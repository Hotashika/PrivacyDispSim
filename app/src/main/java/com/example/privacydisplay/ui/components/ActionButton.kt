package com.example.privacydisplay.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.privacydisplay.RotationService.RotationService

@Composable
fun ActionButton(
    switchPadding: Dp,
    buttonWidth: Dp,
    buttonHeight: Dp,
    value: Boolean,
    onToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val view = LocalView.current


    val switchSize by remember {
        mutableStateOf(buttonHeight-switchPadding*2)
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    var switchClicked by rememberSaveable {
        mutableStateOf(value)
    }

    var padding by remember {
        mutableStateOf(0.dp)
    }

    padding = if (switchClicked) buttonWidth-switchSize-switchPadding*2 else 0.dp

    val animateSize by animateDpAsState(
        targetValue = if (switchClicked) padding else 0.dp,
        tween(
            durationMillis = 700,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        )
    )

    Box(
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .clip(CircleShape)
            .background(if (switchClicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                switchClicked = !switchClicked
                if (switchClicked) {
                    val intent = Intent(RotationService.ACTION_RECALIBRATE).apply {
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(intent)
                }

                onToggle(switchClicked)
            }
    ){
        Row(modifier = Modifier.fillMaxSize().padding(switchPadding)) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(animateSize)
                    .background(Color.Transparent)
            )

            Box(modifier = Modifier
                .size(switchSize)
                .clip(CircleShape)
                .background(Color.White))

        }
    }
}

@Composable
@Preview
fun ActionButtonPreview(
    switchPadding: Dp = 8.dp,
    buttonWidth: Dp = 120.dp,
    buttonHeight: Dp = 48.dp,
    value: Boolean = false
) {
    ActionButton(
        switchPadding = switchPadding,
        buttonWidth = buttonWidth,
        buttonHeight = buttonHeight,
        value = value
    )
}