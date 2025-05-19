package com.example.powermeter

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class BillCalculationTest {
    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var sharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var calculator: BillCalculator

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Setup SharedPreferences mock
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        
        // Default test rates
        `when`(sharedPreferences.getFloat(eq("tier1_rate"), anyFloat())).thenReturn(0.12f)
        `when`(sharedPreferences.getFloat(eq("tier2_rate"), anyFloat())).thenReturn(0.15f)
        `when`(sharedPreferences.getFloat(eq("tier3_rate"), anyFloat())).thenReturn(0.18f)
        
        calculator = BillCalculator(context)
    }

    @Test
    fun `test tier 1 consumption`() {
        // Test consumption within tier 1 (0-30 kWh)
        val consumption = 25.0
        val expectedBill = 25.0 * 0.12 // 3.00
        
        val calculatedBill = calculator.calculateBill(consumption)
        assertEquals(expectedBill, calculatedBill, 0.01)
    }

    @Test
    fun `test tier 1 boundary`() {
        // Test consumption at tier 1 boundary (30 kWh)
        val consumption = 30.0
        val expectedBill = 30.0 * 0.12 // 3.60
        
        val calculatedBill = calculator.calculateBill(consumption)
        assertEquals(expectedBill, calculatedBill, 0.01)
    }

    @Test
    fun `test tier 2 consumption`() {
        // Test consumption within tier 2 (31-60 kWh)
        val consumption = 45.0
        val expectedBill = (30.0 * 0.12) + // Tier 1: 3.60
                          (15.0 * 0.15)    // Tier 2: 2.25
                          // Total: 5.85
        
        val calculatedBill = calculator.calculateBill(consumption)
        assertEquals(expectedBill, calculatedBill, 0.01)
    }

    @Test
    fun `test tier 2 boundary`() {
        // Test consumption at tier 2 boundary (60 kWh)
        val consumption = 60.0
        val expectedBill = (30.0 * 0.12) + // Tier 1: 3.60
                          (30.0 * 0.15)    // Tier 2: 4.50
                          // Total: 8.10
        
        val calculatedBill = calculator.calculateBill(consumption)
        assertEquals(expectedBill, calculatedBill, 0.01)
    }

    @Test
    fun `test tier 3 consumption`() {
        // Test consumption in tier 3 (>60 kWh)
        val consumption = 75.0
        val expectedBill = (30.0 * 0.12) + // Tier 1: 3.60
                          (30.0 * 0.15) + // Tier 2: 4.50
                          (15.0 * 0.18)   // Tier 3: 2.70
                          // Total: 10.80
        
        val calculatedBill = calculator.calculateBill(consumption)
        assertEquals(expectedBill, calculatedBill, 0.01)
    }

    @Test
    fun `test zero consumption`() {
        val consumption = 0.0
        val expectedBill = 0.0
        
        val calculatedBill = calculator.calculateBill(consumption)
        assertEquals(expectedBill, calculatedBill, 0.01)
    }

    @Test
    fun `test custom rates`() {
        // Setup custom rates
        `when`(sharedPreferences.getFloat(eq("tier1_rate"), anyFloat())).thenReturn(0.10f)
        `when`(sharedPreferences.getFloat(eq("tier2_rate"), anyFloat())).thenReturn(0.20f)
        `when`(sharedPreferences.getFloat(eq("tier3_rate"), anyFloat())).thenReturn(0.30f)
        
        val consumption = 75.0
        val expectedBill = (30.0 * 0.10) + // Tier 1: 3.00
                          (30.0 * 0.20) + // Tier 2: 6.00
                          (15.0 * 0.30)   // Tier 3: 4.50
                          // Total: 13.50
        
        val calculatedBill = calculator.calculateBill(consumption)
        assertEquals(expectedBill, calculatedBill, 0.01)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test negative consumption`() {
        calculator.calculateBill(-10.0)
    }
} 