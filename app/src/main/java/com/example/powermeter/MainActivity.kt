package com.example.powermeter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.os.Handler
import android.os.Looper
import kotlin.random.Random
import java.util.Calendar
import android.content.Intent
import android.content.Context
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.BarEntry
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import java.text.SimpleDateFormat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var statsAdapter: PowerStatAdapter
    private lateinit var costValue: TextView
    private lateinit var costTier: TextView
    private lateinit var deviceName: TextView
    private lateinit var lastUpdate: TextView
    private lateinit var dailyUsageChart: LineChart
    private lateinit var monthlyUsageChart: BarChart
    
    private var totalEnergy = 0.0
    private var lastCalculationTime: Long = System.currentTimeMillis()
    private var peakDemand: Double = 0.0

    private lateinit var esp32Connection: ESP32Connection
    private lateinit var deviceManager: DeviceManager
    private var isESP32Connected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        
        setupViews()
        setupRecyclerView()
        setupCharts()
        initializeESP32Connection()
    }

    private fun setupViews() {
        costValue = findViewById(R.id.costValue)
        costTier = findViewById(R.id.costTier)
        deviceName = findViewById(R.id.deviceName)
        lastUpdate = findViewById(R.id.lastUpdate)
        dailyUsageChart = findViewById(R.id.dailyUsageChart)
        monthlyUsageChart = findViewById(R.id.monthlyUsageChart)

        // Add settings button setup
        val settingsButton = findViewById<ExtendedFloatingActionButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        }
    }

    private fun setupRecyclerView() {
        val statsRecyclerView = findViewById<RecyclerView>(R.id.statsGrid)
        statsAdapter = PowerStatAdapter()
        statsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = statsAdapter
        }
    }

    private fun updateReadings(
        voltage: Double,
        current: Double,
        power: Double,
        energy: Double,
        frequency: Double,
        powerFactor: Double
    ) {
        val stats = listOf(
            PowerStat(
                R.drawable.ic_voltage,
                "Voltage",
                String.format("%.1f V", voltage),
                getColor(R.color.voltage_card)
            ),
            PowerStat(
                R.drawable.ic_current,
                "Current",
                String.format("%.2f A (PF: %.2f)", current, powerFactor),
                getColor(R.color.current_card)
            ),
            PowerStat(
                R.drawable.ic_power,
                "Power",
                String.format("%.1f W (%.1f Hz)", power, frequency),
                getColor(R.color.power_card)
            ),
            PowerStat(
                R.drawable.ic_energy,
                "Energy",
                String.format("%.3f kWh", energy),
                getColor(R.color.energy_card)
            )
        )
        
        statsAdapter.updateStats(stats)
        calculateAndUpdateEnergy(power)
        updateCharts(power)
        
        // Update last update time
        lastUpdate.text = "Last Update: ${getCurrentTime()}"
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun setupCharts() {
        setupDailyChart()
        setupMonthlyChart()
    }

    private fun setupDailyChart() {
        dailyUsageChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            setDrawBorders(false)
            
            // Customize appearance
            animateX(1000)
            setBackgroundColor(resources.getColor(R.color.surface))
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(getHourLabels())
                labelCount = 12  // Show fewer labels to avoid crowding
                granularity = 1f
                textColor = resources.getColor(R.color.text_primary)
                setDrawGridLines(false)
                setDrawAxisLine(true)
            }
            
            axisLeft.apply {
                axisMinimum = 0f
                textColor = resources.getColor(R.color.text_primary)
                setDrawGridLines(true)
                gridColor = resources.getColor(R.color.chart_grid)
                gridLineWidth = 0.5f
                setDrawAxisLine(true)
                axisLineColor = resources.getColor(R.color.text_primary)
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = true
                textColor = resources.getColor(R.color.text_primary)
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                form = Legend.LegendForm.LINE
                formSize = 12f
                textSize = 12f
                formLineWidth = 2f
            }
        }
    }

    private fun setupMonthlyChart() {
        monthlyUsageChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            setDrawBorders(false)
            
            // Customize appearance
            animateY(1000)
            setBackgroundColor(resources.getColor(R.color.surface))
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(getMonthLabels())
                labelCount = 12
                granularity = 1f
                textColor = resources.getColor(R.color.text_primary)
                setDrawGridLines(false)
                setDrawAxisLine(true)
            }
            
            axisLeft.apply {
                axisMinimum = 0f
                textColor = resources.getColor(R.color.text_primary)
                setDrawGridLines(true)
                gridColor = resources.getColor(R.color.chart_grid)
                gridLineWidth = 0.5f
                setDrawAxisLine(true)
                axisLineColor = resources.getColor(R.color.text_primary)
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = true
                textColor = resources.getColor(R.color.text_primary)
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                form = Legend.LegendForm.SQUARE
                formSize = 12f
                textSize = 12f
            }
        }
    }

    private fun getHourLabels(): Array<String> {
        return Array(24) { hour -> 
            String.format("%02d:00", hour)
        }
    }

    private fun getMonthLabels(): Array<String> {
        return arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    private fun updateCharts(power: Double) {
        updateDailyChart(power)
        updateMonthlyChart()
    }

    private val hourlyReadings = mutableListOf<Entry>()
    private val monthlyReadings = mutableListOf<BarEntry>()

    private fun updateDailyChart(power: Double) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        hourlyReadings.add(Entry(hour.toFloat(), power.toFloat()))
        
        while (hourlyReadings.size > 24) {
            hourlyReadings.removeAt(0)
        }
        
        val dataSet = LineDataSet(hourlyReadings, "Power Usage (W)").apply {
            color = resources.getColor(R.color.chart_line)
            setDrawCircles(true)
            circleRadius = 4f
            circleColors = listOf(resources.getColor(R.color.chart_line))
            setDrawCircleHole(true)
            circleHoleRadius = 2f
            lineWidth = 2.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = resources.getColor(R.color.chart_fill)
            fillAlpha = 50
            valueTextSize = 10f
            valueTextColor = resources.getColor(R.color.text_primary)
        }
        
        dailyUsageChart.data = LineData(dataSet)
        dailyUsageChart.invalidate()
    }

    private fun updateMonthlyChart() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        
        if (monthlyReadings.size <= month) {
            monthlyReadings.add(BarEntry(month.toFloat(), totalEnergy.toFloat()))
        } else {
            monthlyReadings[month] = BarEntry(month.toFloat(), totalEnergy.toFloat())
        }
        
        val dataSet = BarDataSet(monthlyReadings, "Monthly Energy (kWh)").apply {
            color = resources.getColor(R.color.chart_line)
            valueTextSize = 10f
            valueTextColor = resources.getColor(R.color.text_primary)
            setDrawValues(true)
            highLightColor = resources.getColor(R.color.accent)
        }
        
        monthlyUsageChart.data = BarData(dataSet).apply {
            barWidth = 0.7f
        }
        monthlyUsageChart.invalidate()
    }

    private fun initializeESP32Connection() {
        // Initialize ESP32 connection with default values
        esp32Connection = ESP32Connection(
            onDataReceived = { voltage, current, power, energy, frequency, pf ->
                updateReadings(voltage, current, power, energy, frequency, pf)
            },
            onConnectionStatusChanged = { isConnected ->
                updateConnectionStatus(isConnected)
            }
        )

        // Initialize device manager
        deviceManager = DeviceManager(this)

        // Add connect button click listener
        val connectButton = findViewById<Button>(R.id.connectButton)
        connectButton.setOnClickListener {
            if (!isESP32Connected) {
                showDeviceSelectionDialog()
            } else {
                esp32Connection.disconnect()
            }
        }

        // Add device button
        val addDeviceButton = findViewById<Button>(R.id.addDeviceButton)
        addDeviceButton.setOnClickListener {
            showAddDeviceDialog()
        }
    }

    private fun calculateAndUpdateEnergy(power: Double) {
        // Calculate time elapsed since last calculation in hours
        val currentTime = System.currentTimeMillis()
        val elapsedHours = (currentTime - lastCalculationTime) / (1000.0 * 3600.0)
        lastCalculationTime = currentTime
        
        // Calculate energy consumption
        val energyThisPeriod = power * elapsedHours / 1000.0 // Convert to kWh
        totalEnergy += energyThisPeriod
        
        // Calculate cost
        val cost = calculateTieredBill(totalEnergy)
        
        // Calculate demand charge
        val demandCharge = (peakDemand / 1000.0) * 1000.0 // Rs. 1000 per kW of peak demand
        val totalCost = cost + demandCharge

        // Update UI
        costValue.text = String.format("Rs. %.2f", totalCost)
        
        val currentTier = when {
            totalEnergy <= 30 -> "Tier 1"
            totalEnergy <= 60 -> "Tier 2"
            else -> "Tier 3"
        }
        costTier.text = currentTier
    }

    private fun calculateTieredBill(energyConsumption: Double): Double {
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        
        val tier1Rate = prefs.getFloat(SettingsActivity.KEY_TIER1_RATE, 32.0f).toDouble()
        val tier2Rate = prefs.getFloat(SettingsActivity.KEY_TIER2_RATE, 42.0f).toDouble()
        val tier3Rate = prefs.getFloat(SettingsActivity.KEY_TIER3_RATE, 50.0f).toDouble()
        
        return when {
            energyConsumption <= 30 -> {
                // Only Tier 1
                energyConsumption * tier1Rate
            }
            energyConsumption <= 60 -> {
                // Tier 1 + Tier 2
                (30 * tier1Rate) + 
                ((energyConsumption - 30) * tier2Rate)
            }
            else -> {
                // Tier 1 + Tier 2 + Tier 3
                (30 * tier1Rate) + 
                (30 * tier2Rate) + 
                ((energyConsumption - 60) * tier3Rate)
            }
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        isESP32Connected = isConnected
        val connectButton = findViewById<Button>(R.id.connectButton)
        connectButton.text = if (isConnected) "Disconnect" else "Connect"
        connectButton.setBackgroundColor(
            resources.getColor(if (isConnected) R.color.connected else R.color.disconnected)
        )
        
        deviceName.text = if (isConnected) {
            currentDevice?.name ?: "Connected Device"
        } else {
            "Not Connected"
        }
    }

    private fun showDeviceSelectionDialog() {
        val devices = deviceManager.getDevices()
        if (devices.isEmpty()) {
            Toast.makeText(this, "No devices added. Please add a device first.", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceNames = devices.map { "${it.name} (${it.location})" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Select ESP32 Device")
            .setItems(deviceNames) { _, which ->
                val selectedDevice = devices[which]
                currentDevice = selectedDevice
                connectToDevice(selectedDevice)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddDeviceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_esp32, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.deviceNameInput)
        val ipInput = dialogView.findViewById<TextInputEditText>(R.id.ipAddressInput)
        val locationInput = dialogView.findViewById<TextInputEditText>(R.id.locationInput)

        AlertDialog.Builder(this)
            .setTitle("Add ESP32 Device")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val device = ESP32Device(
                    id = System.currentTimeMillis().toString(),
                    name = nameInput.text.toString(),
                    ipAddress = ipInput.text.toString(),
                    location = locationInput.text.toString()
                )
                deviceManager.saveDevice(device)
                Toast.makeText(this, "Device added successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun connectToDevice(device: ESP32Device) {
        esp32Connection = ESP32Connection(
            ipAddress = device.ipAddress,
            onDataReceived = { voltage, current, power, energy, frequency, pf ->
                updateReadings(voltage, current, power, energy, frequency, pf)
            },
            onConnectionStatusChanged = { isConnected ->
                updateConnectionStatus(isConnected)
            }
        )
        esp32Connection.connect()
    }

    companion object {
        private const val SETTINGS_REQUEST_CODE = 100
    }

    private var currentDevice: ESP32Device? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the display with new rates
            if (isESP32Connected) {
                // If connected, the next data update will use new rates
                Toast.makeText(this, "Rate settings updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh rates when returning to the activity
        val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val tier1 = prefs.getFloat(SettingsActivity.KEY_TIER1_RATE, 32.0f)
        val tier2 = prefs.getFloat(SettingsActivity.KEY_TIER2_RATE, 42.0f)
        val tier3 = prefs.getFloat(SettingsActivity.KEY_TIER3_RATE, 50.0f)
        
        Log.d("MainActivity", """
            Current rates:
            Tier 1: Rs. $tier1/kWh
            Tier 2: Rs. $tier2/kWh
            Tier 3: Rs. $tier3/kWh
        """.trimIndent())
    }
} 