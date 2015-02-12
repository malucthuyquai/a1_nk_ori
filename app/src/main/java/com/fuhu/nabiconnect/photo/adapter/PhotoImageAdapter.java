package com.fuhu.nabiconnect.photo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.photo.object.ViewHolder;

import java.util.ArrayList;

public class PhotoImageAdapter extends BaseAdapter {
	public final static String TAG = "PhotoImageAdapter";
	
	private LayoutInflater mInflater;
	
	private ArrayList<Bitmap> ResizePhotoGridList;
		
	public PhotoImageAdapter(Context mCtx, ArrayList<Bitmap> bitmaplist){
		this.mInflater = LayoutInflater.from(mCtx);
		this.ResizePhotoGridList = bitmaplist;
		
	}
		
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(!ResizePhotoGridList.isEmpty())
			return ResizePhotoGridList.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.photo_cameragallery_griditem, null);
			holder.img = (ImageView) convertView.findViewById(R.id.CameraGallery_GridPhotoItemView);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (!ResizePhotoGridList.isEmpty()) {
			holder.img.setImageBitmap(ResizePhotoGridList.get(position));
			holder.img.setScaleType(ScaleType.CENTER_CROP);
		}
         
        return convertView;
	}

}
