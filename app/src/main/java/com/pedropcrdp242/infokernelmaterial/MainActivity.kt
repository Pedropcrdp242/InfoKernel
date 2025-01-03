package com.pedropcrdp242.infokernelmaterial

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedropcrdp242.infokernelmaterial.ui.theme.InfoKernelTheme
import com.scottyab.rootbeer.RootBeer
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.draw.blur
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import android.content.res.Configuration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalConfiguration


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
    val navController = rememberNavController()

    // Estado para as informações
    var rootStatus by remember { mutableStateOf("Fetching root status...") }
    var kernelInfo by remember { mutableStateOf("Fetching kernel info...") }
    var lkmStatus by remember { mutableStateOf("Fetching LKM status...") }
    var modulesInfo by remember { mutableStateOf("Fetching kernel modules...") }
    var architectureInfo by remember { mutableStateOf("Fetching architecture info...") }
    var gkiSupport by remember { mutableStateOf("Fetching GKI support...") }

    // Atualiza as informações automaticamente ao iniciar o aplicativo
    LaunchedEffect(Unit) {
        rootStatus = checkRootStatus(RootBeer(context))
        kernelInfo = getKernelInfo()
        lkmStatus = checkLKMStatus()
        modulesInfo = getKernelModules()
        architectureInfo = getArchitectureInfo()
        gkiSupport = checkGKISupport()
    }

    // Verifica se o sistema está em tema claro ou escuro
    val isDarkTheme = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val topBarBackground = Color.Transparent  // Mantém a barra transparente

    // Estilo customizado para o título
    val titleStyle = TextStyle(
        color = textColor,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal
    )

    // Scaffold principal com TopAppBar e BottomAppBar
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Ajusta a altura da TopAppBar
            ) {
                // Aplicando blur no fundo da TopAppBar
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = 0.5f }
                        .blur(15.dp) // Ajuste do blur para um efeito mais suave
                )

                // TopAppBar com conteúdo (não afetado pelo blur)
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "RootMate ",
                                style = titleStyle,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val intent = Intent(context, MainActivity3::class.java)
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                    tint = textColor
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBackground),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        modifier = Modifier
            .fillMaxSize() // Garante que o conteúdo ocupe todo o espaço disponível
            .padding(0.dp) // Remove o padding inferior
    ) { innerPadding -> // Adiciona o padding automático calculado pelo Scaffold
        NavHost(
            navController = navController,
            startDestination = "kernel_screen",
            modifier = Modifier
                .padding(innerPadding) // Adiciona o padding do Scaffold
                .fillMaxSize() // Garante que o conteúdo ocupe todo o espaço disponível
        ) {
            composable("kernel_screen") {
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Preenche o restante do espaço
                        .padding(16.dp) // Padding interno do conteúdo
                        .verticalScroll(rememberScrollState()) // Permite a rolagem
                        .padding(bottom = 56.dp), // Garante que o conteúdo não sobreponha o BottomNavigationBar
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Conteúdo da tela
                    InfoCard("Root Status", rootStatus)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoCard("Device Architecture", architectureInfo)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoCard("Kernel Info", kernelInfo)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoCard("LKM Status", lkmStatus)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoCard("GKI Support", gkiSupport)  // Novo card GKI Support
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoCard("Kernel Modules", modulesInfo)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botões de ação
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
                            append("\n\n=== GKI Support ===\n")
                            append(gkiSupport)
                            append("\n\n=== Kernel Modules ===\n")
                            append(modulesInfo)
                        }
                        copyToClipboard(context, infoToCopy)
                    }) {
                        Text("Copy Info to Clipboard")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        rootStatus = checkRootStatus(RootBeer(context))
                        kernelInfo = getKernelInfo()
                        lkmStatus = checkLKMStatus()
                        modulesInfo = getKernelModules()
                        architectureInfo = getArchitectureInfo()
                        gkiSupport = checkGKISupport()  // Atualiza o GKI support também
                    }) {
                        Text("Update Info")
                    }
                }

            }
            composable("performance_screen") {
                NeofetchScreen()
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = remember { mutableStateOf("kernel_screen") }

    // Remover qualquer padding ou margin extra
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth() // Garante que ocupe toda a largura
            .padding(0.dp)  // Remove padding extra
    ) {
        // Navegação para a tela "kernel_screen"
        NavigationBarItem(
            selected = currentRoute.value == "kernel_screen",
            onClick = {
                navController.navigate("kernel_screen") {
                    launchSingleTop = true
                }
                currentRoute.value = "kernel_screen"
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Kernel") },
            label = {
                if (currentRoute.value == "kernel_screen") {
                    Text("Kernel")
                }
            }
        )
        // Navegação para a tela "performance_screen"
        NavigationBarItem(
            selected = currentRoute.value == "performance_screen",
            onClick = {
                navController.navigate("performance_screen") {
                    launchSingleTop = true
                }
                currentRoute.value = "performance_screen"
            },
            icon = { Icon(Icons.Filled.Info, contentDescription = "Performance") },
            label = {
                if (currentRoute.value == "performance_screen") {
                    Text("Advanced")
                }
            }
        )
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // Garante que o card ocupe toda a largura
            .fillMaxHeight(), // Garante que o card ocupe toda a altura
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

fun copyToClipboard(context: Context, text: String) {
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
    return try {
        // Tentando acessar os módulos carregados do kernel em /proc/modules
        val process = Runtime.getRuntime().exec("su -c cat /proc/modules")
        val modules = process.inputStream.bufferedReader().readText().trim()

        // Verifica se há módulos carregados
        if (modules.isNotEmpty()) {
            "LKM Active: Yes (Default for most devices)"
        } else {
            "LKM Active: No (No modules loaded. is abnormal)"
        }
    } catch (e: Exception) {
        "Unable to check LKM status"
    }
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

fun checkGKISupport(): String {
    return try {
        // Verificando se o arquivo /sys/kernel/gki existe, que indica suporte ao GKI
        val process = Runtime.getRuntime().exec("su -c cat /sys/kernel/gki")
        val gkiSupport = process.inputStream.bufferedReader().readText().trim()

        // Se o GKI estiver ativado, retornamos uma mensagem explicativa
        if (gkiSupport == "1") {
            // Verifica se o kernel é GKI ou não
            val isGKIKernel = checkFileExists2("/sys/kernel/gki/config")
            if (isGKIKernel) {
                "GKI Support: Yes\nKernel is GKI compatible.\nIt is possible to enable GKI on this device."
            } else {
                "GKI Support: Yes\nKernel is not currently using GKI.\nIt is possible to enable GKI if supported by your device."
            }
        } else {
            // Caso o GKI não esteja habilitado
            val canActivateGKI = checkFileExists2("/sys/kernel/gki/config")
            if (canActivateGKI) {
                "GKI Support: Yes\nKernel is compatible with GKI.\nHowever, GKI is not currently enabled on your device."
            } else {
                "GKI Support: No\nYour kernel is not GKI compatible.\nIt is not possible to enable GKI on this device.(No files/folders about GKI found)"
            }
        }
    } catch (e: Exception) {
        "Unable to check GKI support.\nError: ${e.message}"
    }
}

fun checkFileExists2(path: String): Boolean {
    return try {
        val process = Runtime.getRuntime().exec("su -c ls $path")
        process.waitFor() == 0
    } catch (e: Exception) {
        false
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InfoKernelTheme {
        KernelInfoApp()
    }
}