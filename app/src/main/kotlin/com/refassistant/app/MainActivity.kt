package com.refassistant.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.refassistant.app.ui.navigation.RootPager
import com.refassistant.app.ui.theme.RefAssistantTheme
import com.refassistant.app.viewmodel.MatchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            RefAssistantTheme {
                val viewModel: MatchViewModel = viewModel()
                RootPager(viewModel = viewModel)
            }
        }
    }
}
