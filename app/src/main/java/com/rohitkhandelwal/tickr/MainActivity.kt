package com.rohitkhandelwal.tickr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rohitkhandelwal.tickr.ui.navigation.TickrNavGraph
import com.rohitkhandelwal.tickr.ui.theme.TickrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as TickrApp).appContainer
        enableEdgeToEdge()
        setContent {
            TickrTheme {
                TickrNavGraph(appContainer = appContainer)
            }
        }
    }
}
