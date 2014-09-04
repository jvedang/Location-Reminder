package com.stealz.lib;

/**

@author - Vedang Jadhav
**/
import java.util.ArrayList;

import com.stealz.locationreminder.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<String> 
{
	private int resource;
	private LayoutInflater inflater;
	ArrayList<String> reminderName,currentStatus,address;
	
	public ListAdapter(Context context, int textViewResourceId,ArrayList<String> reminderName,ArrayList<String> currentStatus, 
			ArrayList<String> address)
	{
		super(context, textViewResourceId, reminderName);
		this.resource = textViewResourceId;
		this.inflater = LayoutInflater.from(context);
		this.reminderName = reminderName;
		this.currentStatus = currentStatus;
		this.address = address;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		convertView = (LinearLayout)inflater.inflate(resource,null);
		
		TextView textRowReminderName = (TextView)convertView.findViewById(R.id.textRowReminderName);
		TextView textRowCurrentStatus = (TextView)convertView.findViewById(R.id.textRowCurrentStatus);
		TextView textRowAddres = (TextView)convertView.findViewById(R.id.textRowAddress);
		
		textRowReminderName.setText(reminderName.get(position));
		textRowCurrentStatus.setText(currentStatus.get(position));
		textRowAddres.setText(address.get(position));
		
		return convertView;
	}
}
