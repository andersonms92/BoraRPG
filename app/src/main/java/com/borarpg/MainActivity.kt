package com.borarpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.borarpg.di.BoraViewModelFactory
import com.borarpg.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(16, 24, 19)
        window.navigationBarColor = android.graphics.Color.rgb(16, 24, 19)
        val factory = BoraViewModelFactory(applicationContext)
        setContent {
            val adventure: AdventureViewModel = viewModel(factory = factory)
            val player: PlayerViewModel = viewModel(factory = factory)
            AdventureApp(adventure, player)
        }
    }
}