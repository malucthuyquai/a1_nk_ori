package com.fuhu.nabiconnect.photo.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.adapter.EditPhotoItemAdapter;

import java.util.ArrayList;


public class EditPhotoFramePortFragment extends Tracking.TrackingInfoFragment {

	public static final String TAG = "EditPhotoFramePortFragment";
	
	private Activity mAct;
	
	private int mNowPhotoWidth;
	private int mNowPhotoHeight;
	private int ScreenWidth = 0;
	private static int SelectItemWidth = 150;
	private static int SelectItemHeight = 200;
	private final static int FrameLoadNum = 20;
	private boolean IsSelectItemFlag = false;
	
	private FrameLayout mShowPhotoLayout;
	
	private Bitmap bitmap;
	private Bitmap tempFrameBitmap;
	
	private ImageView mFrameView;
	
	
	private ArrayList<Bitmap> SelectItemFramesList = new ArrayList<Bitmap>();
	private ArrayList<LoadingImageFromResTask> LoadingImageFromResTaskArray = new ArrayList<LoadingImageFromResTask>();
	
	private ListView mFrameListView;
	
	private int[] FramePictures = {
			R.drawable.photo_nabiphoto_framecancel_vertical,
			R.drawable.photo_nabiphoto_frames_vertical01,
			R.drawable.photo_nabiphoto_frames_vertical02,
			R.drawable.photo_nabiphoto_frames_vertical03,
			R.drawable.photo_nabiphoto_frames_vertical04,
			R.drawable.photo_nabiphoto_frames_vertical05 };
	
	private int[] ResizeFramePictures = {
			R.drawable.photo_nabiphoto_resize_framecancel_vertical,
			R.drawable.photo_nabiphoto_resize_frames_vertical01,
			R.drawable.photo_nabiphoto_resize_frames_vertical02,
			R.drawable.photo_nabiphoto_resize_frames_vertical03,
			R.drawable.photo_nabiphoto_resize_frames_vertical04,
			R.drawable.photo_nabiphoto_resize_frames_vertical05 };
	
	private EditPhotoItemAdapter FrameAdapter;
	
	RelativeLayout.LayoutParams rlp;

    public EditPhotoFramePortFragment() {
        super(EditPhotoFramePortFragment.class.getSimpleName());
    }
    @Override
    public String getTrack() {
        return Tracking.TRACK_PHOTO_EDIT_PHOTO;
    }

    public void SetEditPhotoFramePortFragmentPrameter (int photoW, int photoH, FrameLayout showphotolayout, ImageView mFrameView){
		LOG.I(TAG, "SetEditPhotoFramePortFragmentPrameter ");
		
		this.mNowPhotoWidth = photoW;
		this.mNowPhotoHeight = photoH;
		this.mShowPhotoLayout = showphotolayout;
		this.mFrameView = mFrameView;
		
	}



    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		LOG.I(TAG, " onCreateView ");
		mAct = getActivity();
		Bundle bundle = new Bundle();
		bundle = getArguments();
		ScreenWidth = bundle.getInt("screenwidth", 0);
		SetResolutionParameter();	
		
	
		
		rlp = new RelativeLayout.LayoutParams(SelectItemWidth, SelectItemHeight);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		for(int res : ResizeFramePictures){
			LoadingImageFromResTask task = new LoadingImageFromResTask();
			task.execute(res);
			LoadingImageFromResTaskArray.add(task);
		}
		//LoadFramesFromRes(0);

		FrameAdapter = new EditPhotoItemAdapter(mAct, SelectItemFramesList, rlp);
		mFrameListView = new ListView(mAct);
		mFrameListView.setVerticalScrollBarEnabled(false);
		mFrameListView.setSelector(R.drawable.photo_item_select_frame);
		mFrameListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mFrameListView.setAdapter(FrameAdapter);
		// mFrameListView.setItemChecked(0, true);
		mFrameListView.setOnItemClickListener(itemclicklistener);
		//mFrameListView.setOnScrollListener(scrollListener);
		
		return mFrameListView;
	}
	
	private void SetResolutionParameter() {
		LOG.I(TAG,"screen width = " + ScreenWidth);
		switch (ScreenWidth) {
		case 1920:			
			SelectItemWidth = 189;
     	    SelectItemHeight = 252;		
			break;
		case 1280:
			SelectItemWidth = 120;
     	    SelectItemHeight = 160;		
			break;
		case 1600:
			SelectItemWidth = 150;
     	    SelectItemHeight = 200;
			break;
		case 1024:
			SelectItemWidth = 120;
     	    SelectItemHeight = 160;
			break;
		case 800:
			SelectItemWidth = 75;
     	    SelectItemHeight = 100;
			break;
		default:
			break;
		}
	}	

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		for(LoadingImageFromResTask task : LoadingImageFromResTaskArray)
			task.cancel(true);
		SelectItemFramesList.clear();
	}



	private AdapterView.OnItemClickListener itemclicklistener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			
			LOG.I(TAG, "item position = " + position);
			if(IsSelectItemFlag){
				if (position == 0) {
					// mFrameListView.setItemChecked(0, false);
					mShowPhotoLayout.removeView(mFrameView);

				} else {

					mShowPhotoLayout.removeView(mFrameView);
				
					bitmap = Bitmap.createBitmap(mNowPhotoWidth, mNowPhotoHeight, Config.ARGB_8888);
					
					tempFrameBitmap = combineLayer(bitmap, FramePictures[position]);
					// tempFrameBitmap = combineLayer(bitmap, ResizeFramesList.get(position));
					mFrameView.setImageBitmap(tempFrameBitmap);
					mShowPhotoLayout.addView(mFrameView);
				}
			}

            //tracking
            Tracking.pushTrack(getActivity(), "select_vertical_frame_#" +
                    ((FramePictures.length > position) ? FramePictures[position] : position));
		}		
	};
	
	private AbsListView.OnScrollListener scrollListener = new OnScrollListener(){

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
			
			
			if  (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {      
				//IsLastRow  = true ; 
				
				LoadFramesFromRes(totalItemCount);
				
				if(totalItemCount < ResizeFramePictures.length){
				
					FrameAdapter = new EditPhotoItemAdapter(mAct, SelectItemFramesList, rlp);
					mFrameListView.setAdapter(FrameAdapter);
					mFrameListView.setSelection(firstVisibleItem);
				}
								
				LOG.E(TAG, "visibleItemCount = " + visibleItemCount);
				LOG.E(TAG, "firstVisibleItem = " + firstVisibleItem);
				LOG.E(TAG, " totalItemCount = " +  totalItemCount);
            }      
			
			
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void LoadFramesFromRes(int initnum){
		
		if((FrameLoadNum + initnum) > ResizeFramePictures.length){
			
			/*for (int i = initnum; i < FramePictures.length; i++) {
				LOG.V(TAG, "sticker loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), FramePictures[i]);
				NoResizeStickersList.add(bmp);
			}*/
			
			for (int i = initnum; i < ResizeFramePictures.length; i++) {
				LOG.V(TAG, "sticker item loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), ResizeFramePictures[i]);
				SelectItemFramesList.add(bmp);
			}
			
			
		}else{
			
			/*for (int i = initnum; i < (FrameLoadNum + initnum); i++) {
				LOG.V(TAG, "sticker loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), FramePictures[i]);
				NoResizeStickersList.add(bmp);
			}
*/
			for (int i = initnum; i < (FrameLoadNum + initnum); i++) {
				LOG.V(TAG, "sticker item loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), ResizeFramePictures[i]);
				SelectItemFramesList.add(bmp);
			}
		}
	}

	
	private Bitmap combineLayer(Bitmap bmp, int res) {
		
		LOG.I(TAG, " Frame mNowPhotoWidth = " + bmp.getWidth() + "mNowPhotoHeight = " + bmp.getHeight());

		Bitmap overlayer = BitmapFactory.decodeResource(mAct.getResources(), res);
		
		try {
			Canvas c = new Canvas(bmp);
			
			Drawable drawable1 = new BitmapDrawable(bmp);
			Drawable drawable2 = new BitmapDrawable(overlayer);
			
			drawable1.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
			drawable2.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
			drawable1.draw(c);
			drawable2.draw(c);
			c.save(Canvas.ALL_SAVE_FLAG);
			c.restore();
		} catch (Exception e) {
		}
		
		return bmp;
	}
	
	public void SetSelectItme(boolean flag) {

		this.IsSelectItemFlag = flag;

	}
	
	private class LoadingImageFromResTask extends AsyncTask<Integer, Void, Bitmap>{
		
		@Override
		protected Bitmap doInBackground(Integer... arg0) {
			// TODO Auto-generated method stub
			int res = (int)arg0[0];
			
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);			
			SelectItemFramesList.add(result);
			FrameAdapter.notifyDataSetChanged();
		}	
		
	}
	
}
