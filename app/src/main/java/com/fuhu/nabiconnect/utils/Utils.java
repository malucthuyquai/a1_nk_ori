package com.fuhu.nabiconnect.utils;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.EditText;

import com.fuhu.nabiconnect.log.LOG;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Utils {

	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "Utils";	
	//nabi mode key
	public final static String PRODUCTION_APIKEY = "ea4c424a-c213-48a1-acc1-5a2d33cfa040";
	//staging
	public static final String STAGING_APIKEY = "b3ea5a95-96d7-4a92-98f2-255f4063ecea";
	
	public static final String META_DATA_PROD = "prod";
	public static final String META_DATA_PROD1 = "prod1";
	public static final String META_DATA_STAGING = "staging";
	public static final String META_DATA_DEV = "dev";
	
	public static boolean isProductionVersion(Context ctx)
	{
		boolean result = true;
		
		String metaData = getMetaData(ctx);
		
		if(META_DATA_STAGING.equals(metaData) || META_DATA_DEV.equals(metaData))
			result = false;
		
		return result;
	}

	public static boolean isShowLog(Context ctx){
		
		String metaData = getMetaData(ctx);
		return !META_DATA_PROD.equals(metaData);
	}
	
	public static String getMetaData(Context ctx){
		try {
			ApplicationInfo ai = ctx.getPackageManager()
					.getApplicationInfo(ctx.getPackageName(),
							PackageManager.GET_META_DATA);
			return  ai.metaData.getString("server");
			
		} catch (Throwable tr) {
			LOG.E(TAG, "getMetaData() - failed to get meta data.");
			//e.printStackTrace();
		}
		return META_DATA_PROD;
	}
	
	public static String getAPIKEY(Context context) {	
		return isProductionVersion(context) ? PRODUCTION_APIKEY : STAGING_APIKEY;
	}
	


	public static boolean checkAppExsistence(Context context, String appName)
	{
		boolean result = false;
		
		if(context == null || appName == null)
			return result;
		
		PackageManager pm = context.getPackageManager();
		
		try {
			pm.getApplicationInfo(appName, 0);
		} catch (final NameNotFoundException e) {
			//LOG.V(TAG, "checkAppExsistence() - "+appName+" is not found.");
			return result;
		}
		
		result = true;
		return result;
	}




	
	public static Drawable getAppIconByAppName(Context context, String appName)
	{
		Drawable icon = null;
		
		if(context == null || appName == null)
			return icon;
		
		PackageManager pm = context.getPackageManager();
		ApplicationInfo ai;
		
		try {
		    ai = pm.getApplicationInfo(appName, 0);
		} catch (final NameNotFoundException e) {
		    ai = null;
		}
		if(ai != null)
			icon = ai.loadIcon(pm);		
		
		return icon;
	}
	
	public static String getAppLabelByAppName(Context context, String appName)
	{
		String label = null;
		
		if(context == null || appName == null)
			return label;
		
		PackageManager pm = context.getPackageManager();
		ApplicationInfo ai;
		
		try {
		    ai = pm.getApplicationInfo(appName, 0);
		} catch (final NameNotFoundException e) {
		    ai = null;
		}
		if(ai != null)
			label = ai.loadLabel(pm).toString();
		
		return label;
	}
	

	
	public static boolean isTextEmpty(EditText etText) {
		
		if(etText == null)
			return true;
		
	    if (etText.getText().toString().trim().length() > 0) {
	        return false;
	    } else {
	        return true;
	    }
	}
	
	public static boolean isShowingLog(Context context){
		
		if(getMetaData(context).equals("prod")){
			return false;
		}else{
			return true;
		}
	}
	
	public static boolean compareArrayList(ArrayList<String> list1, ArrayList<String> list2)
	{
		boolean result = true;
		
		if(list1 == null || list2 == null)
			result = false;
		else
		{
			if(list1.size() != list2.size())
				result = false;
			else
			{
				for(String str : list1)
				{
					if(list2.contains(str))
						continue;
					else
					{
						result = false;
						break;
					}
				}
			}
		}
		
		return result;
	}
	
	public static String saveToInternalSorage(Context context, Bitmap bitmapImage){
        return saveToInternalSorage(context, bitmapImage, "tempImage.png");
    }
	
	public static String saveToInternalSorage(Context context, Bitmap bitmapImage, String requestedTempFileName){
        ContextWrapper cw = new ContextWrapper(context);
         // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        String tempFileName = requestedTempFileName;
        File mypath=new File(directory, tempFileName);

        FileOutputStream fos = null;
        try {           

            fos = new FileOutputStream(mypath);

       // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.V(TAG, " big temppath = " + directory.getAbsolutePath()+"/"+tempFileName);
        return directory.getAbsolutePath()+"/"+tempFileName;
    }
	
	// =============add function by ricky
	public static String savethumbnailToInternalSorage(Context context, Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(context);
         // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        String tempFileName = "thumbnailtempImage.png";
        File mypath = new File(directory, tempFileName);

        FileOutputStream fos = null;
        try {           

            fos = new FileOutputStream(mypath);

       // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 10, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.V(TAG, "temppath = " + directory.getAbsolutePath()+"/"+tempFileName);
        return directory.getAbsolutePath()+"/"+tempFileName;
    }
	
	public static String createSizeString(int width, int height)
	{
		return String.valueOf(width)+"x"+String.valueOf(height);
	}
	
	public static String updateUrlToHttp(String url)
	{
		String newUrl = null;
		
		if(url != null)
		{
			//LOG.V(TAG, "updateUrlToHttp() - oldUrl is "+url);
			newUrl = url.replace("https://", "http://");
			//LOG.V(TAG, "updateUrlToHttp() - newUrl is "+newUrl);
			return newUrl;
		}
	
		return newUrl;
	}
	
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
	        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	    else
	        asyncTask.execute(params);
	}
}
