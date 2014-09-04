package com.stealz.locationreminder;

/**
@author - Vedang Jadhav
*/
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

public class CommonMethods
{
	public JSONObject getLocationInfo(double lat, double lon)
	{
		HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng="+lat+","+lon+"&sensor=true");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try 
		{
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) 
			{
				stringBuilder.append((char) b);
			}
		}
		catch (ClientProtocolException e) 
		{}
		catch (IOException e)                                                                                      
		{}

		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject = new JSONObject(stringBuilder.toString());
		}
		catch (JSONException e) 
		{e.printStackTrace();}
		return jsonObject; 
	}

	public double getDistances(String sourceAddress, String destinationAddress)
	{
		//sourceAddress = sourceAddress.replace(" ","");
		//destinationAddress = destinationAddress.replace(" ","");

		StringBuilder stringBuilder = null;
		Double longDistance = null;

		try 
		{
			String strGet = "http://maps.googleapis.com/maps/api/directions/json?origin="
		+URLEncoder.encode(sourceAddress,"UTF-8")+"&destination="+URLEncoder.encode(destinationAddress,"UTF-8")+"&sensor=false";
			//strGet = URLEncoder.encode(strGet,"UTF-8");
			HttpGet httpGet = new HttpGet(strGet);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response;
			stringBuilder = new StringBuilder();

			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) 
			{
				stringBuilder.append((char) b);
			}
		}
		catch (ClientProtocolException e) 
		{}
		catch (IOException e)                                                                                      
		{}

		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject = new JSONObject(stringBuilder.toString());
			jsonObject = jsonObject.getJSONArray("routes").getJSONObject(0);
			jsonObject = jsonObject.getJSONArray("legs").getJSONObject(0);
			jsonObject = jsonObject.getJSONObject("distance");
			String strDistance = jsonObject.getString("value");
			longDistance = new Double(strDistance);
			longDistance = longDistance/1000;
		}
		catch (JSONException e) 
		{e.printStackTrace();}
		return longDistance;
	}

	public boolean share(Context context,String message)
	{
		try
		{
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");

			String[] to = {};
			sharingIntent.putExtra(Intent.EXTRA_EMAIL, to);
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT,
					"Locate Me - Location");
			sharingIntent
			.putExtra(
					Intent.EXTRA_TEXT,
					message);
			context.startActivity(Intent.createChooser(sharingIntent, "Sharing options.."));
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
}
