package com.stealz.locationreminder;

/**
@author - Vedang Jadhav
**/
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.LocationClient;
import com.stealz.db.DbUtil;

public class LocationIntentService extends IntentService 
{
	private ArrayList<String> addresses,reminderNames,currentStatuses;
	private ArrayList<Integer> ids;
	private ArrayList<Integer> locationDistances;
	private String strAddress;
	private CommonMethods cm;

	public LocationIntentService()
	{
		super("Location Service Active");
	}

	public LocationIntentService(String name)
	{
		super("Location Service Active");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Location location = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);
		Log.i("InLocationIntentService", "In onHandleIntent");
		if(location !=null)
		{
			cm = new CommonMethods();


			DbUtil dbUtil = new DbUtil(this);
			try
			{
				Log.i("InLocationIntentService", "In onHandleIntent");
				addresses =  new ArrayList<String>();
				reminderNames = new ArrayList<String>();
				ids = new ArrayList<Integer>();
				currentStatuses = new ArrayList<String>();
				locationDistances = new ArrayList<Integer>();

				try
				{
					dbUtil.open();

					Cursor c = dbUtil.fetchAllValues();
					if( c!= null)
					{
						while(c.moveToNext())
						{
							String address,reminderName,currentStatus;
							int id;

							id = c.getInt(0);

							reminderName = c.getString(4);
							currentStatus = c.getString(3);
							address = c.getString(1);

							addresses.add(address);
							reminderNames.add(reminderName);
							ids.add(id);
							currentStatuses.add(currentStatus);
							locationDistances.add(0);
						}
					}
				}
				catch(Exception e)
				{

				}
				finally
				{
					dbUtil.close();
				}

				JSONObject JSONlocation = cm.getLocationInfo(location.getLatitude(), location.getLongitude());
				JSONlocation = JSONlocation.getJSONArray("results").getJSONObject(0);
				strAddress = JSONlocation.getString("formatted_address");
				Log.i("InLocationIntentService", strAddress);

				Thread th = new Thread(new Runnable() 
				{
					@Override
					public void run() 
					{
						try
						{
							for(int iCount=0 ; iCount<addresses.size() ; iCount++)
							{
								if(!currentStatuses.get(iCount).equals("dismissed"))
								{
									Log.d("strAddress", strAddress);
									strAddress = URLEncoder.encode(strAddress,"UTF-8");
									Log.d("addresses.get(iCount)", addresses.get(iCount));
									/**/
									double longDistance = cm.getDistances(strAddress, addresses.get(iCount));
									Log.d("longDistance", longDistance+" is the distance");
									if(longDistance != 0 && longDistance < 2)
									{
										Intent eIntent = new Intent(LocationIntentService.this,NotificationScreen.class);
										eIntent.putExtra("id",ids.get(iCount));
										eIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP );

										PendingIntent pIntent = PendingIntent.getActivity(LocationIntentService.this, 0,eIntent, PendingIntent.FLAG_UPDATE_CURRENT);

										Notification noti = new NotificationCompat.Builder(LocationIntentService.this)
										.setContentTitle(reminderNames.get(iCount))
										.setContentText(addresses.get(iCount))
										.setSmallIcon(R.drawable.ic_launcher)

										.setContentIntent(pIntent).build();

										NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

										// Hide the notification after its selected
										noti.flags |= Notification.FLAG_AUTO_CANCEL;

										notificationManager.notify(0, noti);

									}
								}
							}
						}
						catch(Exception e)
						{
							Log.d("Exception in LocationIntentService",e.getMessage()+" is the error");
						}

					}
				});
				th.start();
				
				/*Intent eIntent = new Intent(this,HomeScreen.class);
				PendingIntent pIntent = PendingIntent.getBroadcast(this, 0,eIntent, 0);
				Notification noti = new NotificationCompat.Builder(this)
				.setContentTitle("Locate Me! Reminder")
				.setContentText("Location - "+strAddress+" reached")
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent).build();

				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				// Hide the notification after its selected
				noti.flags |= Notification.FLAG_AUTO_CANCEL;

				notificationManager.notify(0, noti);*/
/*
				ArrayList<String> arrayAddressElements = new ArrayList<String>();

				String[] strAddressArray = strAddress.split(",");*/
				//strAddressArray.
				/*				for(int iCount = 0 ; iCount<countries.size() ; iCount++)
				{
					Log.i("InLocationIntentService", countries.get(iCount));
					if(strAddress.contains(countries.get(iCount)))
					{
						Log.i("InLocationIntentService", cities.get(iCount));
						if(strAddress.contains(cities.get(iCount)))
						{

							String[] split1 = streetAddresses1.get(iCount).split(",");
							Log.i("InLocationIntentService", split1.toString());
							for(int i = 0; i <split1.length;i++)
							{
								Log.i("InLocationIntentService", split1[i]);
								if(strAddress.toLowerCase().contains(split1[i].toLowerCase()))
								{
									String[] split2 = streetAddresses2.get(iCount).split(",");
									Log.i("InLocationIntentService", split2.toString());
									for(int j=0; j<split2.length;j++)
									{
										Log.i("InLocationIntentService", split2[j]);
										if(strAddress.toLowerCase().contains(split2[j].toLowerCase()))
										{
											if(currentStatuses.get(iCount).equalsIgnoreCase("active"))
											{
												Intent eIntent = new Intent(LocationIntentService.this,NotificationScreen.class);
												eIntent.putExtra("id",ids.get(iCount));
												eIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP );

												PendingIntent pIntent = PendingIntent.getActivity(this, 0,eIntent, 0);

												Notification noti = new NotificationCompat.Builder(this)
						 						.setContentTitle("Location Reminder")
												.setContentText(reminderNames.get(iCount))
												.setSmallIcon(R.drawable.ic_launcher)

												.setContentIntent(pIntent).build();

												NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

												// Hide the notification after its selected
												noti.flags |= Notification.FLAG_AUTO_CANCEL;

												notificationManager.notify(0, noti);
											}
										}
									}
								}
							}
						}
					}
				}*/
			}
			catch(Exception e)
			{
				e.printStackTrace();
				strAddress = "Not Available";
				Log.i("InLocationIntentService", "In onHandleIntent");
			}
		}
	}
}
