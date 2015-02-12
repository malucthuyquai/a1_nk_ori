package com.fuhu.nabiconnect.photo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.adapter.ViewPagerAdapter;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowPhotoFragment extends Fragment {
	public static final String TAG = "ShowPhotoFragment";

	private Activity mAct;
	
	private ViewPager ViewPager;
	private ViewPagerAdapter VPAdapter;
	private ArrayList<View> ViewList = new ArrayList<View>();
	private ArrayList<Drawable> DrawableList = new ArrayList<Drawable>();
	private ArrayList<String> PhotoUrlList = new ArrayList<String>();
	private ArrayList<String> PhotoIdList = new ArrayList<String>();
	
	private HashMap<String, Boolean> BigPhotoIsLoad = new  HashMap<String, Boolean>();
		
	private int position;
	
	private ImageDownLoadManager mImageDM = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mAct = getActivity();
		View rootView = inflater.inflate(R.layout.photo_show_photo_layout, container, false);		
		ViewPager = (ViewPager) rootView.findViewById(R.id.ViewPager);
		
		Bundle bundle = new Bundle();
		bundle = getArguments();
		position = bundle.getInt("photoposition", 0);
		LOG.I(TAG,"photo position = " + position);
		LOG.I(TAG,"DrawableList.size() = " + DrawableList.size());
	
		if(DrawableList.size() != 0){
	
			for(Drawable pic : DrawableList){
				ImageView view = new ImageView(mAct);
				view.setImageDrawable(pic);
				ViewList.add(view);
			}
		
			LOG.W(TAG,"ViewList size = " + ViewList.size());
			VPAdapter = new ViewPagerAdapter(ViewList);
			ViewPager.setAdapter(VPAdapter);
			ViewPager.setCurrentItem(position);
			
			ViewPager.setOnPageChangeListener(listener);
					
		}
		
		
		if(!PhotoUrlList.isEmpty() && !DrawableList.isEmpty()){
			LOG.W(TAG, "onPageSelected position = " + position);
			ImageView view = VPAdapter.GetPhotoImageView(position);

			String url = PhotoUrlList.get(position);
			String key = "bigsizephoto" + PhotoIdList.get(position);
			LOG.I(TAG, "big photo key = " + key);

			if (BigPhotoIsLoad.containsKey(key) && BigPhotoIsLoad.get(key)) {
				mImageDM.LoadBigSizePhotoToMemoryCache(null, key, view);
			} else if (!BigPhotoIsLoad.containsKey(key)) {
				BigPhotoIsLoad.put(key, true);
				mImageDM.LoadBigSizePhotoToMemoryCache(url, key, view);
			}
			
		}		
				
		return rootView;
	}
	
	
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		LOG.W(TAG,"onDestroyView");
		BigPhotoIsLoad.clear();
		ViewList.clear();
		if(mImageDM != null)
			mImageDM.CancelTask();
	}
	
	public void SetPhotoDrawableArray(ArrayList<Drawable> List){
		this.DrawableList = List;		
	}
	
	public void SetPhotoDrawbleUrlAndIdArray(ArrayList<String> urlList, ArrayList<String> idList, ImageDownLoadManager mImageDM){
		this.PhotoUrlList = urlList;
		this.PhotoIdList = idList;
		this.mImageDM = mImageDM;
	}
	
	private ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
				
			if(!PhotoUrlList.isEmpty()){
				LOG.W(TAG, "onPageSelected position = " + position);
				ImageView view = VPAdapter.GetPhotoImageView(position);

				String url = PhotoUrlList.get(position);
				String key = "bigsizephoto" + PhotoIdList.get(position);
				LOG.I(TAG, "big photo key = " + key);

				if (BigPhotoIsLoad.containsKey(key) && BigPhotoIsLoad.get(key)) {
					mImageDM.LoadBigSizePhotoToMemoryCache(null, key, view);
				} else if (!BigPhotoIsLoad.containsKey(key)) {
					BigPhotoIsLoad.put(key, true);
					mImageDM.LoadBigSizePhotoToMemoryCache(url, key, view);
				}

				VPAdapter.notifyDataSetChanged();
			}		
			
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
	};
}
