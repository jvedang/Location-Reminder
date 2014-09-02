package com.stealz.locationreminder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stealz.db.DbUtil;

public class SetLocationReminder extends FragmentActivity implements
android.view.View.OnClickListener,
ConnectionCallbacks,
OnConnectionFailedListener,
LocationListener,OnCheckedChangeListener
{
	private TextView textStreetAddress1;
	private EditText editReminderName;
	private Button btnSave;
	private GoogleMap map;
	private LocationClient locationClient;
	private String strAddress,currentAddress;	
	private static LatLng loc = null;
	private double latitude,longitude;
	private Marker marker;
	private boolean firstTime = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_reminder);

		final int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (result != ConnectionResult.SUCCESS)
		{
			Toast.makeText(this, "Google Play service is not available (status=" + result + ")", Toast.LENGTH_LONG).show();
			finish();
		}

		textStreetAddress1 = (TextView)findViewById(R.id.textStreetAddress1);
		editReminderName = (EditText)findViewById(R.id.editReminderName);

		btnSave = (Button)findViewById(R.id.btnSave);

		map = ((SupportMapFragment)SetLocationReminder.this.getSupportFragmentManager().findFragmentById(R.id.mapSelectAddress))
				.getMap();

		map.setOnMapClickListener(new OnMapClickListener() 
		{
			@Override
			public void onMapClick(final LatLng latlng)
			{
				final CommonMethods cm = new CommonMethods();

				Thread t = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							JSONObject JSONlocation = cm.getLocationInfo(latlng.latitude,latlng.longitude);
							latitude = latlng.latitude;
							longitude = latlng.longitude;
							Log.d("SetLocationReminder",JSONlocation.toString());
							JSONlocation = JSONlocation.getJSONArray("results").getJSONObject(0);

							strAddress = JSONlocation.getString("formatted_address");
							handlerSetLoc.sendEmptyMessage(0);
						}
						catch(Exception e)
						{
							e.printStackTrace();
							handlerSetLoc.sendEmptyMessage(1);
						}
					}
				});
				t.start();
			}
		});

		btnSave.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View view) 
			{
				if(!editReminderName.getText().toString().equals(""))
				{
					final DbUtil dbUtil = new DbUtil(SetLocationReminder.this);

					final CommonMethods cm = new CommonMethods();
					Thread threadGetDistance = new Thread(new Runnable()
					{
						@Override
						public void run() 
						{
							try
							{
								dbUtil.open();
								double distance = cm.getDistances(currentAddress, strAddress);
								if(distance > 0)
								{
									String address,reminderName,dateCreated,currentStatus;

									address = textStreetAddress1.getText().toString();
									reminderName = editReminderName.getText().toString();
									DateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy hh:mm aa");
									dateCreated = dateFormat.format(new Date());
									currentStatus = "active";

									dbUtil.addRow(address, reminderName, dateCreated, currentStatus);

									handlerSaveLocation.sendEmptyMessage(0);
								}
								else
								{
									handlerSaveLocation.sendEmptyMessage(0);
								}
							}
							catch(Exception e)
							{
								e.printStackTrace();
								handlerSaveLocation.sendEmptyMessage(0);
							}
							finally
							{
								dbUtil.close();
							}
						}
					});
					threadGetDistance.start();
				}
				else
				{
					Toast.makeText(SetLocationReminder.this, "Give your Reminder a Name!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		locationClient = new LocationClient(this, this, this);
	}

	@Override
	protected void onResume()
	{
		super.onResume(); 
		locationClient.connect();
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		locationClient.disconnect();
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
	{

	}

	@Override
	public void onLocationChanged(Location location) 
	{
		try
		{
			Location loc = locationClient.getLastLocation();
			Log.d("onLocationChanged", "location=" + loc.toString());

			getLocationFromThread(loc.getLatitude(), loc.getLongitude());

		}
		catch(Exception e)
		{
			Toast.makeText(SetLocationReminder.this, "Unable to locate, please try again..", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{

	}

	@Override
	public void onConnected(Bundle arg0) 
	{
		try
		{

			Location loc = locationClient.getLastLocation();
			Log.d("onLocationChanged", "location=" + loc.toString());
			getLocationFromThread(loc.getLatitude(), loc.getLongitude());

		}
		catch(Exception e)
		{
			Toast.makeText(SetLocationReminder.this, "Unable to locate, please try again..", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v)
	{
	}

	private void getLocationFromThread(final double lat,final double lon)
	{
		Thread th = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				CommonMethods cm = new CommonMethods();

				try
				{
					JSONObject JSONlocation = cm.getLocationInfo(lat, lon);
					Log.d("SetLocationReminder",JSONlocation.toString());
					JSONlocation = JSONlocation.getJSONArray("results").getJSONObject(0);
					strAddress = JSONlocation.getString("formatted_address");

					if(firstTime)
					{
						currentAddress = strAddress;
						firstTime = false;
					}

					latitude = lat;
					longitude = lon;

					handler.sendEmptyMessage(0);
				}
				catch(Exception e)
				{
					strAddress = "Not Available";
					handler.sendEmptyMessage(1);
				}
			}
		});
		th.start();
	}

	//this is the handler that 
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			loc = new LatLng(latitude,longitude);

			if(marker!=null)
				marker.remove();

			marker = map.addMarker(new MarkerOptions()
			.position(loc)
			.title("Current Location")
			.snippet(strAddress));

			Toast.makeText(SetLocationReminder.this, "You are Connected "+ strAddress, Toast.LENGTH_LONG).show();
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
			map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
			textStreetAddress1.setText(strAddress);
		}
	};

	private Handler handlerSetLoc = new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			if(msg.what == 0)
			{
				Toast.makeText(SetLocationReminder.this,"Selected Address is "+strAddress, Toast.LENGTH_LONG).show();

				loc = new LatLng(latitude,longitude);

				if(marker != null)
				{
					marker.remove();
				}

				marker = map.addMarker(new MarkerOptions()
				.position(loc)
				.title("Current Location")
				.snippet(strAddress));
				textStreetAddress1.setText(strAddress);
			}
			else
			{
				Toast.makeText(SetLocationReminder.this,"Selected Address is Not Available", Toast.LENGTH_LONG).show();
				textStreetAddress1.setText("Not Available");
			}
		}
	};
	
	private Handler handlerSaveLocation = new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			if(msg.what == 0)
			{
				Toast.makeText(SetLocationReminder.this, "Location Reminder Set", Toast.LENGTH_LONG).show();
				Intent i = new Intent(SetLocationReminder.this,HomeActivity.class);
				finish();
				startActivity(i);
			}
			else
			{
				Toast.makeText(SetLocationReminder.this,"Cannot Save, Please try again", Toast.LENGTH_LONG).show();
				textStreetAddress1.setText("Not Available");
			}
		}
	};
}