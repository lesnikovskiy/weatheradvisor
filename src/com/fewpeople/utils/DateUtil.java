package com.fewpeople.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtil {
	static final private String FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	public static String getCurrentDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT, Locale.US);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		
		return sdf.format(c.getTime());
	}
	
	public static String getDateString(int hours) {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT, Locale.US);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.add(Calendar.HOUR, hours);
		
		return sdf.format(c.getTime());
	}
	
	public static String getDateStringByMinutes(int minutes) {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT, Locale.US);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.add(Calendar.MINUTE, minutes);
		
		return sdf.format(c.getTime());
	}
}
