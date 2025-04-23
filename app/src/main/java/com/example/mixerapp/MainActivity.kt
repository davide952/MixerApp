package com.example.mixerapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mixerapp.ui.theme.MixerAppTheme


class MainActivity : ComponentActivity() {

    private val oscManager = OscManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MixerAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "mixer") {
                        composable("mixer") { MixerScreen(navController, oscManager) }
                        composable("settings") { SettingsScreen(navController, oscManager) }
                    }
                }
            }
        }
        oscManager.startStereoMeteringListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        oscManager.disconnect()
    }


}



