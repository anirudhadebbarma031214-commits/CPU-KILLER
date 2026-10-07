package com.anistudio.cpukiller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CpuKillerApp() }
    }
}

private fun readText(path: String): String? = try {
    File(path).takeIf { it.canRead() }?.readText().trim()
} catch (_: Exception) { null }

private fun rootAvailable(): Boolean = try {
    Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        .inputStream.bufferedReader().readText().contains("uid=0")
} catch (_: Exception) { false }

private fun cpuCount() = Runtime.getRuntime().availableProcessors()

private fun maxFreqMHz(): String {
    val paths = listOf(
        "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq",
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"
    )
    for (p in paths) readText(p)?.toLongOrNull()?.let { return "${it / 1000} MHz" }
    return "Unavailable"
}

@Composable
fun CpuKillerApp() {
    val root = remember { rootAvailable() }
    var performance by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Ready") }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFFF3B30),
        secondary = Color(0xFFFF6B35),
        background = Color(0xFF08090D),
        surface = Color(0xFF11131A)
    )) {
        Surface(color = Color(0xFF08090D), modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
            ) {
                Text("CPU KILLER", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF3B30))
                Text("ROOT CPU CONTROL", color = Color.LightGray, fontSize = 12.sp)
                Spacer(Modifier.height(22.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151821)),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            if (root) "ROOT ACCESS: DETECTED" else "ROOT ACCESS: NOT DETECTED",
                            fontWeight = FontWeight.Bold,
                            color = if (root) Color(0xFF62FF7A) else Color(0xFFFFC107)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("Cores: ${cpuCount()}")
                        Text("Kernel max frequency: ${maxFreqMHz()}")
                        Text("Status: $message", color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("PERFORMANCE", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Performance mode", Modifier.weight(1f))
                    Switch(checked = performance, onCheckedChange = {
                        performance = it
                        message = if (it) "Performance profile selected" else "Balanced profile selected"
                    })
                }

                Spacer(Modifier.height(16.dp))
                Text("CPU CONTROL", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))

                Button(onClick = {
                    message = if (root)
                        "Root shell available. Compatible kernel controls can be enabled."
                    else "Root required for frequency/governor changes."
                }, Modifier.fillMaxWidth()) {
                    Text("SCAN KERNEL CONTROLS")
                }

                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = {
                    message = "Safety check: stay within device/kernel thermal limits."
                }, Modifier.fillMaxWidth()) {
                    Text("THERMAL SAFETY")
                }

                Spacer(Modifier.height(24.dp))
                Text("CPU KILLER v1.0 • ANI STUDIO", color = Color.Gray, fontSize = 11.sp)
                Text(
                    "Actual overclocking depends on the rooted kernel. CPU KILLER does not bypass hardware or kernel limits.",
                    color = Color.Gray, fontSize = 11.sp
                )
            }
        }
    }
}
