package com.pedropcrdp242.infokernelmaterial


import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.app.ActivityManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedropcrdp242.infokernelmaterial.ui.theme.InfoKernelTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import android.os.StatFs
import java.io.BufferedReader
import java.io.InputStreamReader
import com.scottyab.rootbeer.RootBeer
import android.app.AlertDialog
import androidx.compose.ui.graphics.Color
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

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
            BackupCard()
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
@Composable
fun BackupCard() {
    val context = LocalContext.current
    var backupMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Backup Boot/Recovery",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "This operation will create a backup of the boot and recovery partitions on your device. Ensure sufficient storage space and proper permissions before proceeding.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = {
                    // Correção: Garantir que a função performBackup esteja configurada corretamente
                    performBackup(context) { message ->
                        backupMessage = message
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Start Backup")
            }
            // Exibição da mensagem de feedback
            backupMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (it.contains("Error", ignoreCase = true)) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

fun performBackup(context: Context, onResult: (String) -> Unit) {
    try {
        val rootbeer = RootBeer(context)
        if (!rootbeer.isRooted) {
            onResult("Error: Device is not rooted. Backup cannot proceed.")
            return
        }

        fun runCommandAsRoot(command: String): Pair<String, Int> {
            return try {
                val process = Runtime.getRuntime().exec("su")
                val outputStream = process.outputStream
                outputStream.write(("$command\nexit\n").toByteArray())
                outputStream.flush()

                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    output.trim() to exitCode
                } else {
                    error.trim() to exitCode
                }
            } catch (e: Exception) {
                "Error executing command: ${e.message}" to -1
            }
        }

        val storagePath = Environment.getExternalStorageDirectory().absolutePath
        val stat = StatFs(storagePath)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong

        if (availableBytes < 500 * 1024 * 1024) {
            onResult("Error: Insufficient storage space on $storagePath. Available: ${availableBytes / (1024 * 1024)} MB")
            return
        }

        val partitions = listOf("boot", "boot_a", "boot_b")
        var bootPartition: String? = null
        var recoveryPartition: String? = null

        // Detect boot and recovery partitions
        for (partition in partitions) {
            val checkCommand = "test -e /dev/block/by-name/$partition"
            val (_, exists) = runCommandAsRoot(checkCommand)
            if (exists == 0) {
                bootPartition = partition
                break
            }
        }

        if (bootPartition == null) {
            onResult("Error: Boot partition not found.")
            return
        }

        val checkRecoveryCommand = "test -e /dev/block/by-name/recovery"
        val (_, recoveryExists) = runCommandAsRoot(checkRecoveryCommand)
        if (recoveryExists == 0) {
            recoveryPartition = "recovery"
        }

        // Check for existing file and prompt for a new name if necessary
        fun getUniqueFileName(baseName: String): String {
            var fileName = "$storagePath/$baseName.img"
            var counter = 1

            while (File(fileName).exists()) {
                fileName = "$storagePath/${baseName}_$counter.img"
                counter++
            }

            return fileName
        }

        // Backup boot partition with checksum for integrity check
        val bootBackupFile = getUniqueFileName("boot_backup")
        val bootBackupCommand = "dd if=/dev/block/by-name/$bootPartition of=$bootBackupFile bs=4096 conv=noerror,sync"
        val (bootResult, bootExitCode) = runCommandAsRoot(bootBackupCommand)
        if (bootExitCode != 0) {
            onResult("Error during boot partition backup: $bootResult")
            return
        }

        // Verify integrity of the backup
        val verifyCommand = "sha256sum $bootBackupFile"
        val (sha256Output, sha256ExitCode) = runCommandAsRoot(verifyCommand)
        if (sha256ExitCode != 0) {
            onResult("Error verifying backup integrity: $sha256Output")
            return
        }

        // Backup recovery partition if available
        if (recoveryPartition != null) {
            val recoveryBackupFile = getUniqueFileName("recovery_backup")
            val recoveryBackupCommand = "dd if=/dev/block/by-name/$recoveryPartition of=$recoveryBackupFile bs=4096 conv=noerror,sync"
            val (recoveryResult, recoveryExitCode) = runCommandAsRoot(recoveryBackupCommand)
            if (recoveryExitCode != 0) {
                onResult("Error during recovery partition backup: $recoveryResult")
                return
            }

            // Verify recovery partition backup integrity
            val recoverySha256Command = "sha256sum $recoveryBackupFile"
            val (recoverySha256Output, recoverySha256ExitCode) = runCommandAsRoot(recoverySha256Command)
            if (recoverySha256ExitCode != 0) {
                onResult("Error verifying recovery backup integrity: $recoverySha256Output")
                return
            }
        } else {
            onResult("Warning: Recovery partition not found. Only boot partition was backed up.")
        }

        onResult("Backup completed successfully. Boot and recovery images saved in $storagePath.")
    } catch (e: Exception) {
        onResult("Error during backup: ${e.message}")
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewNeofetchCard() {
    InfoKernelTheme {
        NeofetchScreen()
    }
}