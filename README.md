
# 📘 BleRC

Projeto Android em Kotlin + Jetpack Compose para gerenciar um dispositivo BLE (ex: ESP32), incluindo:

* Escaneamento de dispositivos BLE
* Conexão ao serviço GATT
* Envio de comandos `"0"` e `"1"` via UTF‑8
* Feedback visual de status de conexão

---

## 🛠️ Funcionalidades

* ✅ Detecta dispositivos BLE próximos
* ✅ Conecta usando `BluetoothGatt`
* ✅ Descobre serviços e características no ESP32
* ✅ Envia comandos `"0"` e `"1"` em UTF‑8
* ✅ UI dinâmica com Jetpack Compose (lista, botões, status)

---

## 🧩 Fluxo do usuário / uso

1. Abrir o app (MainActivity).
2. É pedido para ativar o Bluetooth, se necessário.
3. Permissão de localização é solicitada (Android 11).
4. Toque em **“Escanear BLE”** para listar dispositivos encontrados.
5. Toque no dispositivo desejado para conectar.
6. Use botões **“Enviar 0”** ou **“Enviar 1”** para enviar o comando.
7. Visualize o status (“Conectado”, “Desconectado”, etc).
8. Opcional: botão para desconectar.

---

## ⚙️ Setup

1. Clone o repositório/fork.

2. Abra no Android Studio.

3. No `AndroidManifest.xml`, inclua permissão:

   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
   ```

4. As permissões são solicitadas na inicialização com `rememberLauncherForActivityResult`.

---

## 📦 Arquitetura

* `BleManager` (classe central)

  * `scanLeDevices()` → inicia/para escaneamento
  * `connectToDevice(device: BluetoothDevice)` → conecta
  * `sendCommandUtf8("0"/"1")` → envia comandos
  * `disconnect()` → desconecta
  * `isConnected`, `connectionStatus`, `bleDevices` → estados observáveis

* UI com Compose:

  * **BluetoothEnableScreen**: ativa Bluetooth
  * **ScannerScreen**: botão para escanear
  * **BleDeviceListScreen**: lista de dispositivos
  * **CommandButtons**: botões para envio de comando

---

## 🔬 Internals BLE

* Usa `BluetoothLeScanner` e `ScanCallback` para descoberta.
* Usa `BluetoothGattCallback` para:

  * Conexão (`STATE_CONNECTED`, `discoverServices`)
  * Descoberta de serviços
  * Envio de texto via `writeCharacteristic`

GATT UUIDs do ESP32:

```kotlin
val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
```

---

## 🧪 Depuração

* Logs em `onServicesDiscovered` mostram status e lista de serviços.
* `onCharacteristicWrite` dá feedback sobre sucesso do envio.
* Use `nRF Connect` para verificar se o ESP32 está realmente anunciando o UUID no advertising.

---

## 🔄 Possíveis próximos passos

* Adicionar reconexão automática em caso de queda.
* Permitir leitura da característica.
* Melhorar UI com mensagens de erro, reconectar etc.
* Migrar para a biblioteca Kable + coroutines para simplificar BLE.
* Criar unidade de testes para `BleManager`.

---

## 🤝 Contribuições

* Fork este projeto
* Faça alterações
* Abra pull request

---

## 📃 Licença

MIT

---


