package com.borarpg

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.borarpg.feature.activity.presentation.viewmodel.CepViewModel
import com.borarpg.feature.activity.presentation.viewmodel.PlacesViewModel
import com.borarpg.feature.activity.presentation.viewmodel.WorkoutViewModel
import com.borarpg.feature.backpack.presentation.viewmodel.BackpackViewModel
import com.borarpg.feature.journey.presentation.viewmodel.AdventureViewModel
import com.borarpg.feature.missions.presentation.viewmodel.MissionViewModel
import com.borarpg.feature.profile.presentation.viewmodel.PlayerViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeatureInjectionTest {
    @Test
    fun activityCreatesEveryFeatureViewModelThroughHilt() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val provider = ViewModelProvider(activity)
                assertNotNull(provider[AdventureViewModel::class.java])
                assertNotNull(provider[PlayerViewModel::class.java])
                assertNotNull(provider[BackpackViewModel::class.java])
                assertNotNull(provider[CepViewModel::class.java])
                assertNotNull(provider[PlacesViewModel::class.java])
                assertNotNull(provider[WorkoutViewModel::class.java])
                assertSame(
                    provider[MissionViewModel::class.java],
                    ViewModelProvider(activity)[MissionViewModel::class.java],
                )
            }
        }
    }
}
