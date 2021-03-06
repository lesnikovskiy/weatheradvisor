package com.fewpeople.services;

import static com.fewpeople.constants.IntentConstants.COLD_STATE;
import static com.fewpeople.constants.IntentConstants.DEWPOINT;
import static com.fewpeople.constants.IntentConstants.HUMIDEX;
import static com.fewpeople.constants.IntentConstants.HUMIDITY;
import static com.fewpeople.constants.IntentConstants.PRESSURE;
import static com.fewpeople.constants.IntentConstants.PRESSUREDIFF;
import static com.fewpeople.constants.IntentConstants.SAME_STATE;
import static com.fewpeople.constants.IntentConstants.STOP_SERVICE;
import static com.fewpeople.constants.IntentConstants.TEMP;
import static com.fewpeople.constants.IntentConstants.TEMPDIFF;
import static com.fewpeople.constants.IntentConstants.TEMP_STATE;
import static com.fewpeople.constants.IntentConstants.TITLE;
import static com.fewpeople.constants.IntentConstants.WARMER_STATE;
import static com.fewpeople.constants.IntentConstants.WARNING;
import static com.fewpeople.constants.IntentConstants.WINDCHILL;
import static com.fewpeople.constants.IntentConstants.WINDDIFF;
import static com.fewpeople.constants.IntentConstants.WINDSPEED;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fewpeople.data.LocalDataAdapter;
import com.fewpeople.mitten.MittenActivity;
import com.fewpeople.mitten.contracts.WebApiContract;
import com.fewpeople.models.Condition;
import com.fewpeople.models.LocalData;
import com.fewpeople.models.TemperatureState;
import com.fewpeople.models.WeatherData;
import com.fewpeople.utils.BatteryUtil;
import com.fewpeople.utils.DateUtil;
import com.fewpeople.utils.WeatherUtil;
import com.fewpeople.webservice.WebApiService;

public class MittenService extends Service {
	static final public String BROADCAST_ACTION = "com.fewpeople.services.broadcastevent";
	
	static final private String TAG = "WeatherAdvisorService";	
	static final private int DEFAULT_DELAY = 1000*60*15; // 15 minutes
	static final private int NO_CONNECTION_DELAY = 1000*10; // 10 seconds
	static final private int WI_FI_DELAY = 1000*60*5; //  5 minutes
	static final private int MOBILE_NET_DELAY = 1000*60*60; // 1 hour
	
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
			
			handler.postDelayed(this, getDelay());
		}
	};	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		db.close();
		handler.removeCallbacks(updateUi);
		
		showNotification("Mitten", 
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
			
			HashMap<String, String> map = new HashMap<String, String>();
			
			if (isConnectedOrConnecting()) {
				WebApiContract<WeatherData> webApi = new WebApiService();
				WeatherData weatherData = webApi.get(lat, lon);
				
				if (weatherData != null) {
					Log.d(TAG, "Unable to retrieve weather conditions, the web service returned null");
					
					Condition condition = weatherData.getData().getCurrent_condition().get(0);
					
					LocalData data = new LocalData();
					data.setObservationTime(DateUtil.getCurrentDateString());
					data.setTempC(condition.getTemp_C());
					data.setVisibility(condition.getVisibility());
					data.setCloudcover(condition.getCloudcover());
					data.setHumidity(condition.getHumidity());
					data.setPressure(condition.getPressure());
					data.setWindspeedKmph(condition.getWindspeedKmph());
					
					boolean result = db.insertData(data);
					if (!result)
						Toast.makeText(getApplicationContext(), "Unable to save weather conditions to database.", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Weather service failed to respond.", Toast.LENGTH_LONG).show();
				}
			} else {
				map.put(WARNING, "Warning: No internet connection!");
			}
				
			LocalData currentData = db.getLastData();			
			if (currentData != null) {	
				map.put(TITLE, currentData.getObservationTime());
				map.put(TEMP, String.valueOf(currentData.getTempC()));
				map.put(HUMIDEX, String.valueOf(WeatherUtil.getHumidex(currentData.getTempC(), currentData.getHumidity())));
				map.put(DEWPOINT, String.valueOf(WeatherUtil.getDewPoint(currentData.getTempC(), currentData.getHumidity())));
				map.put(HUMIDITY, String.valueOf(currentData.getHumidity()));
				map.put(PRESSURE, String.valueOf(currentData.getPressure()));
				map.put(WINDSPEED, String.valueOf(currentData.getWindspeedKmph()));
				map.put(WINDCHILL, String.valueOf(WeatherUtil.getWindChill(currentData.getTempC(), currentData.getWindspeedKmph())));
				
				LocalData previousLocalData = db.getLastByHoursData(-24);
				if (previousLocalData != null) {
					TemperatureState tempDif = getTemperatureDiff(currentData, previousLocalData);
					String pressDif = getPressureDiff(currentData, previousLocalData);
					String windDiff = getWindspeedDiff(currentData, previousLocalData);
					
					map.put(TEMPDIFF, tempDif.getMessage());
					map.put(PRESSUREDIFF, pressDif);
					map.put(WINDDIFF, windDiff);
					map.put(TEMP_STATE, tempDif.getState());
				}
				
				String title = String.format("Weather: %s", currentData.getObservationTime());
				String temp = String.format("Temperature: %s\u2103", currentData.getTempC());
				String perceived_temp = String.format("Humidex: %s\u2103", WeatherUtil.getHumidex(currentData.getTempC(), currentData.getHumidity()));
				String dewpoint = String.format("Dew point: %s\u2103", WeatherUtil.getDewPoint(currentData.getTempC(), currentData.getHumidity()));
				String humidity = String.format("Humidity: %s%%", currentData.getHumidity());
				String pressure = String.format("Pressure: %s mm", currentData.getPressure());
				String windSpeed = String.format("Windspeed: %s Kmph", currentData.getWindspeedKmph());
				String windChill = String.format("Wind Chill Factor: %s\u2103", WeatherUtil.getWindChill(currentData.getTempC(), currentData.getWindspeedKmph()));
				
				intent.putExtra(STOP_SERVICE, shouldStopService());
				
				showNotification(title, new String[] {temp, perceived_temp, dewpoint, humidity, pressure, windSpeed, windChill}, map);				
				
				for (Entry<String, String> entry : map.entrySet()) {
					intent.putExtra(entry.getKey(), entry.getValue());
				}
				
				sendBroadcast(intent);
			}								
		} catch (NullPointerException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}	
	}
	
	private boolean isConnectedOrConnecting() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnectedOrConnecting()) {
			return true;
		}
		
		return false;
	}
	
	private int getDelay() {		
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = cm.getActiveNetworkInfo();
		if (network == null)
			return NO_CONNECTION_DELAY;
		
		boolean isConnected = network.isConnectedOrConnecting();
		if (!isConnected)
			return NO_CONNECTION_DELAY;
		
		switch (network.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				return WI_FI_DELAY;
			case ConnectivityManager.TYPE_MOBILE:
				return MOBILE_NET_DELAY;
			default:
				return DEFAULT_DELAY;
		}
	}
	
	private boolean shouldStopService() {
		float batteryLevel = BatteryUtil.getBatterLevel(getApplicationContext());
		
		if (batteryLevel < 0.14)
			return true;
		else
			return false;
	}
	
	private TemperatureState getTemperatureDiff(LocalData current, LocalData previous) {
		TemperatureState state = new TemperatureState();
		state.setMessage("No information about temperature difference available yet!");
		
		if (previous != null && current != null) {
			int now = WeatherUtil.getHumidex(current.getTempC(), current.getHumidity());
			int yesterday = WeatherUtil.getHumidex(previous.getTempC(), previous.getHumidity()) ;				
			
			if (now > yesterday) {
				int diff = now - yesterday;
				state.setMessage(String.format("It got warmer: %s %s difference )))", diff, diff != 1 ? "degrees" : "degree"));
				state.setState(WARMER_STATE);
			} else if (now == yesterday) {
				state.setMessage("The temperature is the same.");
				state.setState(SAME_STATE);
			} else {
				int diff = yesterday - now;
				state.setMessage(String.format("It got more cold: %s %s difference", diff, diff != 1 ? "degrees" : "degree"));
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
		Intent intent = new Intent(getApplicationContext(), MittenActivity.class);
		if (!extras.isEmpty()) {
			for (Entry<String, String> entry : extras.entrySet()) {
				intent.putExtra(entry.getKey(), entry.getValue());
			}
		}
		
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(com.fewpeople.mitten.R.drawable.ic_launcher)
			.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), com.fewpeople.mitten.R.drawable.ic_launcher))
			.setContentTitle(title)
			.setContentIntent(pendingIntent)
			.setOnlyAlertOnce(true)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		
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
