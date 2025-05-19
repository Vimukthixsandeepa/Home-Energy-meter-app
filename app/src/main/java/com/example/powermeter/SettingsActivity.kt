package com.example.powermeter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {
    private lateinit var tier1Rate: TextInputEditText
    private lateinit var tier2Rate: TextInputEditText
    private lateinit var tier3Rate: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button

    // Default rates
    private val defaultRates = mapOf(
        "tier1" to 32.0,  // Rs. 32.00/kWh for 0-30 kWh
        "tier2" to 42.0,  // Rs. 42.00/kWh for 31-60 kWh
        "tier3" to 50.0   // Rs. 50.00/kWh for >60 kWh
    )

    companion object {
        private const val TAG = "SettingsActivity"
        const val PREFS_NAME = "PowerMeterPrefs"
        const val KEY_TIER1_RATE = "tier1_rate"
        const val KEY_TIER2_RATE = "tier2_rate"
        const val KEY_TIER3_RATE = "tier3_rate"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views
        tier1Rate = findViewById(R.id.tier1Rate)
        tier2Rate = findViewById(R.id.tier2Rate)
        tier3Rate = findViewById(R.id.tier3Rate)
        saveButton = findViewById(R.id.saveButton)
        resetButton = findViewById(R.id.resetButton)

        // Load saved rates
        loadSavedRates()

        // Set up button click listeners
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveRates()
                Toast.makeText(this, "Rates saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        resetButton.setOnClickListener {
            resetToDefaultRates()
            Toast.makeText(this, "Rates reset to default values", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedRates() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Load rates with verification
            val tier1 = prefs.getFloat(KEY_TIER1_RATE, defaultRates["tier1"]?.toFloat() ?: 0.12f)
            val tier2 = prefs.getFloat(KEY_TIER2_RATE, defaultRates["tier2"]?.toFloat() ?: 0.15f)
            val tier3 = prefs.getFloat(KEY_TIER3_RATE, defaultRates["tier3"]?.toFloat() ?: 0.18f)
            
            Log.d(TAG, "Loaded rates: T1=$tier1, T2=$tier2, T3=$tier3")
            
            // Update UI
            tier1Rate.setText(tier1.toString())
            tier2Rate.setText(tier2.toString())
            tier3Rate.setText(tier3.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading rates", e)
            Toast.makeText(this, "Error loading rates. Using defaults.", Toast.LENGTH_SHORT).show()
            resetToDefaultRates()
        }
    }

    private fun saveRates() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(prefs.edit()) {
                // Get values with validation
                val tier1 = validateAndParseRate(tier1Rate.text.toString(), "Tier 1")
                val tier2 = validateAndParseRate(tier2Rate.text.toString(), "Tier 2")
                val tier3 = validateAndParseRate(tier3Rate.text.toString(), "Tier 3")

                // Save values
                putFloat(KEY_TIER1_RATE, tier1)
                putFloat(KEY_TIER2_RATE, tier2)
                putFloat(KEY_TIER3_RATE, tier3)
                
                // Verify the save operation
                if (commit()) {
                    Log.d(TAG, "Rates saved successfully: T1=$tier1, T2=$tier2, T3=$tier3")
                    verifyRatesSaved(prefs, tier1, tier2, tier3)
                    Toast.makeText(this@SettingsActivity, "Rates saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Failed to save rates")
                    Toast.makeText(this@SettingsActivity, "Failed to save rates", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving rates", e)
            Toast.makeText(this, "Error saving rates: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndParseRate(input: String, tierName: String): Float {
        return try {
            val rate = input.toFloat()
            if (rate < 0) {
                throw IllegalArgumentException("$tierName rate cannot be negative")
            }
            rate
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid rate format for $tierName: $input")
            throw IllegalArgumentException("Invalid rate format for $tierName")
        }
    }

    private fun verifyRatesSaved(prefs: SharedPreferences, tier1: Float, tier2: Float, tier3: Float) {
        val savedTier1 = prefs.getFloat(KEY_TIER1_RATE, -1f)
        val savedTier2 = prefs.getFloat(KEY_TIER2_RATE, -1f)
        val savedTier3 = prefs.getFloat(KEY_TIER3_RATE, -1f)

        if (savedTier1 != tier1 || savedTier2 != tier2 || savedTier3 != tier3) {
            Log.e(TAG, """
                Rate verification failed:
                T1: expected=$tier1, actual=$savedTier1
                T2: expected=$tier2, actual=$savedTier2
                T3: expected=$tier3, actual=$savedTier3
            """.trimIndent())
        }
    }

    private fun resetToDefaultRates() {
        // Set EditText fields to default values
        tier1Rate.setText(defaultRates["tier1"].toString())
        tier2Rate.setText(defaultRates["tier2"].toString())
        tier3Rate.setText(defaultRates["tier3"].toString())

        // Save default values to SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putFloat(KEY_TIER1_RATE, defaultRates["tier1"]?.toFloat() ?: 0.12f)
            putFloat(KEY_TIER2_RATE, defaultRates["tier2"]?.toFloat() ?: 0.15f)
            putFloat(KEY_TIER3_RATE, defaultRates["tier3"]?.toFloat() ?: 0.18f)
            apply()
        }
    }

    private fun validateInputs(): Boolean {
        val rates = listOf(
            tier1Rate.text.toString(),
            tier2Rate.text.toString(),
            tier3Rate.text.toString()
        )

        // Check for empty fields
        if (rates.any { it.isBlank() }) {
            Toast.makeText(this, "Please fill in all rate fields", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check for valid numbers
        try {
            rates.forEach { it.toFloat() }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check for negative values
        if (rates.any { it.toFloat() < 0 }) {
            Toast.makeText(this, "Rates cannot be negative", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
} 