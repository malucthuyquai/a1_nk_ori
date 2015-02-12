package com.fuhu.nabiconnect.photo.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
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
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.adapter.EditPhotoItemAdapter;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class EditPhotoFrameLandFragment extends Fragment {

    public static final String TAG = "EditPhotoFrameLandFragment";
    private Activity mAct;

    private int mNowPhotoWidth;
    private int mNowPhotoHeight;
    private int ScreenWidth = 0;
    private static int SelectItemWidth = 200;
    private static int SelectItemHeight = 160;
    private final static int FrameLoadNum = 20;

    private boolean IsSelectItemFlag = false;

    private FrameLayout mShowPhotoLayout;

    private Bitmap bitmap;
    private Bitmap tempFrameBitmap;

    private ImageView mFrameView;

    //private ArrayList<Bitmap> ResizeFramesList;
    private ArrayList<Bitmap> SelectItemFramesList = new ArrayList<Bitmap>();
    private ArrayList<LoadingImageFromResTask> LoadingImageFromResTaskArray = new ArrayList<LoadingImageFromResTask>();

    private ListView mFrameListView;

    private int[] FramePictures = {R.drawable.photo_nabiphoto_framecancel,
            R.drawable.photo_nabiphoto_frames01,
            R.drawable.photo_nabiphoto_frames02,
            R.drawable.photo_nabiphoto_frames03,
            R.drawable.photo_nabiphoto_frames04,
            R.drawable.photo_nabiphoto_frames05,
            R.drawable.photo_nabiphoto_frames06,
            R.drawable.photo_nabiphoto_frames07};

    private int[] ResizeFramePictures = {
            R.drawable.photo_nabiphoto_resize_framecancel,
            R.drawable.photo_nabiphoto_resize_frames01,
            R.drawable.photo_nabiphoto_resize_frames02,
            R.drawable.photo_nabiphoto_resize_frames03,
            R.drawable.photo_nabiphoto_resize_frames04,
            R.drawable.photo_nabiphoto_resize_frames05,
            R.drawable.photo_nabiphoto_resize_frames06,
            R.drawable.photo_nabiphoto_resize_frames07};

    private EditPhotoItemAdapter FrameAdapter;

    RelativeLayout.LayoutParams rlp;

    public void SetEditPhotoFrameLandFragment(int photoW, int photoH, FrameLayout showphotolayout, ImageView mFrameView) {

        this.mNowPhotoWidth = photoW;
        this.mNowPhotoHeight = photoH;
        this.mShowPhotoLayout = showphotolayout;
        this.mFrameView = mFrameView;
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


        rlp = new RelativeLayout.LayoutParams(SelectItemWidth, SelectItemHeight);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);

        //LOG.I(TAG, "mNowPhotoWidth = "+ mNowPhotoWidth+" mNowPhotoHeight = " +mNowPhotoHeight);
        //bitmap = Bitmap.createBitmap(mNowPhotoWidth, mNowPhotoHeight, Config.ARGB_8888);

        for (int res : ResizeFramePictures) {
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
        LOG.I(TAG, "screen width = " + ScreenWidth);
        switch (ScreenWidth) {
            case 1920:
                SelectItemWidth = 252;
                SelectItemHeight = 189;
                break;
            case 1280:
                SelectItemWidth = 160;
                SelectItemHeight = 120;
                break;
            case 1600:
                SelectItemWidth = 200;
                SelectItemHeight = 150;
                break;
            case 1024:
                SelectItemWidth = 160;
                SelectItemHeight = 120;
                break;
            case 800:
                SelectItemWidth = 100;
                SelectItemHeight = 75;
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        for (LoadingImageFromResTask task : LoadingImageFromResTaskArray)
            task.cancel(true);
        SelectItemFramesList.clear();

    }

    private OnItemClickListener itemclicklistener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub

            LOG.I(TAG, "item position = " + position);
            if (IsSelectItemFlag) {

                if (position == 0) {
                    // mFrameListView.setItemChecked(0, false);
                    mShowPhotoLayout.removeView(mFrameView);

                } else {
                    mShowPhotoLayout.removeView(mFrameView);

                    bitmap = Bitmap.createBitmap(mNowPhotoWidth, mNowPhotoHeight, Config.ARGB_8888);
                    tempFrameBitmap = combineLayer(bitmap, FramePictures[position]);
                    // tempFrameBitmap = combineLayer(bitmap, ResizeFramesList.get(position));

                    LOG.I(TAG, " Frame mNowPhotoWidth = " + mNowPhotoWidth + "mNowPhotoHeight = " + mNowPhotoHeight);
                    // mFrameView.setImageBitmap(ResizeFramesList.get(position));
                    mFrameView.setImageBitmap(tempFrameBitmap);
                    mShowPhotoLayout.addView(mFrameView);
                }
            }

        }

    };

    private OnScrollListener scrollListener = new OnScrollListener() {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            // TODO Auto-generated method stub


            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                //IsLastRow  = true ;

                LoadFramesFromRes(totalItemCount);

                if (totalItemCount < ResizeFramePictures.length) {

                    FrameAdapter = new EditPhotoItemAdapter(mAct, SelectItemFramesList, rlp);
                    mFrameListView.setAdapter(FrameAdapter);
                    mFrameListView.setSelection(firstVisibleItem);
                }

                LOG.E(TAG, "visibleItemCount = " + visibleItemCount);
                LOG.E(TAG, "firstVisibleItem = " + firstVisibleItem);
                LOG.E(TAG, " totalItemCount = " + totalItemCount);
            }


        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // TODO Auto-generated method stub

        }
    };

    private void LoadFramesFromRes(int initnum) {

        if ((FrameLoadNum + initnum) > ResizeFramePictures.length) {

            for (int i = initnum; i < ResizeFramePictures.length; i++) {
                LOG.V(TAG, "sticker item loading " + i);
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), ResizeFramePictures[i]);
                SelectItemFramesList.add(bmp);
            }

        } else {

            for (int i = initnum; i < (FrameLoadNum + initnum); i++) {
                LOG.V(TAG, "sticker item loading " + i);
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), ResizeFramePictures[i]);
                SelectItemFramesList.add(bmp);
            }
        }
    }

    private Bitmap combineLayer(Bitmap bmp, int res) {

        LOG.I(TAG, " Frame mNowPhotoWidth = " + bmp.getWidth() + "mNowPhotoHeight = " + bmp.getHeight());
        //Bitmap bitmap = null;
        Bitmap overlayer = BitmapFactory.decodeResource(mAct.getResources(), res);
        //Bitmap overlayer = BitmapFactory.decodeResource(mAct.getResources(), res, Options);

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

        overlayer.recycle();

        return bmp;
    }

    public void SetSelectItme(boolean flag) {

        this.IsSelectItemFlag = flag;

    }

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
            SelectItemFramesList.add(result);
            FrameAdapter.notifyDataSetChanged();
        }

    }

}
