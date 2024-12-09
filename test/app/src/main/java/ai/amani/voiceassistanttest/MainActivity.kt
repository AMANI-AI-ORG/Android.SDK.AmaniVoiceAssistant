package ai.amani.voiceassistanttest

import ai.amani.voice_assistant.callback.AmaniVAInitCallBack
import ai.amani.voice_assistant.callback.AmaniVAPlayerCallBack
import ai.amani.voice_assistant.AmaniVoiceAssistant
import ai.amani.voice_assistant.model.TTSVoice
import ai.amani.voiceassistanttest.ui.theme.VoiceAssistantTestTheme
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceAssistantTestTheme {

                var state: VoiceState by remember{
                    mutableStateOf(VoiceState.Loading)
                }

                VoiceScreen(state = state) {
                        voice, isPlaying ->

                    if (isPlaying) {
                        AmaniVoiceAssistant.stop()
                    } else {
                        AmaniVoiceAssistant.play(
                            context = this@MainActivity,
                            key = voice.key,
                            callBack = object : AmaniVAPlayerCallBack {
                                override fun onPlay() {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "onPlay callback is triggered",
                                        Toast.LENGTH_SHORT)
                                        .show()
                                }

                                override fun onStop() {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "onStop callback is triggered",
                                        Toast.LENGTH_SHORT)
                                        .show()
                                }

                                override fun onFailure(exception: Exception) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "onFailure callback is triggered, exception: $exception",
                                        Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        )
                    }
                }

                AmaniVoiceAssistant.init(
                    url = "URL",
                    callBack = object : AmaniVAInitCallBack {
                        override fun onSuccess(voices: List<TTSVoice>) {
                            state = VoiceState.Success(voices)
                        }

                        override fun onFailure(exception: Exception) {
                            state = VoiceState.Error(exception)
                        }
                    }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        AmaniVoiceAssistant.stop()
    }
}

sealed class VoiceState{
    data object Loading: VoiceState()
    data class Success(val voices: List<TTSVoice>): VoiceState()
    data class Error(val error: Exception): VoiceState()
}


@Composable
fun VoiceScreen(state: VoiceState, onVoiceSelected: (TTSVoice, Boolean) -> Unit) {
    when (state) {
        is VoiceState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is VoiceState.Success -> {

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(state.voices.size) { index ->
                        val voice = state.voices[index]
                        var isPlaying by remember { mutableStateOf(false) }

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            ),
                            onClick = {
                                onVoiceSelected(voice, isPlaying)
                                isPlaying = !isPlaying
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                                .height(60.dp) // Customize height as needed
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                // Text indicating current state
                                Text(
                                    text = voice.key,
                                    modifier = Modifier.weight(2f),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.padding(horizontal = 40.dp))

                                // Text indicating current state
                                Text(
                                    color = Color.White,
                                    text = if (isPlaying) "Pause" else "Play",
                                    modifier = Modifier.weight(2f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        is VoiceState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error: ${state.error.localizedMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { /* Retry logic here */ }) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VoiceAssistantTestTheme {
        Greeting("Android")
    }
}