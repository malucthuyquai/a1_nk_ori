package com.fuhu.nabiconnect.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fuhu.nabiconnect.chat.ChatActivity;
import com.fuhu.nabiconnect.friend.FriendActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.MailActivity;
import com.fuhu.nabiconnect.photo.PhotoActivity;

public class SwitchKidIntentReceiver extends BroadcastReceiver{

	private static final String TAG = "SwitchKidIntentReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		LOG.V(TAG, "onReceive() - switch kid");
		
		if(FriendActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - FriendActivity.m_Activity.finish()");
			FriendActivity.m_Activity.finish();
			FriendActivity.m_Activity = null;
		}
		
		if(MailActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - MailActivity.m_Activity.finish()");
			MailActivity.m_Activity.finish();
			MailActivity.m_Activity = null;
		}
		
		if(ChatActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - ChatActivity.m_Activity.finish()");
			ChatActivity.m_Activity.finish();
			ChatActivity.m_Activity = null;
		}
		
		/*if(CameraActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - CameraActivity.m_Activity.finish()");
			CameraActivity.m_Activity.finish();
			CameraActivity.m_Activity = null;
		}
		
		if(CameraEditPhotoActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - CameraEditPhotoActivity.m_Activity.finish()");
			CameraEditPhotoActivity.m_Activity.finish();
			CameraEditPhotoActivity.m_Activity = null;
		}
		
		if(CameraGalleryActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - CameraGalleryActivity.m_Activity.finish()");
			CameraGalleryActivity.m_Activity.finish();
			CameraGalleryActivity.m_Activity = null;
		}
		
		if(MyGalleryActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - MyGalleryActivity.m_Activity.finish()");
			MyGalleryActivity.m_Activity.finish();
			MyGalleryActivity.m_Activity = null;
		}
		*/
		if(PhotoActivity.m_Activity != null)
		{
			LOG.V(TAG, "onReceive() - PhotoActivity.m_Activity.finish()");
			PhotoActivity.m_Activity.finish();
			PhotoActivity.m_Activity = null;
		}
	
	}

}
