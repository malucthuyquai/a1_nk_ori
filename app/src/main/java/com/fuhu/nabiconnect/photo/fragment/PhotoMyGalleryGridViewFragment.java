package com.fuhu.nabiconnect.photo.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.fuhu.data.SharedPhotoData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.PhotoActivity;
import com.fuhu.nabiconnect.photo.PhotoActivity.UserBehaviorListener;
import com.fuhu.nabiconnect.photo.adapter.PhotoUrlAdapter;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager.OnTaskCompleted;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;

import java.util.ArrayList;

public class PhotoMyGalleryGridViewFragment extends Fragment implements OnTaskCompleted{
	public final static String TAG = "MyGalleryGridViewFragment";
	private static final int GetSharedPhotoMaxNum = PhotoParameter.LoadphotofromServerNumber;
	
	private PhotoActivity mAct;
	private View view;
	private GridView mGridView;
	
	private PhotoUrlAdapter PhotoAdapter;
	
	private ArrayList<String> SharedPhotoIdArray = new ArrayList<String>();
	private ArrayList<SharedPhotoData> UserSharedPhotoData = new ArrayList<SharedPhotoData>();

	private String UserId;
	private ImageDownLoadManager mImageDM;
	private UserBehaviorListener userBehaviorListener;
	private boolean IsUpdatePhotoing = false;
		
	public void SetPhotoListData(ArrayList<SharedPhotoData> SharedPhotoData){
		this.UserSharedPhotoData = SharedPhotoData;
		LOG.I(TAG, "UserSharedPhotoData size = " + UserSharedPhotoData.size());
	}
	
	public void SetUserBehaviorListener(UserBehaviorListener listener) {
		this.userBehaviorListener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		LOG.I(TAG,"onCreateView");
		mAct =(PhotoActivity) getActivity();
		mImageDM = mAct.getImageDownloadManager();
		UserId = mAct.getUserId();
		mImageDM.ReSetPhotoFinishCount();
		
		
		view = inflater.inflate(R.layout.photo_mygallerygrid_layout, null);	
		mGridView = (GridView)view.findViewById(R.id.MyGalleryGridView);
		mGridView.setVerticalScrollBarEnabled(false);
		mGridView.setOnItemClickListener(itemclicklistener);				
		mGridView.setOnTouchListener(otl);
		
		IsUpdatePhotoing = false;
		LOG.W(TAG, "onCreateView------UserSharedPhotoData size = "+UserSharedPhotoData.size());
		SetPhotoInAdapter(this.UserSharedPhotoData);
		
		PhotoAdapter = new PhotoUrlAdapter(mAct, SharedPhotoIdArray, mImageDM);
		mGridView.setAdapter(PhotoAdapter);
		
		
		return view;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();		
		LOG.I(TAG,"onResume");
			
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		LOG.I(TAG,"onDestroyView");
		mImageDM.CancelTask();
		mImageDM.ReSetPhotoFinishCount();
		SharedPhotoIdArray.clear();
	}
	

	private AdapterView.OnItemClickListener itemclicklistener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adaptarview, View view, int position, long arg3) {
			// TODO Auto-generated method stub
			
			LOG.I(TAG,"Photo position = " + position + " / " + mImageDM.GetPhotoFinishCount());
			/*if(position < mImageDM.GetPhotoFinishCount())
				mAct.SwitchToShowPhotoFragmentFromGrid(position);	*/
			
			if(position < mImageDM.GetPhotoFinishCount())
				userBehaviorListener.ClickPhoto(position);	
		}		
	};

	private View.OnTouchListener otl = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			LOG.I(TAG,"IsUpdatePhotoing = " + IsUpdatePhotoing);
			LOG.I(TAG,"mImageDM.GetPhotoFinishCount() = " + mImageDM.GetPhotoFinishCount());
			if (mGridView.getLastVisiblePosition() == mGridView.getCount() - 1
					&& !IsUpdatePhotoing && mImageDM.GetPhotoFinishCount() >= GetSharedPhotoMaxNum) {
				
				LOG.I(TAG, "grid view bottom");
				IsUpdatePhotoing = true;
				userBehaviorListener.ScrollBottomLoading();

			}
			return false;
		}
	};
	
	public void SetPhotoInAdapter(ArrayList<SharedPhotoData> datalist){
			
		for(SharedPhotoData data : datalist){
			String photoUrl = data.url.replaceAll("https", "http");
			String thumbnailurl = data.url_tn;
			String photoId = data.id;	
			
			SharedPhotoIdArray.add(photoId);
			mImageDM.LoadImageFromServerForPhoto(thumbnailurl, photoId, this.UserId, photoUrl, null, this);
		}
	
	}
	
	public void UpdateHistoryPhotos(ArrayList<SharedPhotoData> list) {
		LOG.W(TAG, "UpdateHistoryPhotos");
		SetPhotoInAdapter(list);
		int y = mGridView.getScrollY();
		PhotoAdapter.SetUrlAndIdList(SharedPhotoIdArray, mImageDM);
		PhotoAdapter.notifyDataSetChanged();
		mGridView.setScrollY(y);
	
		IsUpdatePhotoing = false;
	}

	@Override
	public void onTaskCompleted() {
		// TODO Auto-generated method stub
		
		LOG.E(TAG, "onTaskCompleted");
		int y = mGridView.getScrollY();
		PhotoAdapter.notifyDataSetChanged();
		mGridView.setScrollY(y);
	}
	
}
