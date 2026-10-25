package com.borarpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.borarpg.navigation.AdventureApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(16, 24, 19)
        window.navigationBarColor = android.graphics.Color.rgb(16, 24, 19)
        setContent { AdventureApp() }
    }
}
