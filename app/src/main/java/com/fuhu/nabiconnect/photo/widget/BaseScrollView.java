package com.fuhu.nabiconnect.photo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.fuhu.nabiconnect.log.LOG;

public class BaseScrollView extends ScrollView {
	public static final String TAG = "BaseScrollView";
	
	private BaseScrollViewListener scrollViewListener = null;
	private BaseScrollStateListener mScrollListener = null; 

	private View view;

	public interface BaseScrollViewListener {
		void onScrollChanged(BaseScrollView scrollView, int x, int y, int oldx, int oldy);
	}
	
	public interface BaseScrollStateListener{
		void onBottom();  
        void onTop();  
        void onScroll();  
	}
	
	public BaseScrollView(Context context) {
		super(context);
		this.setOnTouchListener(onTouchListener);  
	}

	public BaseScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setOnTouchListener(onTouchListener);  
	}

	public BaseScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setOnTouchListener(onTouchListener);  
	}

	public void setScrollViewListener(BaseScrollViewListener scrollViewListener) {
		this.scrollViewListener = scrollViewListener;
	}
	
	public void setScrollViewListener(BaseScrollStateListener mScrollListener){
		this.mScrollListener = mScrollListener;
	}
	
	public void removeScrollViewListener(){
		this.scrollViewListener = null;
	}
	
	public void removeScrollStateListener(){
		this.mScrollListener = null;
	}
		
	public void getView() {
		this.view = getChildAt(0);
		if (view == null) {
			LOG.E(TAG, "view = null" );
		}
	}
	
	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
				
		if (scrollViewListener != null) {
			scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
		}		
	}
	
	OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				LOG.E(TAG, "touch down!");
				
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				
				if (view != null) {
					LOG.E(TAG, "getMeasuredHeight() = " + view.getMeasuredHeight()
							 + " getScrollY() = " + getScrollY()
							 + " getHeight() = " + getHeight());
									
					if (getScrollY() <= 1) {
						if (mScrollListener != null) {
							mScrollListener.onTop();
						}
					} else if (view.getMeasuredHeight() <= getScrollY() + getHeight()) {						
						if (mScrollListener != null) {
							mScrollListener.onBottom();
						}
					} else {
						if (mScrollListener != null) {
							mScrollListener.onScroll();
						}
					}
				}
				
				LOG.E(TAG, "touch up!");
				break;

			default:
				break;
			}
			return false;
		}

	};
	
}
