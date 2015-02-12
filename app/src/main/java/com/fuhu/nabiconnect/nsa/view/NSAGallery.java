package com.fuhu.nabiconnect.nsa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

@SuppressWarnings("deprecation")
public class NSAGallery extends Gallery {

	final private String TAG = NSAGallery.class.getSimpleName();

	public NSAGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NSAGallery(Context context) {
		super(context);
	}

	public NSAGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return super.onFling(e1, e2, 0, velocityY);
		// return super.onFling(e1, e2, velocityX / 200, velocityY);
	}
}
