package com.borarpg.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.borarpg.data.*
import com.borarpg.ui.AdventureViewModel
import com.borarpg.ui.PlayerViewModel

internal class BoraViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val instance =
            when {
                modelClass.isAssignableFrom(AdventureViewModel::class.java) ->
                    AdventureViewModel(
                        AdventureRepository(DatabaseProvider.get(context.applicationContext)),
                        TrackingController(context.applicationContext),
                    )
                modelClass.isAssignableFrom(PlayerViewModel::class.java) ->
                    PlayerViewModel(PlayerRepository(context.applicationContext))
                else -> error("ViewModel desconhecido: $modelClass")
            }
        @Suppress("UNCHECKED_CAST")
        return instance as T
    }
}
