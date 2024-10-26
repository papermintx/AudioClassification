package com.example.audioclasification

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.audioclasification.components.PermissionHandler
import com.example.audioclasification.ui.theme.AudioClasificationTheme
import com.google.mediapipe.tasks.components.containers.Classifications
import android.Manifest
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioClasificationTheme {
               Home()
            }
        }
    }
}


@Composable
fun Home() {
    val context = LocalContext.current

    PermissionHandler(
        permissions = listOf(Manifest.permission.RECORD_AUDIO),
        permissionsNotGranted = {
            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
        },
        permissionsNotAvailable = {
            Toast.makeText(context, "Permission not available", Toast.LENGTH_SHORT).show()
        }
    ) {
        HomeScreen()
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // for my state
    var classificationResults = remember { mutableStateOf<String?>(null) }
    var errorMessage = remember { mutableStateOf<String?>(null) }
    var isClassifying = remember { mutableStateOf(false) }

    // in compose we use remember to create a singleton instance of the helper class
    val audioClassifierHelper = remember {
        AudioClassifierHelper(
            context = context,
            classifierListener = object : AudioClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    errorMessage.value = error
                }

                override fun onResults(results: List<Classifications>, inferenceTime: Long) {
                   results.let {
                       val sortedCategories =
                           it[0].categories().sortedByDescending { it?.score() }
                       val displayResult =
                           sortedCategories.joinToString("\n") {
                               "${it.categoryName()} " + NumberFormat.getPercentInstance()
                                   .format(it.score()).trim()
                           }
                          classificationResults.value = displayResult
                   }

                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Hello, World!")
            Spacer(modifier = Modifier.padding(8.dp))
            Row {
                Button(
                    onClick = {
                        // Start audio classification
                        audioClassifierHelper.startAudioClassification()
                        isClassifying.value = true
                    },
                    enabled = !isClassifying.value
                ) {
                    Text("Start")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    onClick = {
                        // Stop audio classification
                        audioClassifierHelper.stopAudioClassification()
                        isClassifying.value = false
                    },
                    enabled = isClassifying.value
                ) {
                    Text("Stop")
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            // Display error message if any
            errorMessage.value?.let {
                Text(text = "Error: ${errorMessage.value}", color = Color.Red, modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Audio Clasification Results", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.padding(8.dp))
            // Display classification results
            Text("Results: ${classificationResults.value}", modifier = Modifier.padding(16.dp))

            // Clean up resources when the composable is disposed
            DisposableEffect(Unit) {
                onDispose {
                    audioClassifierHelper.stopAudioClassification()
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenView() {
    AudioClasificationTheme {
        HomeScreen()
    }
}
