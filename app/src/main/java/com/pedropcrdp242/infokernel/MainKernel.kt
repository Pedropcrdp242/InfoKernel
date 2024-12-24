package com.pedropcrdp242.infokernel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.scottyab.rootbeer.RootBeer
import java.io.File
import android.content.ClipData
import android.content.ClipboardManager

class MainKernel : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_kernel)

        val rootStatusTextView: TextView = findViewById(R.id.root_status)
        val kernelInfoTextView: TextView = findViewById(R.id.kernel_info)
        val lkmStatusTextView: TextView = findViewById(R.id.lkm_status)
        val modulesTextView: TextView = findViewById(R.id.modules_info)
        val copyButton: MaterialButton = findViewById(R.id.copy_button)

        //copy info
        copyButton.setOnClickListener {
            val infoToCopy = StringBuilder()
            infoToCopy.append("=== Root Status ===\n")
            infoToCopy.append(rootStatusTextView.text)
            infoToCopy.append("\n\n=== Kernel Info ===\n")
            infoToCopy.append(kernelInfoTextView.text)
            infoToCopy.append("\n\n=== LKM Status ===\n")
            infoToCopy.append(lkmStatusTextView.text)
            infoToCopy.append("\n\n=== Kernel Modules ===\n")
            infoToCopy.append(modulesTextView.text)

            copyToClipboard(infoToCopy.toString())
        }

        // Inicializando botões
        val updateButton: MaterialButton = findViewById(R.id.update_button)

        // Verificar status do root e informações do kernel inicialmente
        checkRootStatus(rootStatusTextView)
        getKernelInfo(kernelInfoTextView)
        checkLKMStatus(lkmStatusTextView)
        getKernelModules(modulesTextView)

        // Função de clique do botão "Atualizar Status"
        updateButton.setOnClickListener {
            checkRootStatus(rootStatusTextView)
            getKernelInfo(kernelInfoTextView)
            checkLKMStatus(lkmStatusTextView)
            getKernelModules(modulesTextView)
        }

    }

    fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Kernel Info", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Information copied to clipboard", Toast.LENGTH_SHORT).show()
    }


    private fun checkRootStatus(rootStatusTextView: TextView) {
        val rootBeer = RootBeer(this)
        val statusText = StringBuilder()

        statusText.append("=== Do I have root? ===\n\n")
        if (rootBeer.isRooted) {
            val rootType = getRootType()
            statusText.append("Root Detected:\nType: $rootType")
        } else {
            statusText.append("Root Not Detected :(")
        }

        rootStatusTextView.text = statusText.toString()
    }

    private fun getRootType(): String {
        return when {
            checkMagisk() -> "Magisk"
            checkKernelSU() -> "KernelSU"
            else -> "unknown :/"
        }
    }

    private fun checkMagisk(): Boolean {
        return checkFileExists("/sbin/.magisk") || checkFileExists("/data/adb/magisk")
    }

    private fun checkKernelSU(): Boolean {
        return checkFileExists("/data/adb/ksu") || checkFileExists("/data/adb/modules/ksu")
    }

    private fun checkFileExists(path: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c ls $path")
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getKernelInfo(kernelInfoTextView: TextView) {
        val kernelInfo = StringBuilder()

        // Obter informações básicas do kernel
        kernelInfo.append("=== Basic kernel information ===\n\n")
        kernelInfo.append("Kernel Version:\n${getKernelVersion()}\n\n")
        kernelInfo.append("Arch:\n${getArchitecture()}\n\n")

        kernelInfoTextView.text = kernelInfo.toString()
    }

    private fun getKernelVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec("uname -r")
            process.inputStream.bufferedReader().use { it.readText().trim() }
        } catch (e: Exception) {
            "Error Unable to Get Kernel Information"
        }
    }

    private fun getArchitecture(): String {
        return try {
            val process = Runtime.getRuntime().exec("uname -m")
            process.inputStream.bufferedReader().use { it.readText().trim() }
        } catch (e: Exception) {
            "Error Unable to Get Arch Information"
        }
    }

    private fun checkLKMStatus(lkmStatusTextView: TextView) {
        val isLkm = isLkmKernel()
        lkmStatusTextView.text = "LKM?: ${if (isLkm) "Yes :)" else "No :/"}"
    }

    private fun isLkmKernel(): Boolean {
        val command = "ls /system/lib/modules"  // Verificando o diretório de módulos
        return try {
            val process = Runtime.getRuntime().exec("su -c $command")
            val output = process.inputStream.bufferedReader().use { it.readText() }

            output.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }



    private fun getKernelModules(modulesTextView: TextView) {
        try {
            val process = Runtime.getRuntime().exec("su -c lsmod")
            val modules = process.inputStream.bufferedReader().use { it.readText() }
            val formattedModules = if (modules.isNotEmpty()) {
                "=== Advanced Kernel Information ===\n\n$modules"
            } else {
                "Unable To Get Advanced Kernel Info"
            }
            modulesTextView.text = formattedModules
        } catch (e: Exception) {
            modulesTextView.text = "Error Kernel Modules Not Listed"
        }
    }
}
