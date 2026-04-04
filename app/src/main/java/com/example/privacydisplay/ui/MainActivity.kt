package com.example.privacydisplay.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.privacydisplay.R
import com.example.privacydisplay.RotationService.DemoAreaBckCalc
import com.example.privacydisplay.RotationService.RotationService
import com.example.privacydisplay.ui.components.ActionButton
import com.example.privacydisplay.ui.components.DemoArea
import com.example.privacydisplay.ui.theme.PrivacyDisplayTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = false

        setContent {
            PrivacyDisplayTheme {
                var rotationData by remember { mutableStateOf(Triple(0f, 0f, 0f)) }

                var isDynamic by remember { mutableStateOf(false) }
                var wbBalance by remember { mutableStateOf(1f) }

                val context = LocalContext.current

                val demoCalc = remember {
                    DemoAreaBckCalc(context) { balance ->
                        wbBalance = balance
                    }
                }

                DisposableEffect(Unit) {
                    demoCalc.register()
                    context.startService(Intent(context, RotationService::class.java))

                    val magnetReceiver = object : BroadcastReceiver() {
                        override fun onReceive(ctx: Context?, intent: Intent?) {
                            if (intent?.action == RotationService.ACTION_ROTATION_DATA) {
                                val pitch = intent.getFloatExtra(RotationService.PITCH, 0f)
                                val yaw   = intent.getFloatExtra(RotationService.YAW,   0f)
                                val roll  = intent.getFloatExtra(RotationService.ROLL,  0f)
                                rotationData = Triple(pitch, yaw, roll)
                            }
                        }
                    }
                    ContextCompat.registerReceiver(
                        context,
                        magnetReceiver,
                        IntentFilter(RotationService.ACTION_ROTATION_DATA),
                        ContextCompat.RECEIVER_EXPORTED
                    )

                    onDispose {
                        demoCalc.unregister()
                        context.unregisterReceiver(magnetReceiver)
                        context.stopService(Intent(context, RotationService::class.java))
                    }
                }

                Scaffold { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        DemoArea(wbBalance)

                        Spacer(modifier = Modifier.height(50.dp))

                        val (pitch, yaw, roll) = if (isDynamic) rotationData else Triple(0f, 0f, 0f)

                        Text(text = "Pitch: %.1f".format(pitch), color = Color.Black)
                        Text(text = "Yaw:   %.1f".format(yaw),   color = Color.Black)
                        Text(text = "Roll:  %.1f".format(roll),   color = Color.Black)


                        Spacer(modifier = Modifier.height(50.dp))

                        Text(
                            text = "This is just a privacy screen demo, it has no reality!",
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ActionButton(
                            switchPadding = 5.dp,
                            buttonWidth = 80.dp,
                            buttonHeight = 50.dp,
                            value = isDynamic,
                            onToggle = {
                                isDynamic = !isDynamic
                                demoCalc.setDynamic(isDynamic)
                            }
                        )
                    }
                }
            }
        }
    }
}