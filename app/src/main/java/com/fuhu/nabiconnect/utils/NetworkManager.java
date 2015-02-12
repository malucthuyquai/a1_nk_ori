package com.fuhu.nabiconnect.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fuhu.nabiconnect.log.LOG;

public class NetworkManager {
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "NetworkManager";
	public static final String INTENT_CONNECTION_DIALOG = "fuhu.action.nabiui.CONNECTWIFI";
	public static final int WIFI_DIALOG_REQUEST_CODE = 1001;
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private ConnectivityManager m_Connectivity;
	
	/*======================================================================
	 * Constructor
	 *=======================================================================*/
	public NetworkManager(Context context)
	{
		this.m_Context = context;		
		this.m_Connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	/*======================================================================
	 * Check if Wifi is enabled
	 *=======================================================================*/
	public boolean isWifiEnabled() {

		NetworkInfo info = this.m_Connectivity.getActiveNetworkInfo();
		if (info == null || !this.m_Connectivity.getBackgroundDataSetting()) {
			return false;
		}
		
		int netType = info.getType();
		if (netType == ConnectivityManager.TYPE_WIFI) {
			return info.isConnected();
		}
		
		return false;
	}
	
	public boolean checkWifiProcess()
	{
		if(isWifiEnabled())
			return true;
		else
		{
			LOG.V(TAG,"checkWifiProcess() - There is no wifi.");
			
			// show dialog
			Intent intent = new Intent(INTENT_CONNECTION_DIALOG);
			intent.putExtra("fullscreen",true);
			
			if(m_Context instanceof Activity)
			{
				((Activity)m_Context).startActivityForResult(intent, WIFI_DIALOG_REQUEST_CODE);
			}
			else
				m_Context.startActivity(intent);
			
			return false;
		}
	}
}
