package com.fuhu.nabiconnect.application;

import android.app.Application;

import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;

public class NabiconnectApplication extends Application {
	public static final String TAG = "NabiconnectApplication";

	public ImageDownLoadManager mImageDM;

	/**
	 * @author Ricky
	 */
	@Override
	public void onCreate() {
		// set NSA image cache limit
		BitmapAjaxCallback.setCacheLimit(100);
		BitmapAjaxCallback.setMaxPixelLimit(26214400);
		super.onCreate();
		LOG.I(TAG, "onCreate");
		mImageDM = new ImageDownLoadManager(this);
		// initialize database
		DatabaseAdapter dba = DatabaseAdapter.getInstance(this);
		dba.open();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		LOG.I(TAG, "onLowMemory");
		mImageDM.clearCache();
	}
}
