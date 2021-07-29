package com.rn.devicesinfo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

internal object BatteryTools {
    @JvmStatic
    fun getSystemBatteryLevel(context: Context?): Int {
        val batteryInfoIntent = context!!.applicationContext
            .registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
        val level = batteryInfoIntent!!.getIntExtra("level", 0)
        val batterySum = batteryInfoIntent.getIntExtra("scale", 100)
        return 100 * level / batterySum
    }
    @JvmStatic
    fun getSystemBatterySum(context: Context?): Int {
        val batteryInfoIntent = context!!.applicationContext
            .registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
        return batteryInfoIntent!!.getIntExtra("scale", 100)
    }
    @JvmStatic
    fun getSystemBattery(context: Context?): Int {
        val batteryInfoIntent = context!!.applicationContext
            .registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
        val level = batteryInfoIntent!!.getIntExtra("level", 0)
        val batterySum = batteryInfoIntent.getIntExtra("scale", 100)
        return 100 * level / batterySum
    }
    @JvmStatic
    fun getBatterytemp(context: Context?): Int {
        val batteryInfoIntent = context!!.applicationContext
            .registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
        return batteryInfoIntent!!.getIntExtra("temperature", -1)
    }
    @JvmStatic
    fun getBatteryStatus(context: Context?): Int {
        val batteryInfoIntent = context!!.applicationContext
            .registerReceiver(
                null as BroadcastReceiver?,
                IntentFilter("android.intent.action.BATTERY_CHANGED")
            )
        return batteryInfoIntent!!.getIntExtra("status", -1)
    }
    @JvmStatic
    fun getBatteryCapacity(context: Context?): String? {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context::class.java)
                    .newInstance(context)
            batteryCapacity = Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity.toString()
    }

}