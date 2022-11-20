package dev.bargen.webrtcdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import dev.bargen.webrtcdemo.ui.theme.WebRTCDemoTheme
import org.webrtc.EglBase

class MainActivity : ComponentActivity() {
    val logMessages: MutableList<String> = mutableListOf()
    val logMessagesLiveData: MutableLiveData<List<String>> = MutableLiveData(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load UI
        setContent {
            var demo by remember { mutableStateOf<PeerConnectionDemo?>(null) }

            WebRTCDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colors.background,
                ) {
                    Column {
                        Intro {
                            addToLog("---")
                            demo?.apply {
                                addToLog("> Disposing previous peer connection...")
                                this.dispose()
                            }
                            addToLog("> Creating peer connection...")
                            demo = PeerConnectionDemo(this@MainActivity, EglBase.create()) {
                                // Forward events to log
                                addToLog(it)
                            }
                            addToLog("> Gather candidates...")
                            demo?.gatherCandidates()
                            addToLog("> Disposing peer connection...")
                            //demo.dispose()
                            addToLog("> Done")
                        }

                        LogMessages(logMessagesLiveData.observeAsState())
                    }
                }
            }
        }

        addToLog("Ready! Press button.")
    }

    fun addToLog(message: String) {
        logMessages.add(message)
        val newList = logMessages.toList()
        logMessagesLiveData.postValue(newList)
    }
}

@Composable
fun Intro(onClick: () -> Unit) {
    Column {
        Text(
            text = "WebRTC Demo",
            style = MaterialTheme.typography.h4,
        )
        Text(
            text = "This app demonstrates that the dev.bargen.webrtc-android library compiles " +
                   "without problems, and that some basic WebRTC classes can be instantiated at runtime.",
            style = MaterialTheme.typography.body1,
        )

        Button(onClick = onClick) {
            Text(text = "Create PeerConn & Gather ICE")
        }
    }
}

@Composable
fun LogMessages(messages: State<List<String>?>) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(text = "Log", style = MaterialTheme.typography.h5)
        Column {
            messages.value?.forEach { msg ->
                Text(text = msg)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WebRTCDemoTheme {
        Intro {
            Log.d("Preview", "Button clicked")
        }
    }
}