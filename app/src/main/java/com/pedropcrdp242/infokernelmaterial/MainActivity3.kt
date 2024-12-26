package com.pedropcrdp242.infokernelmaterial

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import okhttp3.Response
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.pedropcrdp242.infokernelmaterial.ui.theme.InfoKernelTheme
import kotlinx.coroutines.launch


class MainActivity3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InfoKernelTheme {
                SettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var isDialogOpen by remember { mutableStateOf(false) }

    // Obtém a configuração do tema do sistema (claro ou escuro)
    val configuration = LocalConfiguration.current
    val isDarkTheme = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    // Definindo as cores de acordo com o tema
    val topAppBarColor = if (isDarkTheme) Color.White else Color.Black
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About",
                        style = TextStyle(
                            color = topAppBarColor,
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = topAppBarColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AppInfoCard()

                // GitHub Repository
                Text("GitHub", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal, color = textColor))
                Spacer(modifier = Modifier.height(8.dp))
                CustomCard(
                    title = "Repository",
                    description = "Go to the GitHub Repository",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Pedropcrdp242/InfoKernel"))
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Credits
                Text("Credits", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal, color = textColor))
                Spacer(modifier = Modifier.height(8.dp))
                CustomCard(
                    title = "Licenses",
                    description = "Click to view the Licenses used in this project",
                    onClick = { isDialogOpen = true }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Collaborators
                Text("Collaborators", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal, color = textColor))
                Spacer(modifier = Modifier.height(8.dp))

                // Colaborador Pedro
                CustomCard(
                    title = "Pedropcrdp242",
                    description = "Developer",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Pedropcrdp242"))
                        context.startActivity(intent)
                    }
                )

                // Colaboradores de Contribuições
                CustomCard(
                    title = "Contributors",
                    description = "View the contributors to this project",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.github.com/repos/Pedropcrdp242/InfoKernel"))
                        context.startActivity(intent)
                    }
                )

                // Mostrar o diálogo com as bibliotecas usadas
                if (isDialogOpen) {
                    LicenseDialog(onDismiss = { isDialogOpen = false })
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Adicionando o AppInfoCard

            }
        }
    )
}

// Função para verificar atualizações
// Função para verificar atualizações
suspend fun checkForUpdates(githubRepoUrl: String, context: Context, onUpdateMessage: (String) -> Unit) {
    val client = OkHttpClient()
    val url = "$githubRepoUrl/releases"

    val request = Request.Builder().url(url).build()
    try {
        withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                val latestVersion = parseVersionFromResponse(jsonResponse)

                val currentVersion = "1.0.3" // A versão atual do app (pode vir de uma variável ou constante)

                withContext(Dispatchers.Main) {
                    // Atualiza a interface com a mensagem sobre a versão
                    if (latestVersion != currentVersion) {
                        onUpdateMessage("A nova versão $latestVersion está disponível!")
                    } else {
                        onUpdateMessage("Você já está com a versão mais recente!")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    // Mensagem de erro caso a resposta da API seja mal-sucedida
                    onUpdateMessage("Erro ao verificar atualizações: ${response.message}")
                }
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            // Mensagem de erro caso ocorra uma exceção
            onUpdateMessage("Erro de conexão: ${e.message}")
        }
    }
}

// Função para analisar a versão da resposta JSON
fun parseVersionFromResponse(response: String?): String {
    // Supondo que o JSON retorne algo como: {"tag_name": "v1.1.0"}
    // Aqui você pode usar uma biblioteca como Gson ou Moshi para parse do JSON
    return response?.let {
        val regex = """"tag_name":\s?"(v\d+\.\d+\.\d+)"""".toRegex()
        val matchResult = regex.find(it)
        matchResult?.groups?.get(1)?.value ?: "0.0.0"  // Caso não encontre, retorna "0.0.0"
    } ?: "0.0.0"  // Se a resposta for nula, retorna "0.0.0"
}

// Composable para a interface do app
@Composable
fun AppInfoCard() {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme() // Verifica se o tema é escuro
    val textColor = if (isDarkTheme) Color.White else Color.Black

    val appName = "InfoKernel"
    val appVersion = "1.0.3"
    val githubRepoUrl = "https://github.com/Pedropcrdp242/InfoKernel"
    val imagePainter = painterResource(id = R.drawable.ic_launcher_round) // Substitua pela URL ou drawable da imagem

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center, // Centraliza verticalmente
            horizontalAlignment = Alignment.CenterHorizontally // Centraliza horizontalmente
        ) {
            // Imagem do App
            Image(painter = imagePainter,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nome do App
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium.copy(color = textColor),
                fontWeight = FontWeight.Bold
            )

            // Versão do App
            Text(
                text = "Version: $appVersion",
                style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para verificar atualizações
            Row(
                modifier = Modifier.fillMaxWidth(), // Garante que o Row ocupe toda a largura
                horizontalArrangement = Arrangement.Center // Alinha o botão ao centro
            ) {
                TextButton(
                    onClick = { checkForUpdates(githubRepoUrl, context) }, // Passando o contexto
                    modifier = Modifier.padding(16.dp) // Espaçamento ao redor do botão
                ) {
                    Text(
                        text = "Check for Updates",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface // Cor do texto
                        )
                    )
                }
            }
        }
    }
}

// Função para simular a verificação de atualizações (não precisa ser composable)
fun checkForUpdates(githubRepoUrl: String, context: android.content.Context) {
    val url = "$githubRepoUrl/releases/latest"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
@Composable
fun CustomCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}


@Composable
fun LicenseDialog(onDismiss: () -> Unit) {
    val licenseText = """
        Apache License, Version 2.0, January 2004

        TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

        1. Definitions.
        "License" shall mean the terms and conditions for use, reproduction, and distribution as defined in this document.
        "Licensor" means the entity granting the License, which may include an individual, a corporation, or a government organization.
        "Contributor" means any entity that contributes to the creation of the Software, either by providing code, documentation, or other forms of contribution.
        
        2. Grant of Copyright License.
        The Licensor hereby grants to you, the Licensee, a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable copyright license to reproduce, prepare derivative works of, publicly display, publicly perform, sublicense, and distribute the Software and Derivative Works, including without limitation the rights to use, reproduce, and distribute copies of the Software, including the right to prepare derivative works based on the Software.

        3. Grant of Patent License.
        The Licensor grants you a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer the Software, where such license applies only to the claims of the Licensor's patent rights, which are infringed by the making, using, or selling of the Software.

        4. Conditions.
        You may not use, copy, modify, or distribute the Software except in compliance with the terms and conditions set forth in this License.

        5. Limitation of Liability.
        THE SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT.

        6. Miscellaneous.
        This License shall be governed by the laws of the jurisdiction where the Licensor is located, without regard to its conflict of law principles. This License does not grant any rights to use the trademarks of the Licensor.

        END OF TERMS AND CONDITIONS
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apache 2.0 License") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Permite rolar o texto
                    .padding(16.dp)
            ) {
                Text(licenseText, style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    InfoKernelTheme {
        SettingsScreen(
        )
    }
}