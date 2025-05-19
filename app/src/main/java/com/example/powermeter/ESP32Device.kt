package com.example.powermeter

data class ESP32Device(
    val id: String,
    val name: String,
    val ipAddress: String,
    val location: String = ""
) 