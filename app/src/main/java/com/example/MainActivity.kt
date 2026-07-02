package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainDashboardHub
import com.example.ui.OnboardingScreen
import com.example.ui.SecurePinLockScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StorageViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: StorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge transparent system bars
        enableEdgeToEdge()
        
        setContent {
            val session by viewModel.userSession.collectAsStateWithLifecycle()
            
            // Auto theme selector based on user profile settings (defaults to Dark)
            val isDarkTheme = (session?.themeMode ?: "Dark") == "Dark"

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        session == null -> {
                            // Simple loading placeholder while database loads
                        }
                        
                        !session!!.isOnboarded -> {
                            // First launch install steps: intro, permission check, signup, selections
                            OnboardingScreen(viewModel = viewModel)
                        }
                        
                        session!!.isLocked -> {
                            // Locked App state numeric PIN pad
                            SecurePinLockScreen(
                                title = "NexStorage Locked",
                                errorMsg = viewModel.wrongPinError.collectAsStateWithLifecycle().value,
                                onPinEntered = { pin ->
                                    viewModel.unlockApp(pin)
                                }
                            )
                        }
                        
                        else -> {
                            // Primary fully responsive and functional Storage workspace
                            MainDashboardHub(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
