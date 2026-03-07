package com.kenlifts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kenlifts.ui.theme.KenliftsTheme
import com.kenlifts.ui.screens.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = AppContainer(this)
        handleRoutineIntent(intent)
        setContent {
            KenliftsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(appContainer = appContainer)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRoutineIntent(intent)
    }

    private fun handleRoutineIntent(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_ROUTINE_ID, -1L)?.takeIf { it >= 0 }
        if (id != null) {
            PendingNavigation.setRoutineId(id)
        }
    }

    companion object {
        const val EXTRA_ROUTINE_ID = "com.kenlifts.EXTRA_ROUTINE_ID"
    }
}
