package com.fuhu.nabiconnect.notification;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.fuhu.nabiconnect.log.LOG;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class NotificationUtils {
	
	final private static String TAG = "NotificationUtils";

	final public static String PROPERTY_REG_ID = "registration_id";
	final public static String PROPERTY_APP_VERSION = "appVersion";
	final public static String SENDER_ID = "201454476226";

	/*
	public static String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			LOG.E(TAG, "Registration not found.");
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			LOG.E(TAG, "App version changed.");
			return "";
		}
		LOG.E(TAG, "getRegistrationId: " + registrationId);
		return registrationId;
	}

	public static void saveGCMId(Context context, String string) {
		final SharedPreferences prefs = getGCMPreferences(context);
		prefs.edit().putString(PROPERTY_REG_ID, string).commit();
	}

	public static SharedPreferences getGCMPreferences(Context context) {
		return context.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	*/
	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static String getMacAddress() {
		String interfaceName = "wlan0";
		String address = "";

		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				if (interfaceName != null) {
					if (!intf.getName().equalsIgnoreCase(interfaceName))
						continue;
				}
				byte[] mac = intf.getHardwareAddress();
				if (mac == null) {
					break;
				}
				StringBuilder buf = new StringBuilder();
				for (int idx = 0; idx < mac.length; idx++)
					buf.append(String.format("%02X:", mac[idx]));
				if (buf.length() > 0)
					buf.deleteCharAt(buf.length() - 1);
				address = buf.toString();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// ===================================
		if (address.isEmpty()) {
			interfaceName = "eth0";
			try {
				List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface intf : interfaces) {
					if (interfaceName != null) {
						if (!intf.getName().equalsIgnoreCase(interfaceName))
							continue;
					}
					byte[] mac = intf.getHardwareAddress();
					if (mac == null) {
						break;
					}
					StringBuilder buf = new StringBuilder();
					for (int idx = 0; idx < mac.length; idx++)
						buf.append(String.format("%02X:", mac[idx]));
					if (buf.length() > 0)
						buf.deleteCharAt(buf.length() - 1);
					address = buf.toString();
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		LOG.V(TAG, "getMacAddress: " + address);
		return address;
	}

	public static String getCountryCode(Context context) {
		return context.getResources().getConfiguration().locale.getCountry();
	}

	public static String getDisplayLanguage() {
		return Locale.getDefault().getLanguage();
	}
	
	
	public static Drawable getAppIconDrawableByPackageName(Context context, String packageName)
	{
		Drawable iconDrawable = null;
		
		if(context == null)
			return null;
		
		// get icon drawable
		PackageManager pm = context.getPackageManager();
		ApplicationInfo ai;
		
		try {
		    ai = pm.getApplicationInfo(packageName, 0);
		} catch (final NameNotFoundException e) {
		    ai = null;
		}
		if(ai != null)
			iconDrawable = ai.loadIcon(pm);	
		
		return iconDrawable;
	}
	
	public static Bitmap convertDrawableToBitmap(Context context, Drawable drawable)
	{
		
		if(context == null || drawable == null)
			return null;
		
		// convert drawable into bitmap
		if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	
	public static Bitmap getAppIconBitmapByPackageName(Context context, String packageName)
	{	
		if(context == null)
			return null;
		
		Drawable iconDrawable = getAppIconDrawableByPackageName(context, packageName);
		Bitmap bitmap = convertDrawableToBitmap(context, iconDrawable);
		
	    return bitmap;
	}
}
