package com.lesnikovski.services;

import static com.lesnikovski.constants.IntentConstants.COLD_STATE;
import static com.lesnikovski.constants.IntentConstants.HUMIDITY;
import static com.lesnikovski.constants.IntentConstants.PRESSURE;
import static com.lesnikovski.constants.IntentConstants.PRESSUREDIFF;
import static com.lesnikovski.constants.IntentConstants.SAME_STATE;
import static com.lesnikovski.constants.IntentConstants.TEMP;
import static com.lesnikovski.constants.IntentConstants.TEMPDIFF;
import static com.lesnikovski.constants.IntentConstants.TEMP_STATE;
import static com.lesnikovski.constants.IntentConstants.TITLE;
import static com.lesnikovski.constants.IntentConstants.WARMER_STATE;
import static com.lesnikovski.constants.IntentConstants.WINDDIFF;
import static com.lesnikovski.constants.IntentConstants.WINDSPEED;

import java.util.HashMap;
import java.util.Map.Entry;

import android.R;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.lesnikovski.models.TemperatureState;
import com.lesnikovski.models.WeatherData;
import com.lesnikovski.utils.Utils;
import com.lesnikovski.weatheradvisor.WeatherAdvisorActivity;
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
				
		showNotification("Weather Advisor", 
				new String[] { "Service that will update weather conditions started!."}, 
				new HashMap<String, String>());
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
		
		showNotification("Weather Advisor", 
				new String[] { "Service has been stopped.", "Start the service to get latest weather conditions." }, 
				new HashMap<String, String>());
		Log.d(TAG, "Service onDestroy");
	}

	@Override
	public IBinder onBind(Intent arg0) {
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
				showNotification("Error", new String[] {"Error in the application", "Hint: Check if you're connected to the Internet"},
						new HashMap<String, String>());
				
				return;
			}
			
			Condition condition = weatherData.getData().getCurrent_condition().get(0);		
									
			String title = String.format("Weather: %s", condition.getObservation_time().toString());
			String temp = String.format("Temperature: %s\u2103", condition.getTemp_C());
			String humidity = String.format("Humidity: %s%%", condition.getHumidity());
			String pressure = String.format("Pressure: %s mm", condition.getPressure());
			String windSpeed = String.format("Windspeed: %s Kmph", condition.getWindspeedKmph());
			
			HashMap<String, String> map = new HashMap<String, String>();
			
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
					map.put(TITLE, d.getObservationTime());
					map.put(TEMP, String.valueOf(d.getTempC()));
					map.put(HUMIDITY, String.valueOf(d.getHumidity()));
					map.put(PRESSURE, String.valueOf(d.getPressure()));
					map.put(WINDSPEED, String.valueOf(d.getWindspeedKmph()));
					
					LocalData previousLocalData = db.getLastByHoursData(-24);
					if (previousLocalData != null) {
						TemperatureState tempDif = getTemperatureDiff(d, previousLocalData);
						String pressDif = getPressureDiff(d, previousLocalData);
						String windDiff = getWindspeedDiff(d, previousLocalData);
						
						map.put(TEMPDIFF, tempDif.getMessage());
						map.put(PRESSUREDIFF, pressDif);
						map.put(WINDDIFF, windDiff);
						map.put(TEMP_STATE, tempDif.getState());
					}
				}				
			}	
			
			showNotification(title, new String[] {temp, humidity, pressure, windSpeed}, map);
			
			for (Entry<String, String> entry : map.entrySet()) {
				intent.putExtra(entry.getKey(), entry.getValue());
			}
			
			sendBroadcast(intent);					
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}	
	}
	
	private TemperatureState getTemperatureDiff(LocalData current, LocalData previous) {
		TemperatureState state = new TemperatureState();
		state.setMessage("No information about temperature difference available yet!");
		
		if (previous != null && current != null) {
			int now = current.getTempC();
			int yesterday = previous.getTempC();				
			
			if (now > yesterday) {
				state.setMessage(String.format("It got warmer: %s degree(s) difference )))", now - yesterday));
				state.setState(WARMER_STATE);
			} else if (now == yesterday) {
				state.setMessage("The temperature is the same.");
				state.setState(SAME_STATE);
			} else {
				state.setMessage(String.format("It got more cold: %s degree(s) difference", yesterday - now));
				state.setState(COLD_STATE);
			}
		}
		
		return state;
	}
	
	private String getPressureDiff(LocalData current, LocalData previous) {
		String diff = "No information about pressure difference available yet!";
		
		if (previous != null && current != null) { 
			int now = current.getPressure();
			int prev = previous.getPressure();
			
			if (now > prev) {
				diff = String.format("The pressure went down %s mm", now - prev);
			} else if (now == prev) {
				diff = "The pressure hasn't changed yet.";
			} else {
				diff = String.format("The pressure went up %s mm", prev - now);
			}
		}
		
		return diff;
	}
	
	private String getWindspeedDiff(LocalData current, LocalData previous) {
		String diff = "No information about wind speed difference available yet!";
		
		if (previous != null && current != null) { 
			int now = current.getWindspeedKmph();
			int prev = previous.getWindspeedKmph();
			
			if (now > prev) {
				diff = String.format("The wind speed increased %s KMPH", now - prev);
			} else if (now == prev) {
				diff = "The wind speed hasn't changed yet.";
			} else {
				diff = String.format("The wind speed decreased %s KMPH", prev - now);
			}
		}
		
		return diff;
	}

	private void showNotification(String title, String[] messages, HashMap<String, String> extras) {	
		Intent intent = new Intent(getApplicationContext(), WeatherAdvisorActivity.class);
		if (!extras.isEmpty()) {
			for (Entry<String, String> entry : extras.entrySet()) {
				intent.putExtra(entry.getKey(), entry.getValue());
			}
		}
		
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.title_bar)
			.setContentTitle(title)
			.setContentIntent(pendingIntent);
		
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
