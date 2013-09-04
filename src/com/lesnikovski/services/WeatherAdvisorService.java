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

import com.lesnikovski.data.LocalDataAdapter;
import com.lesnikovski.models.Condition;
import com.lesnikovski.models.LocalData;
import com.lesnikovski.models.WeatherData;
import com.lesnikovski.utils.Utils;
import com.lesnikovski.weatheradvisor.contracts.WebApiContract;
import com.lesnikovski.webservice.WebApiService;

public class WeatherAdvisorService extends Service {
	static final public String BROADCAST_ACTION = "com.lesnikovski.services.broadcastevent";
	
	static final private String TAG = "WeatherAdvisorService";	
	private static final int delay = 1000*60*5;
	
	private final Handler handler = new Handler();
	private Intent intent;	
	private LocalDataAdapter db;
	
	@Override
	public void onCreate() {
		super.onCreate();
				
		db = new LocalDataAdapter(getApplicationContext());
		db.open();
		intent = new Intent(BROADCAST_ACTION);
		handler.removeCallbacks(updateUi);
				
		showNotification("Weather Advisor", new String[] { "Service that will update weather conditions started!."});
		Log.d(TAG, "Service onCreate()");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Service onStartCommand");		
		handler.postDelayed(updateUi, 10);		
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private Runnable updateUi = new Runnable() {
		public void run() {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					updateUiRunnable();		
				}				
			});
			thread.start();
			
			handler.postDelayed(this, delay);
		}
	};	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		db.close();
		handler.removeCallbacks(updateUi);
		
		showNotification("Weather Advisor", new String[] { "Service has been stopped.", "Start the service to get latest weather conditions." });
		Log.d(TAG, "Service onDestroy");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private synchronized void updateUiRunnable() {		
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
			
			Log.d(TAG, String.format("Location: %s %s", lat, lon));
			
			WebApiContract<WeatherData> webApi = new WebApiService();
			WeatherData weatherData = webApi.get(lat, lon);
			
			if (weatherData == null) {
				showNotification("Error", new String[] {"Error in the application", "Hint: Check if you're connected to the Internet"});
				return;
			}
			
			Condition condition = weatherData.getData().getCurrent_condition().get(0);		
									
			String title = String.format("Weather: %s", condition.getObservation_time().toString());
			String temp = String.format("Temperature: %s\u2103", condition.getTemp_C());
			String humidity = String.format("Humidity: %s%%", condition.getHumidity());
			String pressure = String.format("Pressure: %s mm", condition.getPressure());
			String windSpeed = String.format("Windspeed: %s Kmph", condition.getWindspeedKmph());
			
			showNotification(title, new String[] {temp, humidity, pressure, windSpeed});
			
			LocalData data = new LocalData();
			data.setObservationTime(Utils.getCurrentDateString());
			data.setTempC(condition.getTemp_C());
			data.setVisibility(condition.getVisibility());
			data.setCloudcover(condition.getCloudcover());
			data.setHumidity(condition.getHumidity());
			data.setPressure(condition.getPressure());
			data.setWindspeedKmph(condition.getWindspeedKmph());				
			
			boolean result = db.insertData(data);			
			if (result) {
				LocalData d = db.getLastData();
				
				if (d != null) {				
					intent.putExtra("title", d.getObservationTime());
					intent.putExtra("temp", d.getTempC());
					intent.putExtra("humidity", d.getHumidity());
					intent.putExtra("pressure", d.getPressure());
					intent.putExtra("windSpeed", d.getWindspeedKmph());
				}
				
				String difference = "No weather difference! Keep using software to get it soon!";
				
				LocalData lastHourD = db.getLastByHoursData(-24);
				if (lastHourD != null && d != null) {
					int now = d.getTempC();
					int yesterday = lastHourD.getTempC();				
					
					if (now > yesterday) {
						difference = String.format("It is warmer: %s degree(s) difference )))", now - yesterday);
					} else if (now == yesterday) {
						difference = "The weather is the same. Nothing's changed! Thanks for using the app!";
					} else {
						difference = String.format("It is more cold: %s degree(s) difference", yesterday - now);
					}
					//int humidityDiff = d.getHumidity() - lastHourD.getHumidity();
					//int pressDiff = d.getPressure() - lastHourD.getPressure();
					//int windSDiff = d.getWindspeedKmph() - lastHourD.getWindspeedKmph();
				}
				
				intent.putExtra("diff", difference);
			}			
			
			sendBroadcast(intent);					
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}	
	}

	private void showNotification(String title, String[] messages) {			
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.title_bar)
			.setContentTitle(title)			;
		
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
