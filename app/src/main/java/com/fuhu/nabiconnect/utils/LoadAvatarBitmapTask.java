package com.fuhu.nabiconnect.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.fuhu.nabiconnect.log.LOG;

import java.io.InputStream;
import java.net.URL;

public class LoadAvatarBitmapTask extends AsyncTask<Object, Object, Bitmap> {

	public interface IOnBitmapLoaded {
		public void onBitmapLoaded(Bitmap bitmap);
	}

	public static final String TAG = LoadAvatarBitmapTask.class.getSimpleName();

	IOnBitmapLoaded callback = null;
	String avatarUrl = null;

	@Override
	protected Bitmap doInBackground(Object... arg0) {
		callback = (IOnBitmapLoaded) arg0[0];
		avatarUrl = (String) arg0[1];
		Bitmap avatar = null;
		try {
			avatarUrl = Utils.updateUrlToHttp(avatarUrl);
			InputStream is = (InputStream) new URL(avatarUrl).getContent();
			if (is != null) {
				avatar = BitmapFactory.decodeStream(is);
			}
		} catch (Throwable e) {
			LOG.E(TAG, "failed to load avatar", e);
		}
		return avatar;
	}

	/*
	 * @Override protected void onPostExecute(Drawable result) {
	 * 
	 * super.onPostExecute(result);
	 * 
	 * if(result != null){ if (result instanceof BitmapDrawable) { bitmap =
	 * ((BitmapDrawable)result).getBitmap(); }
	 * 
	 * Bitmap drawableBitmap = Bitmap.createBitmap(result.getIntrinsicWidth(),
	 * result.getIntrinsicHeight(), Config.ARGB_8888); Canvas canvas = new
	 * Canvas(drawableBitmap); result.setBounds(0, 0, canvas.getWidth(),
	 * canvas.getHeight()); result.draw(canvas);
	 * 
	 * bitmap = drawableBitmap; } }
	 */

	@Override
	protected void onPostExecute(Bitmap result) {
		callback.onBitmapLoaded(result);
	}
}
