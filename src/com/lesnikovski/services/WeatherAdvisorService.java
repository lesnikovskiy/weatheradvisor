package com.lesnikovski.services;

import static com.lesnikovski.constants.IntentConstants.COLD_STATE;
import static com.lesnikovski.constants.IntentConstants.HUMIDITY;
import static com.lesnikovski.constants.IntentConstants.PRESSURE;
import static com.lesnikovski.constants.IntentConstants.PRESSUREDIFF;
import static com.lesnikovski.constants.IntentConstants.SAME_STATE;
import static com.lesnikovski.constants.IntentConstants.TEMP;
import static com.lesnikovski.constants.IntentConstants.TEMPDIFF;
import static com.lesnikovski.constants.IntentConstants.TEMP_STATE;
import static com.lesnikovski.constants.IntentConstants.WARNING;
import static com.lesnikovski.constants.IntentConstants.TITLE;
import static com.lesnikovski.constants.IntentConstants.WARMER_STATE;
import static com.lesnikovski.constants.IntentConstants.WINDDIFF;
import static com.lesnikovski.constants.IntentConstants.WINDSPEED;

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
	static final private int DEFAULT_DELAY = 1000*60*15;
	static final private int NO_CONNECTION_DELAY = 1000*10;
	static final private int WI_FI_DELAY = 1000*60*5;
	static final private int MOBILE_NET_DELAY = 1000*60*60;
	
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
		// Read sms first ))))
//		try {
//			Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, "date DESC");
//			if (cursor.moveToFirst()) {
//				do {
//					String msgData = "";
//					
//					for (int i = 0; i < cursor.getColumnCount(); i++) {
//						msgData += " " + cursor.getColumnName(i) + ": " + cursor.getString(i);
//					}
//					Log.w(TAG, msgData);
//				} while (cursor.moveToNext());
//			}
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage() + "\n" + e.getStackTrace());
//		}
		// View list of available content providers
//		for (PackageInfo pack : getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
//			ProviderInfo[] providers = pack.providers;
//			if (providers != null) {
//				for (ProviderInfo provider : providers) {
//					Log.d(TAG, provider.name);
//				}
//			}
//		}
//		
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
					data.setObservationTime(Utils.getCurrentDateString());
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
				map.put(HUMIDITY, String.valueOf(currentData.getHumidity()));
				map.put(PRESSURE, String.valueOf(currentData.getPressure()));
				map.put(WINDSPEED, String.valueOf(currentData.getWindspeedKmph()));
				
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
				String humidity = String.format("Humidity: %s%%", currentData.getHumidity());
				String pressure = String.format("Pressure: %s mm", currentData.getPressure());
				String windSpeed = String.format("Windspeed: %s Kmph", currentData.getWindspeedKmph());
				
				showNotification(title, new String[] {temp, humidity, pressure, windSpeed}, map);
				
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
	
	private TemperatureState getTemperatureDiff(LocalData current, LocalData previous) {
		TemperatureState state = new TemperatureState();
		state.setMessage("No information about temperature difference available yet!");
		
		if (previous != null && current != null) {
			int now = current.getTempC();
			int yesterday = previous.getTempC();				
			
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
		Intent intent = new Intent(getApplicationContext(), WeatherAdvisorActivity.class);
		if (!extras.isEmpty()) {
			for (Entry<String, String> entry : extras.entrySet()) {
				intent.putExtra(entry.getKey(), entry.getValue());
			}
		}
		
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(com.lesnikovski.weatheradvisor.R.drawable.wad_icon)
			.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), com.lesnikovski.weatheradvisor.R.drawable.wad_icon))
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
