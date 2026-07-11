package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.CampusApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CampusViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by lazy { CampusViewModel(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CampusApp(viewModel)
            }
        }
    }
}
