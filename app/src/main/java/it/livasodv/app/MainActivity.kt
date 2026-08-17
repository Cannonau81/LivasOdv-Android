package it.livasodv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import it.livasodv.app.data.LocalManagementStore
import it.livasodv.app.feature.LivasApp
import it.livasodv.app.ui.theme.LivasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LocalManagementStore.init(applicationContext)
        setContent { LivasTheme { LivasApp() } }
    }
}
