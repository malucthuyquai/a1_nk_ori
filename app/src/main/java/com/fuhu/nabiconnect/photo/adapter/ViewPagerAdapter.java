package com.fuhu.nabiconnect.photo.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
	public static final String TAG = "ViewPagerAdapter";
	
	private ArrayList<View> views;

	public ViewPagerAdapter(ArrayList<View> views) {
		this.views = views;
	}

	public ImageView GetPhotoImageView(int position){
		if(!views.isEmpty())
			return (ImageView) views.get(position);
		else 
			return null;
	}
	
	@Override
	public int getCount() {
		if (views != null) {
			//LOG.I(TAG, "viewpager size = " + views.size());
			return views.size();			
		}
		return 0;
	}

	
	@Override
	public Object instantiateItem(View view, int position) {

		((ViewPager) view).addView(views.get(position), 0);

		return views.get(position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(View view, int position, Object arg2) {
		((ViewPager) view).removeView(views.get(position));
	}

}
