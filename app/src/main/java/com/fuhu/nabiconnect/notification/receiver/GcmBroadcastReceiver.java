package com.fuhu.nabiconnect.notification.receiver;

import android.content.Context;
import android.content.Intent;

import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.notification.service.NotificationDialogService;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class GcmBroadcastReceiver extends ClientCloudMessageReceiver.GCMResponseReceiver {

	public final static String TAG = "GcmBroadcastReceiver";
	
	@Override
	public void onNotifyMessage(String responsedMessage) {
		
		if(!NabiNotificationManager.isGcmEnabled)
			return;
		
		LOG.V(TAG, "onNotifyMessage() - responsedMessage is "+responsedMessage);
		Intent serviceIntent = new Intent(getContext(), NotificationDialogService.class);
		try {
			JSONObject json = new JSONObject(responsedMessage);
			serviceIntent.putExtra(NotificationDialogService.KEY_MESSAGE, json.getString(NotificationDialogService.KEY_MESSAGE));
		} catch (JSONException e) {
			LOG.E(TAG, "onNotifyMessage() - failed to parse responsedMessage",e);
		}
		getContext().startService(serviceIntent);
		
	}

	@Override
	public void onMessageSendError(String responsedMessage) {
		
	}

	@Override
	public void onMessageDelete(String responsedMessage) {
		
	}

	@Override
	public void onUnknownReceive(Context mContext, Intent mIntent) {
		
	}

}
