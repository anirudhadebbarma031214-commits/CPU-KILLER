package com.anistudio.cpukiller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
    File(path).takeIf { it.canRead() }?.readText()?.trim()
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
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFF8AA4FF).copy(alpha = 0.055f),
                        Color.White.copy(alpha = 0.035f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.32f),
                        Color(0xFF7C8CFF).copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                RoundedCornerShape(28.dp)
            )
            .padding(20.dp),
        content = content
    )
}

@Composable
fun CpuKillerApp() {
    val root = remember { rootAvailable() }
    var performance by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Ready") }
    val pulse by rememberInfiniteTransition(label = "glassGlow").animateFloat(
        initialValue = 0.20f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "pulse"
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF3B30),
            secondary = Color(0xFF6E8CFF),
            background = Color(0xFF05060A),
            surface = Color(0xFF10131C)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF05060A))
        ) {
            Box(
                Modifier
                    .size(260.dp)
                    .offset(x = (-90).dp, y = 40.dp)
                    .alpha(pulse)
                    .background(Color(0xFF287CFF), RoundedCornerShape(160.dp))
            )
            Box(
                Modifier
                    .size(240.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 80.dp, y = (-80).dp)
                    .alpha(pulse)
                    .background(Color(0xFFFF1744), RoundedCornerShape(150.dp))
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = com.anistudio.cpukiller.R.drawable.cpu_killer_logo),
                    contentDescription = "CPU KILLER logo",
                    modifier = Modifier.size(124.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "CPU KILLER",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    "LIQUID GLASS • ROOT CPU CONTROL",
                    color = Color(0xFFB9C4E8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(22.dp))

                GlassCard {
                    Text(
                        if (root) "●  ROOT ACCESS DETECTED" else "●  ROOT ACCESS NOT DETECTED",
                        fontWeight = FontWeight.Bold,
                        color = if (root) Color(0xFF63FF9A) else Color(0xFFFFC857)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("CPU CORES", color = Color(0xFF9EA8C3), fontSize = 11.sp)
                    Text("${cpuCount()}", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("KERNEL MAX FREQUENCY", color = Color(0xFF9EA8C3), fontSize = 11.sp)
                    Text(maxFreqMHz(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("STATUS  •  $message", color = Color(0xFFB7C0D8), fontSize = 12.sp)
                }

                Spacer(Modifier.height(16.dp))
                GlassCard {
                    Text("PERFORMANCE", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Performance mode", color = Color.White, fontSize = 15.sp)
                            Text(
                                if (performance) "Maximum compatible performance profile" else "Balanced everyday profile",
                                color = Color(0xFF9EA8C3),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = performance,
                            onCheckedChange = {
                                performance = it
                                message = if (it) "Performance profile selected" else "Balanced profile selected"
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                GlassCard {
                    Text("CPU CONTROL", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            message = if (root)
                                "Root shell available. Compatible kernel controls can be enabled."
                            else "Root required for frequency/governor changes."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("SCAN KERNEL CONTROLS")
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            message = "Safety check: stay within device/kernel thermal limits."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("THERMAL SAFETY")
                    }
                }

                Spacer(Modifier.height(16.dp))
                GlassCard {
                    Text("ABOUT CPU KILLER", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Spacer(Modifier.height(10.dp))
                    Text("Created by", color = Color(0xFF9EA8C3), fontSize = 12.sp)
                    Text(
                        "ANIRUDDHA DEBBARMA",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color(0xFFFF4B3E)
                    )
                    Text("ANI STUDIO", fontWeight = FontWeight.Bold, color = Color(0xFF8FA8FF))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "CPU KILLER is an ANI STUDIO project.",
                        color = Color(0xFF9EA8C3),
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text("CPU KILLER v1.0 • ANI STUDIO", color = Color(0xFF7F879D), fontSize = 11.sp)
                Text(
                    "Actual overclocking depends on the rooted kernel. CPU KILLER does not bypass hardware or kernel limits.",
                    color = Color(0xFF656C80),
                    fontSize = 10.sp
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}