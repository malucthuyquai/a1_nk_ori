package com.fuhu.nabiconnect.photo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.photo.object.ViewHolder;

import java.util.ArrayList;

public class EditPhotoItemAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<Bitmap> resizeItemsList;
	
	RelativeLayout.LayoutParams rlp;

	public EditPhotoItemAdapter(Context context, ArrayList<Bitmap> resizeItemsList, RelativeLayout.LayoutParams rlp) {
		this.mInflater = LayoutInflater.from(context);
		// Log.i("NabiPhoto","resizeItemsList:"+resizeItemsList);
		this.resizeItemsList = resizeItemsList;
		this.rlp = rlp;
	}
	
	public EditPhotoItemAdapter(LayoutInflater mInflater, ArrayList<Bitmap> resizeItemsList, RelativeLayout.LayoutParams rlp) {
		this.mInflater = mInflater;
		// Log.i("NabiPhoto","resizeItemsList:"+resizeItemsList);
		this.resizeItemsList = resizeItemsList;
		this.rlp = rlp;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (resizeItemsList != null) {
			return resizeItemsList.size();
		} else {
			return 0;
		}
		
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
		
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.photo_editphoto_list_item, null);
			holder.img = (ImageView) convertView.findViewById(R.id.EditPhoto_Item);
			holder.img2 = (ImageView) convertView.findViewById(R.id.EditPhoto_ItemSelectContent);
			holder.img2.setLayoutParams(rlp);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.img2.setImageBitmap(resizeItemsList.get(position));
		holder.img.setBackgroundResource(R.drawable.photo_editphoto_item_background);
		holder.img.setScaleType(ScaleType.FIT_CENTER);

		return convertView;
	}

}
