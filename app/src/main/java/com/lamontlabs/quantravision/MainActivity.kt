package com.lamontlabs.quantravision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lamontlabs.quantravision.ui.QuantraVisionApp
import com.lamontlabs.quantravision.ui.QuantraVisionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        CrashLogger.initialize(this)
        
        setContent {
            QuantraVisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuantraVisionApp(context = this@MainActivity)
                }
            }
        }
    }
}
