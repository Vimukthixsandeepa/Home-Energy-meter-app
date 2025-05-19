package com.example.powermeter

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import org.json.JSONObject

class ESP32Connection(
    private val ipAddress: String = "192.168.4.1",  // Default ESP32 IP
    private val port: Int = 80,  // Default port
    private val onDataReceived: (
        voltage: Double,
        current: Double,
        power: Double,
        energy: Double,
        frequency: Double,
        powerFactor: Double
    ) -> Unit,
    private val onConnectionStatusChanged: (Boolean) -> Unit
) {
    private var socket: Socket? = null
    private var isConnected = false
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    fun connect() {
        scope.launch {
            try {
                socket = Socket(ipAddress, port)
                isConnected = true
                onConnectionStatusChanged(true)
                startReading()
            } catch (e: Exception) {
                Log.e("ESP32Connection", "Connection failed: ${e.message}")
                onConnectionStatusChanged(false)
            }
        }
    }

    private fun startReading() {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
                while (isConnected) {
                    val data = reader.readLine()
                    data?.let { parseData(it) }
                    delay(1000) // Read every second
                }
            } catch (e: Exception) {
                Log.e("ESP32Connection", "Reading failed: ${e.message}")
                disconnect()
            }
        }
    }

    private suspend fun parseData(data: String) {
        try {
            val json = JSONObject(data)
            if (json.getString("status") == "success") {
                val voltage = json.getDouble("voltage")
                val current = json.getDouble("current")
                val power = json.getDouble("power")
                val energy = json.getDouble("energy")
                val frequency = json.getDouble("frequency")
                val pf = json.getDouble("pf")
                
                withContext(Dispatchers.Main) {
                    onDataReceived(voltage, current, power, energy, frequency, pf)
                }
            } else {
                Log.e("ESP32Connection", "Sensor read error: ${json.getString("message")}")
            }
        } catch (e: Exception) {
            Log.e("ESP32Connection", "Parse error: ${e.message}")
        }
    }

    fun disconnect() {
        isConnected = false
        socket?.close()
        socket = null
        onConnectionStatusChanged(false)
        scope.cancel()
    }

    protected fun finalize() {
        scope.cancel()
    }
} 