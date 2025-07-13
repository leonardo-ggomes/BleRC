package com.example.blerc.presenter


import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blerc.data.toBluetoothDevice
import com.example.blerc.signal.BleManager

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BluetoothEnableScreen(context: Context) {
    val bleManager = remember { BleManager(context) }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Bluetooth ativado!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Bluetooth não ativado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Gerenciador BLE", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val intent = bleManager.getEnableBluetoothIntent()
            if (intent != null) {
                launcher.launch(intent)
            } else {
                Toast.makeText(context, "Bluetooth já está ativado", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Ativar Bluetooth")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            bleManager.scanLeDevices()
        }) {
            Text("Escanear dispositivos BLE")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CommandButtonsUtf8(onSendCommand = { command ->
            bleManager.sendCommandUtf8(command)
        })

        BleDeviceListScreen(bleManager)

    }
}

@Composable
fun BleDeviceListScreen(bleManager: BleManager) {
    val devices = bleManager.getDevices()

    Text("Status: ${bleManager.connectionStatus.value}")
    if (devices.isEmpty()) {
        Text("Nenhum dispositivo encontrado")

    }

    LazyColumn {
        items(devices) { device ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        bleManager.connectToDevice(device.toBluetoothDevice())
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nome: ${device.name}")
                    Text("Endereço: ${device.address}")
                }
                Column(modifier = Modifier.padding(16.dp)){
                    Box(modifier = Modifier.padding(5.dp).clickable {
                        bleManager.disconnect()
                    }){
                        Text("desconectar")
                    }
                }
            }
        }
    }
}

@Composable
fun CommandButtonsUtf8(onSendCommand: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = { onSendCommand("0") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("On")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSendCommand("1") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Off")
        }
    }
}