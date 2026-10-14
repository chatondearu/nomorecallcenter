package fr.chatondearu.nmcc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.chatondearu.nmcc.ui.NmccNav
import fr.chatondearu.nmcc.ui.theme.NmccTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NmccTheme {
                NmccNav(activity = this)
            }
        }
    }
}
