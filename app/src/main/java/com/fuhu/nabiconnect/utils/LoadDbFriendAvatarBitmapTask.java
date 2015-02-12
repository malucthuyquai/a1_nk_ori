package com.fuhu.nabiconnect.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.fuhu.nabiconnect.log.LOG;

public class LoadDbFriendAvatarBitmapTask extends AsyncTask<Object, Object, Bitmap> {

	public interface IOnBitmapLoaded{
		public void onBitmapLoaded(Bitmap bitmap);
	}
	
	public static final String TAG = "LoadDbFriendAvatarBitmapTask";
	
	IOnBitmapLoaded callback = null;
	DatabaseAdapter dbAdapter;
	String userKey;
	String targetKey;
	int requestWidth;
	
	@Override
	protected Bitmap doInBackground(Object... arg0) {	
		
		LOG.V(TAG, "doInBackground() - start");
		
		callback = (IOnBitmapLoaded)arg0[0];
		dbAdapter = (DatabaseAdapter)arg0[1];
		userKey = (String)arg0[2];
		targetKey = (String)arg0[3];
		requestWidth = (Integer)arg0[4];
			
		
		Bitmap avatar = null;

		try {
			avatar = dbAdapter.getFriendAvatar(userKey, targetKey, requestWidth);
		} catch (Throwable e) {
			LOG.E(TAG, "failed to load avatar",e);
		}		
		
		LOG.V(TAG, "doInBackground() - end");
		
		return avatar;
		
	}
	

	
	@Override
	protected void onPostExecute(Bitmap result) {
		
		super.onPostExecute(result);

		callback.onBitmapLoaded(result);
		
	}

}

