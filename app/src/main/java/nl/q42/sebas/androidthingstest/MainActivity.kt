package nl.q42.sebas.androidthingstest

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.contrib.driver.bmx280.Bmx280
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import java.io.IOException


class MainActivity : Activity() {
    private var display: AlphanumericDisplay? = null
    private var sensor: Bmx280? = null
    private var redLed: Gpio? = null

    private var ledOn = false
    private val handler by lazy(LazyThreadSafetyMode.NONE) { Handler(mainLooper) }
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting TemperatureActivity")
        try {
            initializeLeds()
            initializeDisplay()
            initializeSensor()
            runAndReschedule()
        } catch (e: IOException) {
            Log.e(TAG, "Error configuring sensor or display", e)
        }
    }

    private fun initializeLeds() {
        redLed = RainbowHat.openLedRed()
    }

    private fun initializeSensor() {
        sensor = RainbowHat.openSensor()
        sensor?.setMode(Bmx280.MODE_NORMAL)
        sensor?.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X)
    }

    private fun initializeDisplay() {
        display = RainbowHat.openDisplay()
        display?.setEnabled(true)
    }

    private fun runAndReschedule() {
        try {
            display?.display(sensor?.readTemperature()?.toString() ?: "0")
            redLed?.value = ledOn
            ledOn = !ledOn
        } catch (e: IOException) {
            Log.e(TAG, "Error displaying temperature", e)
        }

        handler.postDelayed({ runAndReschedule() }, 200)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Closing sensor")
        display?.close()
        sensor?.close()
    }
}
