package com.fuhu.nabiconnect.photo.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.photo.object.ViewHolder;

import java.util.ArrayList;

public class PhotoUrlAdapter extends BaseAdapter {
	public final static String TAG = "PhotoUrlAdapter";

	private LayoutInflater mInflater;

	private ArrayList<String> PhotoIdList = new ArrayList<String>();
	private ArrayList<Drawable> PhotoDrawableArray = new ArrayList<Drawable>();
	
	private ImageDownLoadManager mImageDM;
				
	public PhotoUrlAdapter(Context mCtx, ArrayList<String> IdList, ImageDownLoadManager DM) {

		this.mInflater = LayoutInflater.from(mCtx);
		this.PhotoIdList.clear();
		this.PhotoIdList = IdList;
		LOG.I(TAG, "PhotoIdList  size () = "+ PhotoIdList.size());
		this.mImageDM = DM;
		//count =  GetFinishLoadingPhotoCount();
	}
	
	public void SetUrlAndIdList(ArrayList<String> IdList, ImageDownLoadManager DM){
 	   	
		this.PhotoIdList = IdList;
 	   	this.mImageDM = DM;	
 	   	
 	    //count =  GetFinishLoadingPhotoCount();
	}
	
	public int GetFinishLoadingPhotoCount(){
		
		PhotoDrawableArray.clear();
		for(String id : PhotoIdList){
			Drawable img = mImageDM.GetMemoryCache().get(id);
			if(img != null)
				PhotoDrawableArray.add(img);
			
		}
		LOG.I(TAG,"GetFinishLoadingPhotoCount size = " + PhotoDrawableArray.size());
		return PhotoDrawableArray.size();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (PhotoIdList != null) {
			return PhotoIdList.size();
		} else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return this.PhotoIdList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder = null;
			
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.photo_mygallery_griditem, null);
			holder.loadbar = (ProgressBar) convertView.findViewById(R.id.MyGallery_GridItem_marker_progress);
			holder.img = (ImageView) convertView.findViewById(R.id.MyGallery_GridPhotoItemView);
		
			convertView.setTag(holder);
					
		} else {
			holder = (ViewHolder) convertView.getTag();		
			LOG.I(TAG, "convertView not null Load photo gridview position ("+ position + ")");
		
		}
				
		holder.img.setImageDrawable(mImageDM.GetMemoryCache().get(PhotoIdList.get(position)));
		                 
        return convertView ;
	}
	
		
}
