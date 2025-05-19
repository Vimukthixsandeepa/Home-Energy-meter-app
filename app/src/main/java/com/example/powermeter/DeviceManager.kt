package com.example.powermeter

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DeviceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "ESP32Devices"
        private const val KEY_DEVICES = "devices"
    }

    fun saveDevice(device: ESP32Device) {
        val devices = getDevices().toMutableList()
        devices.add(device)
        saveDevices(devices)
    }

    fun updateDevice(device: ESP32Device) {
        val devices = getDevices().toMutableList()
        val index = devices.indexOfFirst { it.id == device.id }
        if (index != -1) {
            devices[index] = device
            saveDevices(devices)
        }
    }

    fun deleteDevice(deviceId: String) {
        val devices = getDevices().toMutableList()
        devices.removeAll { it.id == deviceId }
        saveDevices(devices)
    }

    fun getDevices(): List<ESP32Device> {
        val json = prefs.getString(KEY_DEVICES, "[]")
        val type = object : TypeToken<List<ESP32Device>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveDevices(devices: List<ESP32Device>) {
        val json = gson.toJson(devices)
        prefs.edit().putString(KEY_DEVICES, json).apply()
    }
} 