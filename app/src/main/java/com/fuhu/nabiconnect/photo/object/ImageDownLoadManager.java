package com.fuhu.nabiconnect.photo.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ImageDownLoadManager {
	private static final String TAG = "ImageDownLoadManager";
	private static Context mCtx;
	static MemoryCache memoryCache = new MemoryCache(mCtx);
	
	private static String TAGUPDATE_AVATAR = "UpdateAvatar";
	private static String TAGAVATAR = "Avatar";
	private static String TAGPHOTO = "Photo";
	public int Mode;
	
	private int AvatarFinishCount = 0;
	private int PhotoFinishCount = 0;
	
	private ArrayList<DownloadPhotoToCacheTaskWithProgressRate> DownloadPhotoToCacheTaskWithProgressRateTaskArray = new ArrayList<DownloadPhotoToCacheTaskWithProgressRate>();
	
	public OnTaskCompleted listener;
	
	DatabaseAdapter db;
	
	public interface OnTaskCompleted{
	    void onTaskCompleted();
	}

	public ImageDownLoadManager(Context ctx){
		mCtx = ctx;	
		db = DatabaseAdapter.getInstance(mCtx);
		Mode=0;
	}
	
	//load photo for showfragment
	public void LoadBigSizePhotoToMemoryCache(String url, String key, ImageView view){
		
		Drawable _drawable = memoryCache.get(key);

		if (_drawable != null) {
			
			view.setImageDrawable(_drawable);
		} else if (url != null) {
			
			DownloadPhotoToCacheTaskWithProgressRate task = new DownloadPhotoToCacheTaskWithProgressRate(
					key, null, null, "", view, null);		
			DownloadPhotoToCacheTaskWithProgressRateTaskArray.add(task);
			//task.execute(url);	
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
			
		} else {
			LOG.E(TAG, "the photo not found and no url !");
			
		}
						
	}
	
	public void LoadToDataBaseAndUpdateAvatarView(String url, String key,
			String userid, ImageView imgView) {
		
		DownloadPhotoToCacheTaskWithProgressRate task = new DownloadPhotoToCacheTaskWithProgressRate(
				key, userid, null, TAGUPDATE_AVATAR, null, null);
		DownloadPhotoToCacheTaskWithProgressRateTaskArray.add(task);
		// task.execute(url);
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
	}
	
	public void LoadImageFromServerForAvatar(String url, String key, String userid, String bigurl, ImageView imgView, OnTaskCompleted listener){
		Drawable drawable = memoryCache.get(key);
		
		if(drawable != null){
			if(imgView != null)
				imgView.setImageDrawable(drawable);
			
			AvatarFinishCount++;	
			
			if(listener != null)
				listener.onTaskCompleted();
			LOG.I(TAG, "cache end");
		}else if(db.friendAvatarExist(userid, key)){
			 url = null;
			 LoadAvatarFromDBTask dbtask = new LoadAvatarFromDBTask(imgView, listener);
			 dbtask.execute(key, userid);
			 LOG.I(TAG, "database end");
		}else if(url != null){
			DownloadPhotoToCacheTaskWithProgressRate task = new DownloadPhotoToCacheTaskWithProgressRate(
					key, userid, bigurl, TAGAVATAR, imgView, listener);
			DownloadPhotoToCacheTaskWithProgressRateTaskArray.add(task);
			//task.execute(url);		
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
			LOG.I(TAG, "url end");
		}else{
			LOG.E(TAG, "the avatar not found and no url !");
			AvatarFinishCount++;	
			listener.onTaskCompleted();
		}	
		
	}
	
	public void LoadImageFromServerForPhoto(String url, String key, String userid, String bigurl, ImageView imgView, OnTaskCompleted listener){
		Drawable drawable = memoryCache.get(key);
		
		if(drawable != null){
			if(imgView != null)
				imgView.setImageDrawable(drawable);
			
			PhotoFinishCount++;	
			LOG.I(TAG, "photo cache end");
		}else if(db.photoThumbnailExist(userid, key)){
			url = null;
			LoadPhotoFromDBTask dbtask = new LoadPhotoFromDBTask(imgView, listener);
			dbtask.execute(key, userid);		
			LOG.I(TAG, "photo database end");
		}else if(url != null){
			DownloadPhotoToCacheTaskWithProgressRate task = new DownloadPhotoToCacheTaskWithProgressRate(
					key, userid, bigurl, TAGPHOTO, imgView, listener);
			DownloadPhotoToCacheTaskWithProgressRateTaskArray.add(task);
			//task.execute(url);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
			LOG.I(TAG, "photo url end");		
			
		}else{
			LOG.E(TAG, "the Photo not found and no url !");
			
		}	
		
	}
		
	public void CancelTask(){
		LOG.E(TAG, "Cancel Task ");
		
		for(int i = 0; i<DownloadPhotoToCacheTaskWithProgressRateTaskArray.size(); i++){
			DownloadPhotoToCacheTaskWithProgressRateTaskArray.get(i).cancel(true);
		}
		
		DownloadPhotoToCacheTaskWithProgressRateTaskArray.clear();
		
	}

	public void ReSetAvatarFinishCount(){
		this.AvatarFinishCount = 0;
	}
	
	public void ReSetPhotoFinishCount(){
		this.PhotoFinishCount = 0;
	}
	
	public int GetAvatarFinishCount(){
		return this.AvatarFinishCount;
	}

	public int GetPhotoFinishCount(){
		return this.PhotoFinishCount;
	}
	
		 
//------------------------
	
	// Loading friend avatar from database task
	private class LoadAvatarFromDBTask extends AsyncTask<String, Void, Bitmap> {
		
		private ImageView imageView;
		private String key;	
		private String userid;
		private OnTaskCompleted listener;
		
		public LoadAvatarFromDBTask(ImageView imageView, OnTaskCompleted listener) {
			this.imageView = imageView;
			this.listener = listener;					
		}		

		protected Bitmap doInBackground(String... object) {
			key = object[0];
			userid = object[1];
			Bitmap bmp = null;

			if (!isCancelled()) {
				
				bmp = db.getFriendAvatar(userid, key, 316);
				
				return bmp;
			}else{				
				return null;
			}
		}

		protected void onPostExecute(Bitmap result) {
 
			if (result != null) {
				LOG.I(TAG, "load avatar from database success");

				Drawable drawable = new BitmapDrawable(result);
				imageView.setImageDrawable(drawable);
				memoryCache.put(key, drawable);
				AvatarFinishCount++;
				if(listener != null)
					listener.onTaskCompleted();
											
			} else {
				LOG.E(TAG, "the avatar is not exist in database !!");
			}

		}
	}

	// Loading photo from database task
	private class LoadPhotoFromDBTask extends AsyncTask<String, Void, Bitmap> {
		
		private ImageView imageView;
		private String key;	
		private String userid;
		private OnTaskCompleted listener;
				
		public LoadPhotoFromDBTask(ImageView imageView, OnTaskCompleted listener) {
			this.imageView = imageView;
			this.listener = listener;		
		}		

		protected Bitmap doInBackground(String... object) {
			key = object[0];
			userid = object[1];
			Bitmap bmp = null;

			if (!isCancelled()) {
				
				bmp = db.getPhotoThumbnail(userid, key);
				
				return bmp;
			}else{				
				return null;
			}
		}

		protected void onPostExecute(Bitmap result) {
 
			if (result != null) {
				LOG.I(TAG, "load photo from database success");
							
				Drawable drawable = new BitmapDrawable(result);
				
				memoryCache.put(key, drawable);
				if(imageView != null)
					imageView.setImageDrawable(drawable);
		
				PhotoFinishCount++;
				LOG.E(TAG, "PhotoFinishCount = " + PhotoFinishCount);
				if(listener != null)
					listener.onTaskCompleted();
				
			} else {
				LOG.E(TAG, "the photo is not exist in database !!");
			}

		}
	}	

//---------------------------------------------------------------------------
	
	// Load big size photo from server and only save memory cache
	private class DownloadPhotoToCacheTaskWithProgressRate extends AsyncTask<String, Integer, Drawable> {

		private String url;
		private String key;
		private String bigurl;
		private String userid;
		private String tag = null;
		private ImageView view = null;
		private OnTaskCompleted listener = null;
		
		public DownloadPhotoToCacheTaskWithProgressRate(String key, String userid, String bigurl, String tag, 
				                                        ImageView view, OnTaskCompleted listener) {
			this.key = key;
			this.userid = userid;
			this.bigurl = bigurl;
			this.tag = tag;
			this.view = view;
			this.listener = listener;			
			
		}

		protected Drawable doInBackground(String... urls) {
			url = (String) urls[0];
			Drawable photo = null;

			while(!isCancelled()){
			
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response = null;
				try {
					response = client.execute(get);
				} catch (ClientProtocolException e1) {
					e1.printStackTrace();
				} catch(IllegalStateException e1){
					cancel(true);
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if (response == null) {
					return null;
				}

				HttpEntity entity = response.getEntity();
				InputStream is;
				try {
					is = entity.getContent();

					long total = entity.getContentLength();
					int count = 0;
					int length = -1;
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[1024];

					while ((length = is.read(buf)) != -1 && !isCancelled()) {
						baos.write(buf, 0, length);
						count += length;
						if((count * 100 / total) < 0)
							return null;
						// �ե�publishProgress���G�i��,�̫�onProgressUpdate��k�N�Q����
						publishProgress((int) (count * 100 / total));

					}
					is.close();
					baos.close();

					byte[] bitmapdata = baos.toByteArray();
					Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata,
							0, bitmapdata.length);

					photo = new BitmapDrawable(bitmap);

					return photo;
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return null;
				}
			}
			
			return null;
			
		/*	if (!isCancelled()) {
				try {
					InputStream is = (InputStream) new URL(url).getContent();

					photo = Drawable.createFromStream(is, "src name");
					LOG.I(TAG, "loading photo");
				} catch (Throwable e) {
					LOG.E(TAG, "failed to load photo", e);
				}
				return photo;
			} else {

				return null;
			}*/
			 
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			int d = values[0];
			LOG.V(TAG, " Loading... " + d + " %");

		}

		protected void onPostExecute(Drawable result) {
			
			if (result != null) {
				memoryCache.put(key, result);				
				
				if(view != null)				
					view.setImageDrawable(result);
								
				if(tag.equals(TAGAVATAR) && userid != null && key != null){
					BitmapDrawable BD = (BitmapDrawable) result;
					Bitmap BM = BD.getBitmap();
					db.saveAvatarAsync(userid, key, BM);
					AvatarFinishCount++;
					LOG.E(TAG, "AvatarFinishCount = " + AvatarFinishCount);
				}else if(tag.equals(TAGPHOTO) && userid != null && key != null){
					BitmapDrawable BD = (BitmapDrawable) result;
					Bitmap BM = BD.getBitmap();
					db.savePhotoThumbnailAsync(userid, key, BM);
					PhotoFinishCount++;	
					LOG.E(TAG, "PhotoFinishCount = " + PhotoFinishCount);
				}else if (tag.equals(TAGUPDATE_AVATAR)) {
					BitmapDrawable BD = (BitmapDrawable) result;
					Bitmap BM = BD.getBitmap();
					db.saveAvatarAsync(userid, key, BM);
					LOG.E(TAG, "only update Avatar and no add avatar count, count = " + AvatarFinishCount);
				}	
				
				if(listener != null)
					listener.onTaskCompleted();	
				
			} else {
				LOG.E(TAG, "Load fail !!");
						
				if (bigurl != null) {
					LOG.E(TAG, "try to Load big photo !!");
					
					DownloadPhotoToCacheTaskWithProgressRate loadbigtask = new DownloadPhotoToCacheTaskWithProgressRate(
							this.key, this.userid, null, TAGPHOTO, this.view, this.listener); 					
					loadbigtask.execute(bigurl);
					DownloadPhotoToCacheTaskWithProgressRateTaskArray.add(loadbigtask);
					//DownloadImageToCacheTaskArray.add(loadbigtask);
				}		
				
			}
		}
	}
	
	
	public static Drawable getDrawableFromURL(String src) {       
      //from web
	    try {
	        URL url = new URL(src);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setReadTimeout(10000 /* milliseconds */);
	        connection.setConnectTimeout(15000 /* milliseconds */);
	        connection.setDoInput(true);
	        connection.setInstanceFollowRedirects(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        

            
            Drawable _Drawable = Drawable.createFromStream(input, url.toString());


	        return _Drawable;
	        
	    } catch (IOException e) {
//	           System.out.println("=====IOException=====");
	           return null;
	    } catch (Throwable e){
	        e.printStackTrace();
	           if(e instanceof OutOfMemoryError){
	        	   memoryCache.clear();
	           }
	        
	        return null;
	    }
	}//getDrawableFromURL end
		
//------------------------
	public MemoryCache GetMemoryCache(){		
		return memoryCache;
	}
    
    public void clearCache() {
        memoryCache.clear();
        AvatarFinishCount = 0;
        PhotoFinishCount = 0;
    }
}
