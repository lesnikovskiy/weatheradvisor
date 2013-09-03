package com.lesnikovski.services;

import android.R;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.lesnikovski.models.Condition;
import com.lesnikovski.models.WeatherData;
import com.lesnikovski.weatheradvisor.contracts.WebApiContract;
import com.lesnikovski.webservice.WebApiService;

public class WeatherAdvisorService extends Service {
	static final public String BROADCAST_ACTION = "com.lesnikovski.services.broadcastevent";
	
	static final private String TAG = "WeatherAdvisorService";	
	private static final int delay = 1000*60;
	
	private final Handler handler = new Handler();
	private Intent intent;	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		intent = new Intent(BROADCAST_ACTION);
		
		showNotification("Weather Advisor", new String[] { "service successfully started."});
		Log.d(TAG, "Service onCreate()");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Service onStartCommand");
		
		handler.removeCallbacks(updateUi);
		handler.postDelayed(updateUi, delay);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private Runnable updateUi = new Runnable() {
		public void run() {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					UpdateUiRunnable();					
				}				
			});
			thread.start();
			
			handler.postDelayed(this, delay);
		}
	};
	
	private void UpdateUiRunnable() {		
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String bestProvider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		double lat, lon;
		
		try {						
			if (location == null) {
				lat = 50;
				lon = 30;
			} else {
				lat = location.getLatitude();
				lon = location.getLongitude();
			}
			
			WebApiContract<WeatherData> webApi = new WebApiService();
			WeatherData weatherData = webApi.get(lat, lon);
			
			if (weatherData == null) {
				showNotification("Error", new String[] {"Error in the application", "Hint: Check if you're connected to the Internet"});
				return;
			}
			
			Condition condition = weatherData.getData().getCurrent_condition().get(0);		
									
			String title = String.format("Weather: %s", condition.getObservation_time().toString());
			String temp = String.format("Temperature: %s C", condition.getTemp_C());
			String humidity = String.format("Humidity: %s", condition.getHumidity());
			String pressure = String.format("Pressure: %s mm", condition.getPressure());
			String windSpeed = String.format("Windspeed: %s Kmph", condition.getWindspeedKmph());
			
			showNotification(title, new String[] {temp, humidity, pressure, windSpeed});
			
			intent.putExtra("title", title);
			intent.putExtra("temp", temp);
			intent.putExtra("humidity", humidity);
			intent.putExtra("pressure", pressure);
			intent.putExtra("windSpeed", windSpeed);
			
			sendBroadcast(intent);					
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}	
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		showNotification("Weather Advisor", new String[] { "service has been stopped."});
		Log.d(TAG, "Service onDestroy");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void showNotification(String title, String[] messages) {			
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.title_bar)
			.setContentTitle(title);
		
		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		
		inboxStyle.setBigContentTitle(title);
		
		for (int i = 0; i < messages.length; i++) {
			inboxStyle.addLine(messages[i]);
		}
		
		mBuilder.setStyle(inboxStyle);
		
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int notifyId = 1;
		manager.notify(notifyId, mBuilder.build());
	}
}
