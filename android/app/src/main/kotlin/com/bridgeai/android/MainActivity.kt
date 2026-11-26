package com.bridgeai.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bridgeai.android.ui.Navigation
import com.bridgeai.android.ui.theme.BridgeAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BridgeAiTheme {
                Navigation(app = application as BridgeAiApp)
            }
        }
    }
}
