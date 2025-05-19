package com.example.powermeter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class BillCalculator(private val context: Context) {
    companion object {
        const val PREFS_NAME = "PowerMeterPrefs"
        const val KEY_TIER1_RATE = "tier1_rate"
        const val KEY_TIER2_RATE = "tier2_rate"
        const val KEY_TIER3_RATE = "tier3_rate"
        private const val TAG = "BillCalculator"
        private const val DEFAULT_RATE_1 = 32.0f  // Rs. 32/kWh
        private const val DEFAULT_RATE_2 = 42.0f  // Rs. 42/kWh
        private const val DEFAULT_RATE_3 = 50.0f  // Rs. 50/kWh
    }

    fun calculateBill(energyConsumption: Double): Double {
        if (energyConsumption < 0) {
            throw IllegalArgumentException("Energy consumption cannot be negative")
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Load rates with debug logging
        val tier1Rate = prefs.getFloat(KEY_TIER1_RATE, DEFAULT_RATE_1).toDouble()
        val tier2Rate = prefs.getFloat(KEY_TIER2_RATE, DEFAULT_RATE_2).toDouble()
        val tier3Rate = prefs.getFloat(KEY_TIER3_RATE, DEFAULT_RATE_3).toDouble()
        
        Log.d(TAG, "Calculating bill with rates: T1=$tier1Rate, T2=$tier2Rate, T3=$tier3Rate")
        Log.d(TAG, "Energy consumption: $energyConsumption kWh")
        
        val bill = when {
            energyConsumption <= 30 -> {
                val cost = energyConsumption * tier1Rate
                Log.d(TAG, "Tier 1 calculation: $energyConsumption kWh * $tier1Rate = $cost")
                cost
            }
            energyConsumption <= 60 -> {
                val tier1Cost = 30 * tier1Rate
                val tier2Cost = (energyConsumption - 30) * tier2Rate
                val totalCost = tier1Cost + tier2Cost
                Log.d(TAG, """
                    Tier 1 & 2 calculation:
                    T1: 30 kWh * $tier1Rate = $tier1Cost
                    T2: ${energyConsumption - 30} kWh * $tier2Rate = $tier2Cost
                    Total: $totalCost
                """.trimIndent())
                totalCost
            }
            else -> {
                val tier1Cost = 30 * tier1Rate
                val tier2Cost = 30 * tier2Rate
                val tier3Cost = (energyConsumption - 60) * tier3Rate
                val totalCost = tier1Cost + tier2Cost + tier3Cost
                Log.d(TAG, """
                    All tiers calculation:
                    T1: 30 kWh * $tier1Rate = $tier1Cost
                    T2: 30 kWh * $tier2Rate = $tier2Cost
                    T3: ${energyConsumption - 60} kWh * $tier3Rate = $tier3Cost
                    Total: $totalCost
                """.trimIndent())
                totalCost
            }
        }
        
        Log.d(TAG, "Final bill amount: $bill")
        return bill
    }

    private fun loadRate(prefs: SharedPreferences, key: String, defaultValue: Float): Double {
        val rate = try {
            prefs.getFloat(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading rate for $key", e)
            defaultValue
        }
        Log.d(TAG, "Loaded rate for $key: $rate")
        return rate.toDouble()
    }
} 