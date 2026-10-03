package com.resonix.media.crash

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import com.resonix.media.MainActivity
import com.resonix.media.core.ui.theme.ResonixTheme

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exception = intent.getStringExtra("exception").orEmpty()
        setContent {
            ResonixTheme {
                val clipboard = LocalClipboard.current
                CrashScreen(
                    exception = exception,
                    onCopy = { copyCrashReportToClipboard(clipboard.nativeClipboard, exception) },
                    onRestart = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}

@Composable
private fun CrashScreen(
    exception: String,
    onCopy: () -> Unit,
    onRestart: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Resonix stopped unexpectedly", style = MaterialTheme.typography.headlineSmall)
            Text(text = "You can copy this report for troubleshooting, then restart the app.")
            SelectionContainer {
                Text(text = exception, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
                Text("Copy crash report")
            }
            OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
                Text("Restart app")
            }
        }
    }
}
