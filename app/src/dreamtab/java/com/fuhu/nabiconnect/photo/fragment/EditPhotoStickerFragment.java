package com.fuhu.nabiconnect.photo.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.adapter.EditPhotoItemAdapter;
import com.fuhu.nabiconnect.utils.stickerwidget.StickerButtonListener;
import com.fuhu.nabiconnect.utils.stickerwidget.StickerWidget;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class EditPhotoStickerFragment extends Fragment {

    public static final String TAG = "EditPhotoStickerFragment";

    private Activity mAct;
    private FrameLayout mShowPhotoLayout;
    private Bitmap StickerBitmap;

    private ImageView mPhotoView;

    private ArrayList<Bitmap> ResizeStickersList = new ArrayList<Bitmap>();
    private ArrayList<Bitmap> NoResizeStickersList = new ArrayList<Bitmap>();
    private ArrayList<StickerWidget> mStickerList = new ArrayList<StickerWidget>();

    private ListView mStickerListView;

    private int[] mStickers = {R.drawable.sticker_a_01,
            R.drawable.sticker_a_02, R.drawable.sticker_a_03,
            R.drawable.sticker_a_04, R.drawable.sticker_a_05,
            R.drawable.sticker_a_06, R.drawable.sticker_a_07,
            R.drawable.sticker_a_08, R.drawable.sticker_a_09,
            R.drawable.sticker_a_10, R.drawable.sticker_a_11,
            R.drawable.sticker_a_12, R.drawable.sticker_a_13,
            R.drawable.sticker_b_01, R.drawable.sticker_b_02,
            R.drawable.sticker_b_03, R.drawable.sticker_b_04,
            R.drawable.sticker_b_05, R.drawable.sticker_b_06,
            R.drawable.sticker_b_07, R.drawable.sticker_b_08,
            R.drawable.sticker_b_09, R.drawable.sticker_b_10,
            R.drawable.sticker_c_01, R.drawable.sticker_c_02,
            R.drawable.sticker_c_03, R.drawable.sticker_c_04,
            R.drawable.sticker_c_05, R.drawable.sticker_c_06,
            R.drawable.sticker_c_07, R.drawable.sticker_c_08,
            R.drawable.sticker_c_09, R.drawable.sticker_c_10,
            R.drawable.sticker_c_11, R.drawable.sticker_c_12,
            R.drawable.sticker_c_13, R.drawable.sticker_c_14,
            R.drawable.sticker_d_01, R.drawable.sticker_d_02,
            R.drawable.sticker_d_03, R.drawable.sticker_d_04,
            R.drawable.sticker_d_05, R.drawable.sticker_d_06,
            R.drawable.sticker_d_07, R.drawable.sticker_d_08,
            R.drawable.sticker_d_09, R.drawable.sticker_d_10,
            R.drawable.sticker_e_01, R.drawable.sticker_e_02,
            R.drawable.sticker_e_03, R.drawable.sticker_e_04,
            R.drawable.sticker_e_05, R.drawable.sticker_e_06,
            R.drawable.sticker_e_07, R.drawable.sticker_e_08};

    private int[] mResizeStickers = {R.drawable.sticker_a_01_resize,
            R.drawable.sticker_a_02_resize, R.drawable.sticker_a_03_resize,
            R.drawable.sticker_a_04_resize, R.drawable.sticker_a_05_resize,
            R.drawable.sticker_a_06_resize, R.drawable.sticker_a_07_resize,
            R.drawable.sticker_a_08_resize, R.drawable.sticker_a_09_resize,
            R.drawable.sticker_a_10_resize, R.drawable.sticker_a_11_resize,
            R.drawable.sticker_a_12_resize, R.drawable.sticker_a_13_resize,
            R.drawable.sticker_b_01_resize, R.drawable.sticker_b_02_resize,
            R.drawable.sticker_b_03_resize, R.drawable.sticker_b_04_resize,
            R.drawable.sticker_b_05_resize, R.drawable.sticker_b_06_resize,
            R.drawable.sticker_b_07_resize, R.drawable.sticker_b_08_resize,
            R.drawable.sticker_b_09_resize, R.drawable.sticker_b_10_resize,
            R.drawable.sticker_c_01_resize, R.drawable.sticker_c_02_resize,
            R.drawable.sticker_c_03_resize, R.drawable.sticker_c_04_resize,
            R.drawable.sticker_c_05_resize, R.drawable.sticker_c_06_resize,
            R.drawable.sticker_c_07_resize, R.drawable.sticker_c_08_resize,
            R.drawable.sticker_c_09_resize, R.drawable.sticker_c_10_resize,
            R.drawable.sticker_c_11_resize, R.drawable.sticker_c_12_resize,
            R.drawable.sticker_c_13_resize, R.drawable.sticker_c_14_resize,
            R.drawable.sticker_d_01_resize, R.drawable.sticker_d_02_resize,
            R.drawable.sticker_d_03_resize, R.drawable.sticker_d_04_resize,
            R.drawable.sticker_d_05_resize, R.drawable.sticker_d_06_resize,
            R.drawable.sticker_d_07_resize, R.drawable.sticker_d_08_resize,
            R.drawable.sticker_d_09_resize, R.drawable.sticker_d_10_resize,
            R.drawable.sticker_e_01_resize, R.drawable.sticker_e_02_resize,
            R.drawable.sticker_e_03_resize, R.drawable.sticker_e_04_resize,
            R.drawable.sticker_e_05_resize, R.drawable.sticker_e_06_resize,
            R.drawable.sticker_e_07_resize, R.drawable.sticker_e_08_resize};

//	private int[] mStickers = {R.drawable.sticker_e_01, R.drawable.sticker_e_02,
//			R.drawable.sticker_e_03, R.drawable.sticker_e_04,
//			R.drawable.sticker_e_05, R.drawable.sticker_e_06,
//			R.drawable.sticker_e_07, R.drawable.sticker_e_08};
//	
//	private int[] mResizeStickers = {R.drawable.sticker_e_01_resize, R.drawable.sticker_e_02_resize,
//			R.drawable.sticker_e_03_resize, R.drawable.sticker_e_04_resize,
//			R.drawable.sticker_e_05_resize, R.drawable.sticker_e_06_resize,
//			R.drawable.sticker_e_07_resize, R.drawable.sticker_e_08_resize};

    private EditPhotoItemAdapter StickerAdapter;
    //private StickerWidget Sw;

    private boolean IsSelectItemFlag = false;

    private int mStickerCounter = 0;
    private int ScreenWidth = 0;

    private final static int StickerMaxAmount = 5;
    //private final static int StickerLoadNum = 20;
    private static int SelectItemWidth = 200;
    private static int SelectItemHeight = 200;

    RelativeLayout.LayoutParams rlp;

    private ArrayList<LoadingImageFromResTask> LoadingImageFromResTaskArray = new ArrayList<LoadingImageFromResTask>();

    @SuppressLint("ValidFragment")
    public EditPhotoStickerFragment(ImageView photoview, FrameLayout showphotolayout) {

        this.mPhotoView = photoview;
        this.mShowPhotoLayout = showphotolayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mStickerCounter = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mAct = getActivity();

        Bundle bundle = new Bundle();
        bundle = getArguments();
        ScreenWidth = bundle.getInt("screenwidth", 0);
        SetResolutionParameter();


        mPhotoView.setOnClickListener(clicklistener);


        rlp = new RelativeLayout.LayoutParams(SelectItemWidth, SelectItemHeight);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);

        if (NoResizeStickersList.size() == 0 && ResizeStickersList.size() == 0) {

            for (int res : mResizeStickers) {
                LoadingImageFromResTask task = new LoadingImageFromResTask();
                task.execute(res);
                LoadingImageFromResTaskArray.add(task);
            }

            //LoadStickerFromRes(0);

            StickerAdapter = new EditPhotoItemAdapter(mAct, ResizeStickersList, rlp);
            mStickerListView = new ListView(mAct);
            mStickerListView.setVerticalScrollBarEnabled(false);
            mStickerListView.setAdapter(StickerAdapter);
            mStickerListView.setSelector(R.drawable.photo_item_select_frame);
            mStickerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mStickerListView.setItemChecked(0, true);
            mStickerListView.setOnItemClickListener(itemclicklistener);

        }

        return mStickerListView;
    }

    private void SetResolutionParameter() {
        LOG.I(TAG, "screen width = " + ScreenWidth);
        switch (ScreenWidth) {
            case 1920:
                SelectItemWidth = 250;
                SelectItemHeight = 250;
                break;
            case 1280:
                SelectItemWidth = 150;
                SelectItemHeight = 150;
                break;
            case 1600:
                SelectItemWidth = 200;
                SelectItemHeight = 200;
                break;
            case 1024:
                SelectItemWidth = 160;
                SelectItemHeight = 160;
                break;
            case 800:
                SelectItemWidth = 100;
                SelectItemHeight = 100;
                break;
            default:
                break;
        }
    }

    private OnClickListener clicklistener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            if (mStickerList != null) {
                LOG.I(TAG, "sticker list size = " + mStickerList.size());
                for (StickerWidget widget : mStickerList)
                    widget.hideControl();

            }
        }
    };

    private OnItemClickListener itemclicklistener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            if (IsSelectItemFlag && mStickerList.size() < StickerMaxAmount) {

                //StickerBitmap = NoResizeStickersList.get(position);
                StickerBitmap = BitmapFactory.decodeResource(getResources(), mStickers[position]);

                addSticker(StickerBitmap);

            }
        }
    };

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        LOG.E(TAG, "onDestroyView");
        for (LoadingImageFromResTask task : LoadingImageFromResTaskArray)
            task.cancel(true);

        ResizeStickersList.clear();
        NoResizeStickersList.clear();
    }

	/*private void LoadStickerFromRes(int initnum){

		if((StickerLoadNum + initnum) > mResizeStickers.length){
			
			for (int i = initnum; i < mStickers.length; i++) {
				LOG.V(TAG, "sticker loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), mStickers[i]);
				NoResizeStickersList.add(bmp);
			}
			
			for (int i = initnum; i < mResizeStickers.length; i++) {
				LOG.V(TAG, "sticker item loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), mResizeStickers[i]);
				ResizeStickersList.add(bmp);
			}			
			
		}else{
			
			for (int i = initnum; i < (StickerLoadNum + initnum); i++) {
				LOG.V(TAG, "sticker loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), mStickers[i]);
				NoResizeStickersList.add(bmp);
			}

			for (int i = initnum; i < (StickerLoadNum + initnum); i++) {
				LOG.V(TAG, "sticker item loading " + i);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), mResizeStickers[i]);
				ResizeStickersList.add(bmp);
			}
		}
	}*/

/*	private Bitmap resizeBitmap(Bitmap bmp, int width, int height) {

		Matrix matrix = new Matrix();
		//BitmapFactory.Options options = new BitmapFactory.Options();
		//options.inSampleSize = 2;
		int oWidth = bmp.getWidth();
		int oHeight = bmp.getHeight();
		//LOG.I("NabiPhoto", "original width & height:" + oWidth + "," + oHeight);
		
		float scaleWidth = (float) width / oWidth;
		float scaleHeight = (float) height / oHeight;
		//LOG.I("nabiPhoto", "resizeBitmap scale:" + scaleWidth + "," + scaleHeight);
		
		matrix.postScale(scaleWidth, scaleHeight);
		bmp = Bitmap.createBitmap(bmp, 0, 0, oWidth, oHeight, matrix, true);
		//LOG.I("NabiPhoto", "resized width & height:" + oWidth + "," + oHeight);
		
		return bmp;
	}
*/

    private class LoadingImageFromResTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            int res = (int) arg0[0];

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            ResizeStickersList.add(result);
            StickerAdapter.notifyDataSetChanged();
        }

    }

    private StickerButtonListener stickerlistener = new StickerButtonListener() {

        @Override
        public void onClick(StickerWidget sw) {
            // TODO Auto-generated method stub
            mShowPhotoLayout.removeView(sw);
            mStickerList.remove(sw);
            //LOG.I(TAG, "sw id = " + sw);
        }

        @Override
        public void onGainFocus(int index) {
            // TODO Auto-generated method stub
            LOG.W(TAG, "stikcer onGainForcus and index = " + index);
            for (StickerWidget widget : mStickerList) {
                if (widget.getIndex() != index) {
                    widget.hideControl();
                }

				/*if(widget.getIndex() == index)
                    widget.bringToFront();*/
            }

        }
    };

    private void addSticker(Bitmap bitmap) {

        if (mStickerList == null) {
            mStickerList = new ArrayList<StickerWidget>();
            // LOG.I(TAG, "new sticker list");
        }

        HideAllStickerControl();

        // add sticker

        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);

        StickerWidget sw = new StickerWidget(mAct, bitmap, stickerlistener);
        mShowPhotoLayout.addView(sw, flp);
        mStickerList.add(sw);

        // add index for each sticker
        sw.setIndex(mStickerCounter);
        mStickerCounter++;

    }

    public void SetSelectItme(boolean flag) {

        this.IsSelectItemFlag = flag;
        SetStickerEditable(flag);

    }

    public void SetStickerEditable(boolean flag) {

        if (mStickerList != null) {
            LOG.I(TAG, "sticker editable = " + mStickerList.size());
            for (StickerWidget widget : mStickerList)
                widget.setEditable(flag);

        }

    }

    public void HideAllStickerControl() {

        if (mStickerList != null) {
            LOG.I(TAG, "sticker hidecontrol = " + mStickerList.size());
            for (StickerWidget widget : mStickerList)
                widget.hideControl();

        }
    }

}
