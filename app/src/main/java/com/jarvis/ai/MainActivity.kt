package com.jarvis.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

private val bg = Color(0xFF020A0B)
private val neon = Color(0xFF00FFB3)

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("bn", "BD")
            }
        }

        setContent {
            JarvisScreen(
                onSpeak = { text ->
                    tts?.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "jarvis_reply"
                    )
                }
            )
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun JarvisScreen(onSpeak: (String) -> Unit) {
    var message by remember {
        mutableStateOf("আমি JARVIS। কীভাবে সাহায্য করতে পারি?")
    }
    var listening by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        listening = false
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()

        if (spoken != null) {
            message = "তুমি বলেছ: $spoken"
            onSpeak("তোমার কথা শুনেছি। AI সংযোগ যোগ করার পর আমি উত্তর দিতে পারব।")
        } else {
            message = "কথা বুঝতে পারিনি। আবার চেষ্টা করো।"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            listening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            }
            try {
                speechLauncher.launch(intent)
            } catch (_: Exception) {
                listening = false
                message = "এই ফোনে Speech Recognition চালু নেই।"
            }
        } else {
            message = "ভয়েস ব্যবহারের অনুমতি প্রয়োজন।"
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "J.A.R.V.I.S",
                    color = neon,
                    fontSize = 32.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "PERSONAL AI ASSISTANT",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(50.dp))

                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(Color(0xFF063B30), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (listening) "LISTENING" else "JARVIS",
                        color = neon,
                        fontSize = 26.sp
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val intent = Intent(
                                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                            ).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE,
                                    "bn-BD"
                                )
                            }

                            try {
                                listening = true
                                speechLauncher.launch(intent)
                            } catch (_: Exception) {
                                listening = false
                                message = "Speech Recognition চালু করা যায়নি।"
                            }
                        } else {
                            permissionLauncher.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007F5F)
                    )
                ) {
                    Text("🎙 কথা বলো", color = Color.White)
                }
            }
        }
    }
}
