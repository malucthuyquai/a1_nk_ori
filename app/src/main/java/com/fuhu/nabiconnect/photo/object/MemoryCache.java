package com.fuhu.nabiconnect.photo.object;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MemoryCache {

	private static final String TAG = "MemoryCache";
	private Context mCtx;
	private Map<String, Bitmap> cache = Collections
			.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));
	private long size = 0;
	private long limit = 1000000;

	public MemoryCache(Context ctx) {

		mCtx = ctx;
		setLimit(Runtime.getRuntime().maxMemory() / 4);
	}

	public void setLimit(long new_limit) {
		limit = new_limit;

	}

	public Drawable get(String id) {

		try {
			if (!cache.containsKey(id)) {

				return null;
			}
			// NullPointerException sometimes happen here
			// http://code.google.com/p/osmdroid/issues/detail?id=78

			Drawable bd = new BitmapDrawable(cache.get(id));

			// Log.e(TAG, "bd");
			return bd;
			// return cache.get(id);
		} catch (NullPointerException ex) {
			// Log.e(TAG, "NullPointerException");
			ex.printStackTrace();
			return null;
		}
	}

	public void put(String id, Drawable drawable) {
		BitmapDrawable bd = (BitmapDrawable) drawable;
		Bitmap bitmap = bd.getBitmap();
		try {
			if (cache.containsKey(id)) {

				size -= getSizeInBytes(cache.get(id));
			}
			cache.put(id, bitmap);
			size += getSizeInBytes(bitmap);
			checkSize();
		} catch (Throwable th) {

			th.printStackTrace();
		}
	}
    
    private void checkSize() {
//        Log.e(TAG, "cache size="+size+" length="+cache.size());
        if(size>limit){
            Iterator<Entry<String, Bitmap>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
            while(iter.hasNext()){
                Entry<String, Bitmap> entry=iter.next();
                size-=getSizeInBytes(entry.getValue());
                iter.remove();
                if(size<=limit)
                    break;
            }
//            Log.e(TAG, "Clean cache. New size "+cache.size());
        }
    }

	public void clear() {
		try {
			// NullPointerException sometimes happen here
			// http://code.google.com/p/osmdroid/issues/detail?id=78
			cache.clear();
			size = 0;
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	long getSizeInBytes(Bitmap bitmap) {
		if (bitmap == null)
			return 0;
		return bitmap.getRowBytes() * bitmap.getHeight();
	}
}