package com.extremewakeup.soundalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.extremewakeup.soundalarm.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use the ViewModel here
    }
}