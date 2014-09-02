package com.stealz.locationreminder;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stealz.db.DbUtil;

public class NotificationScreen extends FragmentActivity
{
	private String reminderName,address;
	private TextView textNotifReminderName,textNotifAddress;
	private Button btnDismiss;
	private static LatLng loc = null;
	private double latitude,longitude;
	private Marker marker;
	private String strAddress,currentAddress;
	private GoogleMap map;
	private int id;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notification_activity);

		textNotifReminderName = (TextView)findViewById(R.id.textNotifRemindName);
		textNotifAddress = (TextView)findViewById(R.id.textNotificationAddress);
		btnDismiss = (Button)findViewById(R.id.btnNotificationDismiss);

		Bundle extras = getIntent().getExtras(); 
		if(extras != null)
		{
			id = getIntent().getExtras().getInt("id");
			DbUtil dbUtil = new DbUtil(this);

			try
			{
				dbUtil.open();
				Cursor c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status"},
						"_id = ?",new String[]{new Integer(id).toString()});

				if(c != null)
				{
					while(c.moveToNext())
					{
						reminderName = c.getString(1);
						address = c.getString(0);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Toast.makeText(NotificationScreen.this, "Cannot Display Address", Toast.LENGTH_SHORT).show();
			}
			finally
			{
				dbUtil.close();
			}
		}

		textNotifReminderName.setText(reminderName);
		textNotifAddress.setText(address);

		btnDismiss.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				DbUtil dbUtil = new DbUtil(NotificationScreen.this);
				try
				{
					dbUtil.open();
					dbUtil.updateRow(id, "dismissed");
					Toast.makeText(NotificationScreen.this, "Dismissed", Toast.LENGTH_SHORT).show();
					finish();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					dbUtil.close();
				}
			}
		});
		
		map = ((SupportMapFragment)this.getSupportFragmentManager().findFragmentById(R.id.mapNotifAddress))
				.getMap();

		Thread th = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				try
				{
					strAddress = textNotifAddress.getText().toString();
					JSONObject jsonObject = getLocationInfo(strAddress);
					jsonObject = jsonObject.getJSONArray("results").getJSONObject(0);
					jsonObject = jsonObject.getJSONObject("geometry");//.getJSONObject(0);
					jsonObject = jsonObject.getJSONObject("location");
					String lat = jsonObject.getString("lat");
					String lon = jsonObject.getString("lng");
					
					latitude = new Double(lat);
					longitude = new Double(lon);
					
					handler.sendEmptyMessage(0);
				}
				catch(Exception e)
				{
					handler.sendEmptyMessage(-1);
				}
				
			}
		});
		
		th.start();
		/*loc = new LatLng(latitude,longitude);

		if(marker != null)
		{
			marker.remove();
		}

		marker = map.addMarker(new MarkerOptions()
		.position(loc)
		.title("Current Location")
		.snippet(strAddress));
		textStreetAddress1.setText(strAddress);*/
	}
	
	public JSONObject getLocationInfo(String address)
	{
		address = address.replace(" ", "+");
		HttpGet httpGet = new HttpGet("http://maps.googleapis.com/maps/api/geocode/json?address="+address+"&sensor=true");
		//http://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=true_or_false
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
	
	//this is the handler that 
		private Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) 
			{
				if(msg.what == 0)
				{
					loc = new LatLng(latitude,longitude);

					if(marker!=null)
						marker.remove();

					marker = map.addMarker(new MarkerOptions()
					.position(loc)
					.title("Current Location")
					.snippet(strAddress));

					Toast.makeText(NotificationScreen.this, "You are Connected "+ strAddress, Toast.LENGTH_LONG).show();
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
					map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
				}
				else
				{
					Toast.makeText(NotificationScreen.this, "You are not Connected "+ strAddress, Toast.LENGTH_LONG).show();
				}
			}
		};
		
		/*{
			   "results" : [
			      {
			         "address_components" : [
			            {
			               "long_name" : "1600",
			               "short_name" : "1600",
			               "types" : [ "street_number" ]
			            },
			            {
			               "long_name" : "Amphitheatre Pkwy",
			               "short_name" : "Amphitheatre Pkwy",
			               "types" : [ "route" ]
			            },
			            {
			               "long_name" : "Mountain View",
			               "short_name" : "Mountain View",
			               "types" : [ "locality", "political" ]
			            },
			            {
			               "long_name" : "Santa Clara",
			               "short_name" : "Santa Clara",
			               "types" : [ "administrative_area_level_2", "political" ]
			            },
			            {
			               "long_name" : "California",
			               "short_name" : "CA",
			               "types" : [ "administrative_area_level_1", "political" ]
			            },
			            {
			               "long_name" : "United States",
			               "short_name" : "US",
			               "types" : [ "country", "political" ]
			            },
			            {
			               "long_name" : "94043",
			               "short_name" : "94043",
			               "types" : [ "postal_code" ]
			            }
			         ],
			         "formatted_address" : "1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA",
			         "geometry" : {
			            "location" : {
			               "lat" : 37.42291810,
			               "lng" : -122.08542120
			            },
			            "location_type" : "ROOFTOP",
			            "viewport" : {
			               "northeast" : {
			                  "lat" : 37.42426708029149,
			                  "lng" : -122.0840722197085
			               },
			               "southwest" : {
			                  "lat" : 37.42156911970850,
			                  "lng" : -122.0867701802915
			               }
			            }
			         },
			         "types" : [ "street_address" ]
			      }
			   ],
			   "status" : "OK"
			}*/
}