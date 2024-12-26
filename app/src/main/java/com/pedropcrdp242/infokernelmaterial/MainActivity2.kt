package com.pedropcrdp242.infokernelmaterial

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.app.ActivityManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.os.Debug
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedropcrdp242.infokernelmaterial.ui.theme.InfoKernelTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.io.BufferedReader
import java.io.InputStreamReader
import com.scottyab.rootbeer.RootBeer

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InfoKernelTheme {
                NeofetchScreen()
            }
        }
    }
}

@Composable
fun NeofetchScreen() {
    val context = LocalContext.current
    val fingerprint = Build.FINGERPRINT
    val deviceName = Build.MODEL
    val manufacturer = Build.MANUFACTURER
    val androidVersion = Build.VERSION.RELEASE
    val buildDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(Build.TIME))

    // Obtendo a versão do Clang diretamente do /proc/version
    val compilerVersion = remember { mutableStateOf("Fetching Clang version...") }
    LaunchedEffect(Unit) {
        compilerVersion.value = getKernelCompilerVersionWithRoot(context)
    }

    var uptime by remember { mutableStateOf(getUptime()) }
    var memoryInfo by remember { mutableStateOf(getMemoryInfo(context)) }

    // Atualiza uptime e uso de memória
    LaunchedEffect(Unit) {
        while (true) {
            uptime = getUptime()
            memoryInfo = getMemoryInfo(context)
            delay(1000) // Atualiza a cada segundo
        }
    }

    // Usando LazyColumn para rolagem garantida
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Espaçamento entre os itens
    ) {
        item {
            InfoCard2("Device Model", "$manufacturer $deviceName")
        }
        item {
            InfoCard2("Android Version", "Android $androidVersion")
        }
        item {
            InfoCard2("Kernel Build Date", buildDate)
        }
        item {
            InfoCard2("Clang Compiler", compilerVersion.value)
        }
        item {
            InfoCard2("System Uptime", uptime)
        }
        item {
            InfoCard2("Fingerprint", fingerprint)
        }
        item {
            InfoCard2("Memory Info", "Used: ${memoryInfo.first} / Free: ${memoryInfo.second}")
        }
    }
}

@Composable
fun InfoCard2(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getUptime(): String {
    val uptimeMillis = SystemClock.elapsedRealtime()
    val seconds = (uptimeMillis / 1000) % 60
    val minutes = (uptimeMillis / (1000 * 60) % 60)
    val hours = (uptimeMillis / (1000 * 60 * 60) % 24)
    val days = (uptimeMillis / (1000 * 60 * 60 * 24))

    return "${days}d ${hours}h ${minutes}m ${seconds}s"
}

fun getMemoryInfo(context: Context): Pair<String, String> {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val totalMemory = memoryInfo.totalMem / (1024 * 1024)  // Total em MB
    val availableMemory = memoryInfo.availMem / (1024 * 1024)  // Livre em MB
    val usedMemory = totalMemory - availableMemory  // Usado em MB

    return Pair("$usedMemory MB", "$availableMemory MB")
}

// Função para obter a versão do Clang diretamente do /proc/version
fun getKernelCompilerVersionWithRoot(context: Context): String {
    return try {
        // Verifica se o dispositivo está com root
        val rootbeer = RootBeer(context) // Passa o contexto
        if (!rootbeer.isRooted) {
            return "Device is not rooted"
        }

        // Executa o comando 'uname -v' como root para pegar a versão do compilador
        val process = Runtime.getRuntime().exec("su -c uname -v")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = reader.readLine()
        process.waitFor()

        // Verifica se a versão do Clang está presente na saída do comando
        val clangVersionRegex = Regex("clang version ([^ ]+)")
        val matchResult = clangVersionRegex.find(output)

        if (matchResult != null) {
            return "Clang ${matchResult.groupValues[1]}" // Retorna a versão do Clang
        }

        // Caso não encontre a versão do Clang, tenta usar /proc/version
        val versionProcess = Runtime.getRuntime().exec("su -c cat /proc/version")
        val versionReader = BufferedReader(InputStreamReader(versionProcess.inputStream))
        val versionOutput = versionReader.readLine()
        versionProcess.waitFor()

        // Verifica novamente no /proc/version
        val versionMatchResult = clangVersionRegex.find(versionOutput)
        if (versionMatchResult != null) {
            return "Clang ${versionMatchResult.groupValues[1]}" // Retorna a versão do Clang encontrada
        } else {
            "Clang version not found"
        }
    } catch (e: Exception) {
        "Error retrieving Clang version: ${e.message}"
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNeofetchCard() {
    InfoKernelTheme {
        NeofetchScreen()
    }
}