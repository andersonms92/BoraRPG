package com.borarpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.borarpg.data.*
import com.borarpg.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(16, 24, 19)
        window.navigationBarColor = android.graphics.Color.rgb(16, 24, 19)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val instance = when {
                    modelClass.isAssignableFrom(AdventureViewModel::class.java) -> AdventureViewModel(AdventureRepository(DatabaseProvider.get(applicationContext)))
                    modelClass.isAssignableFrom(PlayerViewModel::class.java) -> PlayerViewModel(PlayerRepository(applicationContext))
                    else -> error("ViewModel desconhecido: $modelClass")
                }
                @Suppress("UNCHECKED_CAST") return instance as T
            }
        }
        setContent {
            val adventure: AdventureViewModel = viewModel(factory = factory)
            val player: PlayerViewModel = viewModel(factory = factory)
            AdventureApp(adventure, player)
        }
    }
}

private object DatabaseProvider {
    @Volatile private var instance: GameDatabase? = null
    fun get(context: android.content.Context): GameDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext, GameDatabase::class.java, "borarpg.db").build().also { instance = it }
    }
}
