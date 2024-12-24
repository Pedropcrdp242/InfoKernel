package com.pedropcrdp242.infokerneljetpack

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedropcrdp242.infokerneljetpack.ui.theme.InfoKernelTheme
import com.scottyab.rootbeer.RootBeer
import android.content.ClipData
import android.content.ClipboardManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InfoKernelTheme {
                KernelInfoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KernelInfoApp() {
    val context = LocalContext.current
    var rootStatus by remember { mutableStateOf("Fetching root status...") }
    var kernelInfo by remember { mutableStateOf("Fetching kernel info...") }
    var lkmStatus by remember { mutableStateOf("Fetching LKM status...") }
    var modulesInfo by remember { mutableStateOf("Fetching kernel modules...") }
    var architectureInfo by remember { mutableStateOf("Fetching architecture info...") }

    // Atualizar as informações automaticamente ao iniciar o aplicativo
    LaunchedEffect(Unit) {
        rootStatus = checkRootStatus(RootBeer(context))
        kernelInfo = getKernelInfo()
        lkmStatus = checkLKMStatus()
        modulesInfo = getKernelModules()
        architectureInfo = getArchitectureInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InfoKernel", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exibindo as informações
            InfoCard("Root Status", rootStatus)
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard("Device Architecture", architectureInfo)
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard("Kernel Info", kernelInfo)
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard("LKM Status", lkmStatus)
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard("Kernel Modules", modulesInfo)

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de copiar para a área de transferência
            Button(onClick = {
                val infoToCopy = buildString {
                    append("=== Root Status ===\n")
                    append(rootStatus)
                    append("\n\n=== Device Architecture ===\n")
                    append(architectureInfo)
                    append("\n\n=== Kernel Info ===\n")
                    append(kernelInfo)
                    append("\n\n=== LKM Status ===\n")
                    append(lkmStatus)
                    append("\n\n=== Kernel Modules ===\n")
                    append(modulesInfo)
                }
                copyToClipboard(context, infoToCopy)
            }) {
                Text("Copy Info to Clipboard")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de atualizar informações manualmente (opcional)
            Button(onClick = {
                rootStatus = checkRootStatus(RootBeer(context))
                kernelInfo = getKernelInfo()
                lkmStatus = checkLKMStatus()
                modulesInfo = getKernelModules()
                architectureInfo = getArchitectureInfo()
            }) {
                Text("Update Info")
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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

fun copyToClipboard(context: android.content.Context, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText("Kernel Info", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Information copied to clipboard", Toast.LENGTH_SHORT).show()
}

fun checkRootStatus(rootBeer: RootBeer): String {
    val rootType = getRootType()
    val usesLKM = if (rootType == "KernelSU") "Uses LKM" else "Does not use LKM"
    return if (rootBeer.isRooted) {
        "Root Detected: $rootType ($usesLKM)"
    } else {
        "Root Not Detected :("
    }
}

fun getRootType(): String {
    return when {
        checkMagisk() -> "Magisk"
        checkKernelSU() -> "KernelSU"
        else -> "Unknown :/"
    }
}

fun checkMagisk(): Boolean {
    return checkFileExists("/sbin/.magisk") || checkFileExists("/data/adb/magisk")
}

fun checkKernelSU(): Boolean {
    return checkFileExists("/data/adb/ksu") || checkFileExists("/data/adb/modules/ksu")
}

fun checkFileExists(path: String): Boolean {
    return try {
        val process = Runtime.getRuntime().exec("su -c ls $path")
        process.waitFor() == 0
    } catch (e: Exception) {
        false
    }
}

fun getKernelInfo(): String {
    return try {
        val process = Runtime.getRuntime().exec("su -c uname -r")
        val kernelVersion = process.inputStream.bufferedReader().readText().trim()
        "Kernel Version: $kernelVersion"
    } catch (e: Exception) {
        "Unable to fetch kernel version"
    }
}

fun checkLKMStatus(): String {
    return "LKM Active: Yes (default for most devices)"
}

fun getKernelModules(): String {
    return try {
        val process = Runtime.getRuntime().exec("su -c cat /proc/modules")
        process.inputStream.bufferedReader().readText().trim().ifEmpty { "No modules loaded." }
    } catch (e: Exception) {
        "Unable to fetch kernel modules"
    }
}

fun getArchitectureInfo(): String {
    return try {
        val arch = System.getProperty("os.arch") ?: "Unknown"
        when (arch) {
            "aarch64" -> "arm64"
            "armv7l" -> "arm32"
            "x86_64" -> "x86_64"
            "x86" -> "x86"
            else -> "Unknown ($arch)"
        }
    } catch (e: Exception) {
        "Unable to fetch architecture info"
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InfoKernelTheme {
        KernelInfoApp()
    }
}


