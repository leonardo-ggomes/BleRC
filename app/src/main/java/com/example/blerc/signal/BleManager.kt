package com.example.blerc.signal

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.ActivityCompat
import com.example.blerc.data.BleDevice
import java.util.UUID

class BleManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private var bluetoothGatt: BluetoothGatt? = null

    private val handler = Handler(Looper.getMainLooper())
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    var isConnected = mutableStateOf(false)
    var connectionStatus = mutableStateOf("Desconectado")

    private val bleDevices = mutableStateListOf<BleDevice>()
    fun getDevices(): SnapshotStateList<BleDevice> = bleDevices


    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    fun getEnableBluetoothIntent(): Intent? {
        return if (bluetoothAdapter?.isEnabled == false) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        } else {
            null
        }
    }

    fun scanLeDevices(){
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context, "Permissão BLUETOOTH_SCAN negada", Toast.LENGTH_SHORT).show()
                    return@postDelayed
                }
                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device

            val deviceName = if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                "Permissão negada"
            } else {
                device.name ?: "Não identificado"

            }

            val address = device.address
            Log.e("BLE", "Dispositivo encontrado: $deviceName [$address]")

            if (bleDevices.none { it.address == address }) {
                bleDevices.add(BleDevice(deviceName, address))
            }

        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permissão BLUETOOTH_CONNECT negada", Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.i("BLE", "✅ Conectado com sucesso!")
                        connectionStatus.value = "Conectado"
                        isConnected.value = true

                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.w("BLE", "🔌 Bluetooth sem permissão")
                            return
                        }

                        bluetoothGatt = gatt
                        gatt.discoverServices()

                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.w("BLE", "🔌 Dispositivo desconectado.")
                    }
                }
            } else {
                Log.e("BLE", "❌ Erro na conexão (status: $status)")
                gatt.close()
            }
        }

        /*

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.services.forEach { service ->
                    Log.i("BLE DEBG", "Service UUID: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.i("BLE DEBG", "↳ Characteristic UUID: ${characteristic.uuid}")
                    }
                }
            }
        }
        */


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(" TESTE", "Serviços descobertos com sucesso")

                // ✅ Aqui já é seguro chamar getService()
                val service = gatt.getService(SERVICE_UUID)
                if (service == null) {
                    Log.e("BLE TESTE", "Serviço não encontrado após descoberta")
                } else {
                    // Exemplo: armazenar para uso posterior
                    bluetoothGatt = gatt
                }

            } else {
                Log.e("BLE TESTE", "Erro ao descobrir serviços. Status: $status")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BLE", "✉️ Comando enviado com sucesso!")
            } else {
                Log.e("BLE", "⚠️ Falha ao enviar comando! Status: $status")
            }
        }

    }

    fun disconnect() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        connectionStatus.value = "Desconectado"
        isConnected.value = false
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }

    fun sendCommandUtf8(command: String) {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID) ?: return

        characteristic.value = command.toByteArray(Charsets.UTF_8)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Sem permissão para conectar", Toast.LENGTH_SHORT).show()
            return
        }

        val success = gatt.writeCharacteristic(characteristic)
        Log.i("BLE", if (success) "✅ Comando '$command' enviado com sucesso!" else "❌ Falha ao enviar '$command'")
    }
}
