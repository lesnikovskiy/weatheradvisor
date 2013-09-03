package com.lesnikovski.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
import android.widget.Toast;

public final class HttpUtil {
	static final private String TAG = "HttpUtil";
	
	public static String get(String url) {
		HttpClient httpclient = new DefaultHttpClient();		
		HttpGet httpget = new HttpGet(url);		
		
		HttpResponse httpResponse = null;		
		
		try {
			httpResponse = httpclient.execute(httpget);
			
			String line = "";
			StringBuilder sb = new StringBuilder();
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(httpResponse.getEntity().getContent()));
			
			while((line = reader.readLine()) != null) {
				sb.append(line);
			}
			
			reader.close();
			
			return sb.toString().trim().replace("\n", "").replace("\r", "");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;		
	}
}
