package com.refassistant.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientAware
import com.refassistant.app.ui.navigation.RootPager
import com.refassistant.app.ui.theme.RefAssistantTheme
import com.refassistant.app.viewmodel.MatchViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalHorologistApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            RefAssistantTheme {
                val viewModel: MatchViewModel = viewModel()
                AmbientAware { ambientStateUpdate ->
                    val isAmbient = ambientStateUpdate.ambientState is
                            com.google.android.horologist.compose.ambient.AmbientState.Ambient
                    RootPager(viewModel = viewModel, isAmbient = isAmbient)
                }
            }
        }
    }
}
