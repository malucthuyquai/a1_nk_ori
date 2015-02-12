package com.fuhu.nabiconnect.utils;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.fuhu.nabiconnect.log.LOG;

import java.io.InputStream;
import java.net.URL;

public class LoadAvatarTask extends AsyncTask<Object, Object, Drawable> {

	public static final String TAG = "LoadAvatarTask";
	
	ImageView iconView = null;
	String avatarUrl = null;
	
	@Override
	protected Drawable doInBackground(Object... arg0) {	
		iconView = (ImageView)arg0[0];
		avatarUrl = (String)arg0[1];
			
		Drawable avatar = null;

		try {
			LOG.V(TAG, "avatarUrl is "+avatarUrl);
			avatarUrl = Utils.updateUrlToHttp(avatarUrl);
            InputStream is = (InputStream) new URL(avatarUrl).getContent();
            avatar = Drawable.createFromStream(is, "src name");
		} catch (Throwable e) {
			LOG.E(TAG, "failed to load avatar",e);
		}		
		return avatar;
	}
	
	@Override
	protected void onPostExecute(Drawable result) {
		
		super.onPostExecute(result);
		
		if(result != null){
			iconView.setImageDrawable(result);
		}		
	}

}

