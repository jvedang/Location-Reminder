package com.stealz.locationreminder;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.stealz.db.DbUtil;
import com.stealz.lib.ListAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class HomeActivity extends Activity implements OnClickListener,
ConnectionCallbacks,
OnConnectionFailedListener,
LocationListener,OnCheckedChangeListener
{
	private Button btnActive,btnDismissed;
	private ListView listOfReminders;
	private TextView textNoReminders;
	private LocationClient locationClient;
	private ImageView imageViewAddReminders;
	private ArrayList<String> reminderNames,currentStatuses,addresses,ids;
	private boolean activeReminders = true;
	private LocationRequest locationrequest;
	private Intent mIntentService;
	private PendingIntent mPendingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_activity);

		reminderNames = new ArrayList<String>();
		currentStatuses = new ArrayList<String>();
		addresses = new ArrayList<String>();
		ids = new ArrayList<String>();

		mIntentService = new Intent(this,LocationIntentService.class);
		mPendingIntent = PendingIntent.getService(this, 1, mIntentService, 0);

		final int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (result != ConnectionResult.SUCCESS)
		{
			Toast.makeText(this, "Google Play service is not available (status=" + result + ")", Toast.LENGTH_LONG).show();
			finish();
		}

		locationClient = new LocationClient(this, this, this);

		initializeLayouts();
		registerListeners();

		DbUtil dbUtil = new DbUtil(this);

		try
		{
			dbUtil.open();

			Cursor c = dbUtil.fetchAllValues();

			dbUtil = new DbUtil(this);

			try
			{
				dbUtil.open();
				c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status","_id"},
						"current_status = ?",new String[]{"active"});

				if(c != null)
				{
					if(c.getCount()>0)
					{
						textNoReminders.setText("");
						textNoReminders.setVisibility(View.GONE);
						while(c.moveToNext())
						{
							String reminderName = c.getString(1);
							String address = c.getString(0);
							String status = c.getString(2);
							String id = c.getString(3);

							reminderNames.add(reminderName);
							currentStatuses.add(status);
							addresses.add(address);
							ids.add(id);
						}
					}
					else
					{
						textNoReminders.setText("No Active Reminders !");
						textNoReminders.setVisibility(View.VISIBLE);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Toast.makeText(HomeActivity.this, "Error", Toast.LENGTH_SHORT).show();
			}
			finally
			{
				dbUtil.close();
			}
		}
		catch(Exception e)
		{}
		finally
		{
			dbUtil.close();
		}

		listOfReminders.setAdapter(new ListAdapter(this, R.layout.listreminder_row, reminderNames, currentStatuses, addresses));

		listOfReminders.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parentView, View view, final int position,
					long id)
			{
				if(activeReminders)
				{
					new AlertDialog.Builder(HomeActivity.this)
					.setMessage("Options")
					.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{ 
							DbUtil dbUtil = new DbUtil(HomeActivity.this);
							try
							{
								dbUtil.open();
								dbUtil.deleteTitle(ids.get(position));
								Toast.makeText(HomeActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

								dbUtil = new DbUtil(HomeActivity.this);
								try
								{
									dbUtil.open();

									Cursor c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status","_id"},
											"current_status = ?",new String[]{"active"});

									reminderNames = new ArrayList<String>();
									currentStatuses = new ArrayList<String>();
									addresses = new ArrayList<String>();
									ids = new ArrayList<String>();

									if(c != null)
									{
										if(c.getCount()>0)
										{
											textNoReminders.setText("");
											textNoReminders.setVisibility(View.GONE);
											while(c.moveToNext())
											{
												String reminderName = c.getString(1);
												String address = c.getString(0);
												String status = c.getString(2);
												String idInsideActive = c.getString(3);

												reminderNames.add(reminderName);
												currentStatuses.add(status);
												addresses.add(address);
												ids.add(idInsideActive);
											}
										}
										else
										{
											textNoReminders.setText("No Active Reminders !");
											textNoReminders.setVisibility(View.VISIBLE);
										}

									}

									listOfReminders.setAdapter(new ListAdapter(HomeActivity.this, R.layout.listreminder_row, reminderNames, currentStatuses, addresses));

									if (Context.NOTIFICATION_SERVICE!=null) 
									{
										String ns = Context.NOTIFICATION_SERVICE;
										NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
										nMgr.cancel(0);
									}
								}
								catch(Exception e)
								{

								}
								finally
								{
									dbUtil.close();
								}
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
					})
					.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{ 
							DbUtil dbUtil = new DbUtil(HomeActivity.this);
							try
							{
								dbUtil.open();
								dbUtil.updateRow(new Integer(ids.get(position)), "dismissed");
								Toast.makeText(HomeActivity.this, "Dismissed", Toast.LENGTH_SHORT).show();

								dbUtil = new DbUtil(HomeActivity.this);
								try
								{
									dbUtil.open();

									Cursor c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status","_id"},
											"current_status = ?",new String[]{"active"});

									reminderNames = new ArrayList<String>();
									currentStatuses = new ArrayList<String>();
									addresses = new ArrayList<String>();
									ids = new ArrayList<String>();

									if(c != null)
									{
										if(c.getCount()>0)
										{
											textNoReminders.setText("");
											textNoReminders.setVisibility(View.GONE);
											while(c.moveToNext())
											{
												String reminderName = c.getString(1);
												String address = c.getString(0);
												String status = c.getString(2);
												String idInsideActive = c.getString(3);

												reminderNames.add(reminderName);
												currentStatuses.add(status);
												addresses.add(address);
												ids.add(idInsideActive);
											}
										}
										else
										{
											textNoReminders.setText("No Active Reminders !");
											textNoReminders.setVisibility(View.VISIBLE);
										}
									}

									listOfReminders.setAdapter(new ListAdapter(HomeActivity.this, R.layout.listreminder_row, reminderNames, currentStatuses, addresses));

									if (Context.NOTIFICATION_SERVICE!=null) 
									{
										String ns = Context.NOTIFICATION_SERVICE;
										NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
										nMgr.cancel(0);
									}
								}
								catch(Exception e)
								{

								}
								finally
								{
									dbUtil.close();
								}
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
					})
					.show();



				}
				else
				{
					//Dismissed options
					new AlertDialog.Builder(HomeActivity.this)
					.setMessage("Options")
					.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{ 
							DbUtil dbUtil = new DbUtil(HomeActivity.this);
							try
							{
								dbUtil.open();
								dbUtil.deleteTitle(ids.get(position));
								Toast.makeText(HomeActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

								dbUtil = new DbUtil(HomeActivity.this);
								try
								{


									dbUtil.open();

									Cursor c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status","_id"},
											"current_status = ?",new String[]{"dismissed"});

									reminderNames = new ArrayList<String>();
									currentStatuses = new ArrayList<String>();
									addresses = new ArrayList<String>();
									ids = new ArrayList<String>();

									if(c != null)
									{
										if(c.getCount()>0)
										{
											textNoReminders.setText("");
											textNoReminders.setVisibility(View.GONE);
											while(c.moveToNext())
											{
												String reminderName = c.getString(1);
												String address = c.getString(0);
												String status = c.getString(2);
												String idInsideActive = c.getString(3);

												reminderNames.add(reminderName);
												currentStatuses.add(status);
												addresses.add(address);
												ids.add(idInsideActive);
											}
										}
										else
										{
											textNoReminders.setText("No Dismissed Reminders !");
											textNoReminders.setVisibility(View.VISIBLE);
										}
									}

									listOfReminders.setAdapter(new ListAdapter(HomeActivity.this, R.layout.listreminder_row, reminderNames, currentStatuses, addresses));
								}
								catch(Exception e)
								{

								}
								finally
								{
									dbUtil.close();
								}


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
					})
					.setNegativeButton("Make Active", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{ 
							DbUtil dbUtil = new DbUtil(HomeActivity.this);
							try
							{
								dbUtil.open();
								dbUtil.updateRow(new Integer(ids.get(position)), "active");
								Toast.makeText(HomeActivity.this, "Actived", Toast.LENGTH_SHORT).show();

								dbUtil = new DbUtil(HomeActivity.this);
								try
								{
									dbUtil.open();

									Cursor c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status","_id"},
											"current_status = ?",new String[]{"dismissed"});

									reminderNames = new ArrayList<String>();
									currentStatuses = new ArrayList<String>();
									addresses = new ArrayList<String>();
									ids = new ArrayList<String>();

									if(c != null)
									{
										if(c.getCount()>0)
										{
											textNoReminders.setText("");
											textNoReminders.setVisibility(View.GONE);
											while(c.moveToNext())
											{
												String reminderName = c.getString(1);
												String address = c.getString(0);
												String status = c.getString(2);
												String idInsideActive = c.getString(3);

												reminderNames.add(reminderName);
												currentStatuses.add(status);
												addresses.add(address);
												ids.add(idInsideActive);
											}
										}
										else
										{
											textNoReminders.setText("No Dismissed Reminders !");
											textNoReminders.setVisibility(View.VISIBLE);
										}
									}

									listOfReminders.setAdapter(new ListAdapter(HomeActivity.this, R.layout.listreminder_row, reminderNames, currentStatuses, addresses));


								}
								catch(Exception e)
								{

								}
								finally
								{
									dbUtil.close();
								}
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
					})
					.show();


				}
			}
		});

		btnActive.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				btnActive.setTextColor(getResources().getColor(R.color.whiteColor));
				btnActive.setBackground(getResources().getDrawable(R.color.backgroundColor));
				btnDismissed.setTextColor(getResources().getColor(R.color.blackColor));
				btnDismissed.setBackground(getResources().getDrawable(R.color.whiteColor));
				activeReminders = true;

				DbUtil dbUtil = new DbUtil(HomeActivity.this);
				try
				{


					dbUtil.open();

					Cursor c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status","_id"},
							"current_status = ?",new String[]{"active"});

					reminderNames = new ArrayList<String>();
					currentStatuses = new ArrayList<String>();
					addresses = new ArrayList<String>();
					ids = new ArrayList<String>();

					if(c != null)
					{
						if(c.getCount()>0)
						{
							textNoReminders.setText("");
							textNoReminders.setVisibility(View.GONE);
							while(c.moveToNext())
							{
								String reminderName = c.getString(1);
								String address = c.getString(0);
								String status = c.getString(2);
								String idInsideActive = c.getString(3);

								reminderNames.add(reminderName);
								currentStatuses.add(status);
								addresses.add(address);
								ids.add(idInsideActive);
							}
						}
						else
						{
							textNoReminders.setText("No Active Reminders !");
							textNoReminders.setVisibility(View.VISIBLE);
						}
					}

					listOfReminders.setAdapter(new ListAdapter(HomeActivity.this, R.layout.listreminder_row, reminderNames, currentStatuses, addresses));
				}
				catch(Exception e)
				{
					dbUtil.close();
				}
			}
		});

		btnDismissed.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				btnDismissed.setTextColor(getResources().getColor(R.color.whiteColor));
				btnDismissed.setBackground(getResources().getDrawable(R.color.backgroundColor));
				btnActive.setTextColor(getResources().getColor(R.color.blackColor));
				btnActive.setBackground(getResources().getDrawable(R.color.whiteColor));
				activeReminders = false;

				DbUtil dbUtil = new DbUtil(HomeActivity.this);
				try
				{
					dbUtil.open();

					Cursor c = dbUtil.query("locateme_table", new String[]{"address","reminder_name","current_status","_id"},
							"current_status = ?",new String[]{"dismissed"});

					reminderNames = new ArrayList<String>();
					currentStatuses = new ArrayList<String>();
					addresses = new ArrayList<String>();
					ids = new ArrayList<String>();

					if(c != null)
					{
						if(c.getCount()>0)
						{
							textNoReminders.setText("");
							textNoReminders.setVisibility(View.GONE);
							while(c.moveToNext())
							{
								String reminderName = c.getString(1);
								String address = c.getString(0);
								String status = c.getString(2);
								String idInsideActive = c.getString(3);

								reminderNames.add(reminderName);
								currentStatuses.add(status);
								addresses.add(address);
								ids.add(idInsideActive);
							}
						}
						else
						{
							textNoReminders.setText("No Dismissed Reminders !");
							textNoReminders.setVisibility(View.VISIBLE);
						}
					}

					listOfReminders.setAdapter(new ListAdapter(HomeActivity.this, R.layout.listreminder_row, reminderNames, currentStatuses, addresses));
				}
				catch(Exception e)
				{
					dbUtil.close();
				}
			}
		});
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

	private void initializeLayouts()
	{
		listOfReminders = (ListView)findViewById(R.id.listOfReminders);
		imageViewAddReminders = (ImageView)findViewById(R.id.imageViewAddReminder);
		btnActive = (Button)findViewById(R.id.btnActive);
		btnDismissed = (Button)findViewById(R.id.btnDismissed);
		textNoReminders = (TextView)findViewById(R.id.textNoReminders);
	}

	private void registerListeners()
	{ 
		imageViewAddReminders.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
		case R.id.imageViewAddReminder:
			Intent i = new Intent(HomeActivity.this, SetLocationReminder.class);
			finish();
			startActivity(i);
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location arg0) 
	{
		try
		{
			Location loc = locationClient.getLastLocation();
			Log.d("onLocationChanged", "location=" + loc.toString());

			/*getLocationFromThread(loc.getLatitude(), loc.getLongitude());*/

		}
		catch(Exception e)
		{
			Toast.makeText(HomeActivity.this, "Unable to locate, please try again..", Toast.LENGTH_LONG).show();
		}

	}



	@Override
	public void onConnected(Bundle arg0)
	{
		try
		{
			locationrequest = LocationRequest.create();
			locationrequest.setInterval(20000);
			locationClient.requestLocationUpdates(locationrequest, mPendingIntent);

			Location loc = locationClient.getLastLocation();
			Log.d("onConnected", "location=" + loc.toString());
			Toast.makeText(HomeActivity.this, "You are connected..", Toast.LENGTH_LONG).show();
			/*getLocationFromThread(loc.getLatitude(), loc.getLongitude());*/
		}
		catch(Exception e)
		{
			Toast.makeText(HomeActivity.this, "Unable to locate, please try again..", Toast.LENGTH_LONG).show();
		}	// TODO Auto-generated method stub

	}



	@Override
	public void onConnectionFailed(ConnectionResult arg0) 
	{
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
	}
	@Override
	public void onDisconnected() 
	{
		Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
	}
}
