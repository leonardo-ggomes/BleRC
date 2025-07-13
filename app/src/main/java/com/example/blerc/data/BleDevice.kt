package com.example.blerc.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

data class BleDevice(val name: String?, val address: String)

fun BleDevice.toBluetoothDevice(): BluetoothDevice {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    return bluetoothAdapter.getRemoteDevice(this.address)
}