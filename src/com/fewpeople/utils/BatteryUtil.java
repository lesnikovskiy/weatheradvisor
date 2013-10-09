package com.fewpeople.utils;

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
	
	public static float getBatterLevel(Context context) {
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		
		Intent batteryLevel = context.registerReceiver(null, intentFilter);
		
		int level = batteryLevel.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryLevel.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		
		return level / (float) scale;
	}
	
	private static int getBatteryStatus(Context context) {
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, intentFilter);
		
		return batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
	}
}
