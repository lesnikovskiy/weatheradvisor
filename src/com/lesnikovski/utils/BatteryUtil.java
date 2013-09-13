package com.lesnikovski.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryUtil {
	public static boolean isCharging(Context context) {			
		return getBatteryStatus(context) == BatteryManager.BATTERY_STATUS_CHARGING;
	}
	
	public static boolean isFull(Context context) {
		return getBatteryStatus(context) == BatteryManager.BATTERY_STATUS_FULL;
	}
	
	private static int getBatteryStatus(Context context) {
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, intentFilter);
		
		return batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
	}
}
