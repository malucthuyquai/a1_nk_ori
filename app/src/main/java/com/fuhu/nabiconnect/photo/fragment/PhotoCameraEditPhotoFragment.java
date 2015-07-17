package com.fuhu.nabiconnect.photo.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.data.FriendData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.dialog.ChooseContactDialog;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.photo.util.Exif;
import com.fuhu.nabiconnect.photo.util.ExifInterface;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;
import com.fuhu.nabiconnect.photo.widget.PhotoSendingAnimationDialog;
import com.fuhu.nabiconnect.photo.widget.PhotoSentFailedDialog;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.ndnslibsoutstructs.friends_outObj;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMSenderEventCallback;

import java.util.ArrayList;

public class PhotoCameraEditPhotoFragment extends PhotoBaseFragment {
    public static final String TAG = "PhotoCameraEditPhotoFragment";

    private View rootView;

    private FragmentManager mFragmentManager = null;
    //private PageControllerListener mPageControllerListener = null;
    private ChooseContactDialog m_ChooseContactDialog;

    private ImageButton mFrameButton;
    private ImageButton mFrameVerticalButton;
    private ImageButton mStickerButton;
    private ImageButton mMoveButton;
    private ImageButton mMailButton;

    private EditPhotoFramePortFragment mFrameVerticalPage;
    private EditPhotoFrameLandFragment mFramePage;
    private EditPhotoStickerFragment mStickerPage;

    private Bitmap photoBitmap;

    private ImageView mFrameView;
    private ImageView mPhotoView;

    private Point PhotoBoundsInfo = new Point(); //x = width, y = height
    private FrameLayout mShowPhotoLayout;

    private Matrix photoMatrix = new Matrix();

    private String photoPath;
    private String UserId;
    private String UserName;

    private static final int FRAMEMODE = 11;
    private static final int FRAMEVERTICALMODE = 12;
    private static final int STICKERMODE = 13;
    private static final int PHOTOMOVEMODE = 14;
    private static final int PHOTOROTATEMODE = 15;
    private static final float PhotoThumbnailWidth = 320f;
    private static final float PhotoThumbnailHeight = 240f;

    private int mNowPhotoWidth;
    private int mNowPhotoHeight;
    private int NOWMODE = 0;
    private int PhotoExifOrientation = 0;

    ArrayList<String> idList = new ArrayList<String>();
    private ArrayList<FriendData> m_CurrentSelectedFriendDataList;

    Bundle bundle = new Bundle();

    private PhotoSendingAnimationDialog m_PhotoSendingAnimationDialog;

    @Override
    public String getTrack() {
        return "camera_edit_photo";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Bundle bundle = getArguments();
        photoPath = bundle.getString(PhotoParameter.PHOTOPATH);
        UserId = mAct.getUserId();
        UserName = mAct.getUserName();
        rootView = inflater.inflate(R.layout.photo_cameraeditphoto_fragment_layout, null);
        findView();
        photoMatrix.reset();

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
        LOG.I(TAG, "Max memory is " + maxMemory + "MB");

        mFragmentManager = getFragmentManager();

        ExifInterface exif = Exif.getExif(photoPath);
        if (exif != null)
            PhotoExifOrientation = Exif.getOrientation(exif);
        LOG.W(TAG, "photo exif = " + PhotoExifOrientation);

        try {
            BitmapFactory.Options options = GetBitmapOptions();
            photoBitmap = BitmapFactory.decodeFile(photoPath, options);
            LOG.I(TAG, "original size = " + photoBitmap.getWidth() + "," + photoBitmap.getHeight());
        } catch (OutOfMemoryError e) {
            LOG.I(TAG, "OOM !!!!!!");
        }

        bundle.putInt("screenwidth", ScreenWidth);

        SetPhotoViewAndResizePhoto();

        mStickerPage = new EditPhotoStickerFragment(mPhotoView, mShowPhotoLayout);
        mStickerPage.setArguments(bundle);

        return rootView;
    }

    private void findView() {
        mShowPhotoLayout = (FrameLayout) rootView.findViewById(R.id.ShowPhotoLayout);
        mPhotoView = (ImageView) rootView.findViewById(R.id.PhotoView);

        mFrameButton = (ImageButton) rootView.findViewById(R.id.Edit_Photo_Frame);
        mFrameVerticalButton = (ImageButton) rootView.findViewById(R.id.Edit_Photo_Frame_Vertical);
        mStickerButton = (ImageButton) rootView.findViewById(R.id.Edit_Photo_Sticker);
        mMoveButton = (ImageButton) rootView.findViewById(R.id.Edit_Photo_Move);
        mMailButton = (ImageButton) rootView.findViewById(R.id.Edit_Photo_Mail);

        mFrameButton.setOnClickListener(clickListener);
        mFrameVerticalButton.setOnClickListener(clickListener);
        mStickerButton.setOnClickListener(clickListener);
        mMoveButton.setOnClickListener(clickListener);
        mMailButton.setOnClickListener(clickListener);

        mFrameView = new ImageView(mAct);
    }

    private void SetPhotoViewAndResizePhoto() {

        FrameLayout.LayoutParams flp = null;
        RelativeLayout.LayoutParams rlp = null;
        Bundle bundle = new Bundle();
        bundle.putInt("screenwidth", ScreenWidth);

        LOG.I(TAG, "Screen Width Size:" + ScreenWidth);
        LOG.I(TAG, "Screen Height Size:" + ScreenHeight);

        if (ScreenWidth == 1920) {      // LG 1920x1114 dreamtap 1920x1200
            LOG.I(TAG, "photoBitmap.getWidth() = " + photoBitmap.getWidth() + " photoBitmap.getHeight() = " + photoBitmap.getHeight());

            if (photoBitmap.getWidth() < photoBitmap.getHeight() || (PhotoExifOrientation % 180) == 90) {  //vertical photo

                float scaleWidth;

                if (ScreenHeight == 1008) {

                    if (photoBitmap.getWidth() < photoBitmap.getHeight() && PhotoExifOrientation == 0)
                        scaleWidth = (float) 1008 / photoBitmap.getHeight();
                    else
                        scaleWidth = (float) 1008 / photoBitmap.getWidth();

                } else {

                    if (photoBitmap.getWidth() < photoBitmap.getHeight() && PhotoExifOrientation == 0)
                        scaleWidth = (float) 1116 / photoBitmap.getHeight();
                    else
                        scaleWidth = (float) 1116 / photoBitmap.getWidth();

                }
                LOG.I(TAG, "scale " + scaleWidth);

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);
                matrix.postRotate(PhotoExifOrientation);
                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();

                LOG.I(TAG, " layout vertical photo height = " + mNowPhotoHeight);
                LOG.I(TAG, " layout vertical photo width = " + mNowPhotoWidth);

                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);
                rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);


                mFrameVerticalButton.setSelected(true);
                mFrameButton.setVisibility(View.GONE);

                mFrameVerticalPage = new EditPhotoFramePortFragment();
                mFrameVerticalPage.SetEditPhotoFramePortFragmentPrameter(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFrameVerticalPage.SetSelectItme(true);
                mFrameVerticalPage.setArguments(bundle);
                addFragment(mFrameVerticalPage, "FrameVerticalPage");

            } else {  // land photo

                float scaleWidth = (float) 1116 / photoBitmap.getWidth();
                // float scaleHeight = (float) height / photoBitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);

                if (PhotoExifOrientation == 180)
                    matrix.postRotate(PhotoExifOrientation);

                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();
                LOG.I(TAG, "scaleWidth " + scaleWidth);
                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);

                mFrameButton.setSelected(true);
                mFrameVerticalButton.setVisibility(View.GONE);

                mFramePage = new EditPhotoFrameLandFragment();
                mFramePage.SetEditPhotoFrameLandFragment(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFramePage.SetSelectItme(true);
                mFramePage.setArguments(bundle);
                addFragment(mFramePage, "StickerPage");

            }

        } else if (ScreenWidth == 1024) {    // nabi2 1024x552

            LOG.I(TAG, "photoBitmap.getWidth() = " + photoBitmap.getWidth() + " photoBitmap.getHeight() = " + photoBitmap.getHeight());

            if (photoBitmap.getWidth() < photoBitmap.getHeight() || (PhotoExifOrientation % 180) == 90) {  //vertical photo

                float scaleWidth;

                if (photoBitmap.getWidth() < photoBitmap.getHeight() && PhotoExifOrientation == 0)
                    scaleWidth = (float) 552 / photoBitmap.getHeight();
                else
                    scaleWidth = (float) 552 / photoBitmap.getWidth();
                LOG.I(TAG, "scale " + scaleWidth);

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);
                matrix.postRotate(PhotoExifOrientation);
                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();

                LOG.I(TAG, " layout vertical photo height = " + mNowPhotoHeight);
                LOG.I(TAG, " layout vertical photo width = " + mNowPhotoWidth);

                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);
                rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);

                mFrameVerticalButton.setSelected(true);
                mFrameButton.setVisibility(View.GONE);

                mFrameVerticalPage = new EditPhotoFramePortFragment();
                mFrameVerticalPage.SetEditPhotoFramePortFragmentPrameter(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFrameVerticalPage.SetSelectItme(true);
                mFrameVerticalPage.setArguments(bundle);
                addFragment(mFrameVerticalPage, "FrameVerticalPage");


            } else {  // land photo

                float scaleWidth = (float) 594 / photoBitmap.getWidth();

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);

                if (PhotoExifOrientation == 180)
                    matrix.postRotate(PhotoExifOrientation);

                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();
                LOG.I(TAG, "scaleWidth " + scaleWidth);
                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);

                mFrameButton.setSelected(true);
                mFrameVerticalButton.setVisibility(View.GONE);

                mFramePage = new EditPhotoFrameLandFragment();
                mFramePage.SetEditPhotoFrameLandFragment(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFramePage.SetSelectItme(true);
                mFramePage.setArguments(bundle);
                addFragment(mFramePage, "StickerPage");

            }


        } else if (ScreenWidth == 1280) {

            LOG.I(TAG, "photoBitmap.getWidth() = " + photoBitmap.getWidth() + " photoBitmap.getHeight() = " + photoBitmap.getHeight());

            if (photoBitmap.getWidth() < photoBitmap.getHeight() || (PhotoExifOrientation % 180) == 90) {  //vertical photo

                float scaleWidth;

                if (photoBitmap.getWidth() < photoBitmap.getHeight() && PhotoExifOrientation == 0)
                    scaleWidth = (float) 728 / photoBitmap.getHeight();
                else
                    scaleWidth = (float) 728 / photoBitmap.getWidth();
                LOG.I(TAG, "scale " + scaleWidth);

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);
                matrix.postRotate(PhotoExifOrientation);
                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();

                LOG.I(TAG, " layout vertical photo height = " + mNowPhotoHeight);
                LOG.I(TAG, " layout vertical photo width = " + mNowPhotoWidth);

                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);
                rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);

                mFrameVerticalButton.setSelected(true);
                mFrameButton.setVisibility(View.GONE);

                mFrameVerticalPage = new EditPhotoFramePortFragment();
                mFrameVerticalPage.SetEditPhotoFramePortFragmentPrameter(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFrameVerticalPage.SetSelectItme(true);
                mFrameVerticalPage.setArguments(bundle);
                addFragment(mFrameVerticalPage, "FrameVerticalPage");


            } else {  // land photo

                float scaleWidth = (float) 745 / photoBitmap.getWidth();

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);

                if (PhotoExifOrientation == 180)
                    matrix.postRotate(PhotoExifOrientation);

                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();
                LOG.I(TAG, "scaleWidth " + scaleWidth);
                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);

                mFrameButton.setSelected(true);
                mFrameVerticalButton.setVisibility(View.GONE);

                mFramePage = new EditPhotoFrameLandFragment();
                mFramePage.SetEditPhotoFrameLandFragment(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFramePage.SetSelectItme(true);
                mFramePage.setArguments(bundle);
                addFragment(mFramePage, "StickerPage");

            }

        } else if (ScreenWidth == 1600) {

            LOG.I(TAG, "photoBitmap.getWidth() = " + photoBitmap.getWidth() + " photoBitmap.getHeight() = " + photoBitmap.getHeight());

            if (photoBitmap.getWidth() < photoBitmap.getHeight() || (PhotoExifOrientation % 180) == 90) {  //vertical photo

                float scaleWidth;

                if (photoBitmap.getWidth() < photoBitmap.getHeight() && PhotoExifOrientation == 0)
                    scaleWidth = (float) 828 / photoBitmap.getHeight();
                else
                    scaleWidth = (float) 828 / photoBitmap.getWidth();
                LOG.I(TAG, "scale " + scaleWidth);

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);
                matrix.postRotate(PhotoExifOrientation);
                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();

                LOG.I(TAG, " layout vertical photo height = " + mNowPhotoHeight);
                LOG.I(TAG, " layout vertical photo width = " + mNowPhotoWidth);

                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);
                rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);

                mFrameVerticalButton.setSelected(true);
                mFrameButton.setVisibility(View.GONE);

                mFrameVerticalPage = new EditPhotoFramePortFragment();
                mFrameVerticalPage.SetEditPhotoFramePortFragmentPrameter(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFrameVerticalPage.SetSelectItme(true);
                mFrameVerticalPage.setArguments(bundle);
                addFragment(mFrameVerticalPage, "FrameVerticalPage");


            } else {  // land photo

                float scaleWidth = (float) 930 / photoBitmap.getWidth();
                // float scaleHeight = (float) height / photoBitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);

                if (PhotoExifOrientation == 180)
                    matrix.postRotate(PhotoExifOrientation);

                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();
                LOG.I(TAG, "scaleWidth " + scaleWidth);
                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);

                mFrameButton.setSelected(true);
                mFrameVerticalButton.setVisibility(View.GONE);

                mFramePage = new EditPhotoFrameLandFragment();
                mFramePage.SetEditPhotoFrameLandFragment(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFramePage.SetSelectItme(true);
                mFramePage.setArguments(bundle);
                addFragment(mFramePage, "StickerPage");

            }

        } else if (ScreenWidth == 800) {

            LOG.I(TAG, "photoBitmap.getWidth() = " + photoBitmap.getWidth() + " photoBitmap.getHeight() = " + photoBitmap.getHeight());

            if (photoBitmap.getWidth() < photoBitmap.getHeight() || (PhotoExifOrientation % 180) == 90) {  //vertical photo

                float scaleWidth;

                if (photoBitmap.getWidth() < photoBitmap.getHeight() && PhotoExifOrientation == 0)
                    scaleWidth = (float) 424 / photoBitmap.getHeight();
                else
                    scaleWidth = (float) 424 / photoBitmap.getWidth();
                LOG.I(TAG, "scale " + scaleWidth);

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);
                matrix.postRotate(PhotoExifOrientation);
                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();

                LOG.I(TAG, " layout vertical photo height = " + mNowPhotoHeight);
                LOG.I(TAG, " layout vertical photo width = " + mNowPhotoWidth);

                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);
                rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);

                mFrameVerticalButton.setSelected(true);
                mFrameButton.setVisibility(View.GONE);

                mFrameVerticalPage = new EditPhotoFramePortFragment();
                mFrameVerticalPage.SetEditPhotoFramePortFragmentPrameter(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFrameVerticalPage.SetSelectItme(true);
                mFrameVerticalPage.setArguments(bundle);
                addFragment(mFrameVerticalPage, "FrameVerticalPage");


            } else {  // land photo

                float scaleWidth = (float) 464 / photoBitmap.getWidth();
                // float scaleHeight = (float) height / photoBitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleWidth);

                if (PhotoExifOrientation == 180)
                    matrix.postRotate(PhotoExifOrientation);

                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), matrix, true);

                mNowPhotoWidth = photoBitmap.getWidth();
                mNowPhotoHeight = photoBitmap.getHeight();
                LOG.I(TAG, "scaleWidth " + scaleWidth);
                rlp = new RelativeLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);

                mFrameButton.setSelected(true);
                mFrameVerticalButton.setVisibility(View.GONE);

                mFramePage = new EditPhotoFrameLandFragment();
                mFramePage.SetEditPhotoFrameLandFragment(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                mFramePage.SetSelectItme(true);
                mFramePage.setArguments(bundle);
                addFragment(mFramePage, "StickerPage");

            }

        }

        PhotoBoundsInfo.set(mNowPhotoWidth, mNowPhotoHeight);  // for calculate photo scale max value and min value

        LOG.I(TAG, "nowPhotoWidth,nowPhotoHeight = " + mNowPhotoWidth + "," + mNowPhotoHeight);
        mShowPhotoLayout.setLayoutParams(rlp);

        flp = new FrameLayout.LayoutParams(mNowPhotoWidth, mNowPhotoHeight);
        mPhotoView.setLayoutParams(flp);
        mPhotoView.setImageBitmap(photoBitmap);
        //mShowPhotoLayout.addView(mPhotoView, flp);
        //mPhotoView.setScaleType(ScaleType.MATRIX);
        mPhotoView.setOnTouchListener(phototouchListener);

    }

    private void addFragment(Fragment f, String tag) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.RightFrame, f, tag);
        transaction.commit();
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();
            bundle.putInt("screenwidth", ScreenWidth);

            switch (v.getId()) {

                case R.id.Edit_Photo_Frame:

                    if (mStickerPage != null)
                        mStickerPage.HideAllStickerControl();


                    mStickerPage.SetSelectItme(false);

                    NOWMODE = FRAMEMODE;


                    mFrameButton.setSelected(true);
                    mStickerButton.setSelected(false);
                    mMoveButton.setSelected(false);
                    mMailButton.setSelected(false);

                    if (mFramePage == null) {
                        mFramePage = new EditPhotoFrameLandFragment();
                        mFramePage.SetEditPhotoFrameLandFragment(mNowPhotoWidth,
                                mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                        mFramePage.SetSelectItme(true);
                        mFramePage.setArguments(bundle);
                    } else {

                        if (mFramePage.isAdded())
                            return;

                        mFramePage.SetSelectItme(true);
                        mFramePage.setArguments(bundle);
                    }

                    addFragment(mFramePage, "FramePage");

                    //tracking
                    Tracking.pushTrack(getActivity(), "add_horizontal_frames");

                    break;

                case R.id.Edit_Photo_Frame_Vertical:

                    if (mStickerPage != null)
                        mStickerPage.HideAllStickerControl();

                    mStickerPage.SetSelectItme(false);


                    NOWMODE = FRAMEVERTICALMODE;

                    mFrameVerticalButton.setSelected(true);
                    mStickerButton.setSelected(false);
                    mMoveButton.setSelected(false);
                    mMailButton.setSelected(false);


                    if (mFrameVerticalPage == null) {
                        mFrameVerticalPage = new EditPhotoFramePortFragment();
                        mFrameVerticalPage.SetEditPhotoFramePortFragmentPrameter(mNowPhotoWidth, mNowPhotoHeight, mShowPhotoLayout, mFrameView);
                        mFrameVerticalPage.SetSelectItme(true);
                        mFrameVerticalPage.setArguments(bundle);
                    } else {

                        if (mFrameVerticalPage.isAdded())
                            return;

                        mFrameVerticalPage.SetSelectItme(true);
                        mFrameVerticalPage.setArguments(bundle);
                    }

                    addFragment(mFrameVerticalPage, "FrameVerticalPage");

                    //tracking
                    Tracking.pushTrack(getActivity(), "add_vertical_frames");

                    break;


                case R.id.Edit_Photo_Sticker:

                    NOWMODE = STICKERMODE;
                    mStickerPage.SetSelectItme(true);

                    mFrameButton.setSelected(false);
                    mFrameVerticalButton.setSelected(false);
                    mStickerButton.setSelected(true);
                    mMoveButton.setSelected(false);
                    mMailButton.setSelected(false);


                    if (mStickerPage.isAdded())
                        return;
                    addFragment(mStickerPage, "StickerPage");

                    //tracking
                    Tracking.pushTrack(getActivity(), "add_character");

                    break;
                case R.id.Edit_Photo_Move:


                    NOWMODE = PHOTOMOVEMODE;

                    if (mStickerPage != null) {
                        mStickerPage.HideAllStickerControl();
                        mStickerPage.SetSelectItme(false);
                    }

                    mFrameButton.setSelected(false);
                    mFrameVerticalButton.setSelected(false);
                    mStickerButton.setSelected(false);
                    mMoveButton.setSelected(true);
                    mMailButton.setSelected(false);

                    Fragment EmptyPage = new Fragment();
                    addFragment(EmptyPage, "EmptyPage");

                    //tracking
                    Tracking.pushTrack(getActivity(), "move_photo");

                    break;
                case R.id.Edit_Photo_Mail:

                    if (mStickerPage != null) {
                        mStickerPage.HideAllStickerControl();
                        mStickerPage.SetSelectItme(false);
                    }

                    mFrameButton.setSelected(false);
                    mFrameVerticalButton.setSelected(false);
                    mStickerButton.setSelected(false);
                    mMoveButton.setSelected(false);
                    mMailButton.setSelected(true);

                    mMailButton.setEnabled(false);
                    mAct.SetLastButtonEnable(false);
                    EmptyPage = new Fragment();
                    addFragment(EmptyPage, "EmptyPage");
                    mAct.CheckWifi();
                    mAct.getFriendList(UserId);
                    //CheckMode(); //check and login function

                    //tracking
                    Tracking.pushTrack(getActivity(), "send_photo");

                    break;
                default:
                    break;

            }
        }
    };

    private ImageView.OnTouchListener phototouchListener = new ImageView.OnTouchListener() {

        private Matrix matrix = photoMatrix;
        private Matrix savedMatrix = new Matrix();
        float oldRotation = 0;

        private static final int NONE = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private static final int ROTATE = 3;
        private static final int ZOOM_OR_ROTATE = 4;
        private int mode = NONE;

        private boolean zoom = false;
        private boolean IsPhotoScaleMax = false;
        private boolean IsPhotoScaleMin = false;

        private PointF prev = new PointF();
        private PointF mid = new PointF();
        private Point savephotoinfo = new Point();   //recode photo width and height information , x=width, y=height
        private float dist = 1f;

        float ScaleMax = 1.5f
                ,
                ScaleMin = 0.75f;  //set parameter photo scale max and min number
        int photoMax
                ,
                photoMin;
        float tScale = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (NOWMODE == STICKERMODE)
                return false;

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    prev.set(event.getX(), event.getY());   //save coordinate

                    mode = DRAG;

                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    dist = spacing(event);
                    oldRotation = rotation(event);

                    if (spacing(event) > 10f) {

                        savedMatrix.set(matrix);
                        savephotoinfo.set(PhotoBoundsInfo.x, PhotoBoundsInfo.y);
                        midPoint(mid, event);
                        mode = ZOOM;
                        zoom = true;
                    }

                    if (PhotoBoundsInfo.x < photoMin) {
                        mode = ROTATE;
                        IsPhotoScaleMin = true;
                        //Log.e(TAG, " photoMin");
                    } else if (PhotoBoundsInfo.x > photoMax) {
                        mode = ROTATE;
                        IsPhotoScaleMax = true;
                        //Log.e(TAG, " photoMax");
                    }


                    LOG.I(TAG, "down mode = " + mode);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    zoom = false;

                    break;
                case MotionEvent.ACTION_MOVE:

                    if (NOWMODE == PHOTOMOVEMODE) {

                        photoMax = (int) (mNowPhotoWidth * ScaleMax);  //calculate photo scale max value
                        photoMin = (int) (mNowPhotoWidth * ScaleMin);  //calculate photo scale min value

                        if (PhotoBoundsInfo.x >= photoMin && PhotoBoundsInfo.x <= photoMax)
                            zoom = true;
                        else
                            zoom = false;

                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - prev.x, event.getY() - prev.y);
                            //LOG.I(TAG, "photo draging");
                        } else if (mode == ZOOM) {

                            float rotation = rotation(event) - oldRotation;
                            float newDist = spacing(event);

                            if (newDist > 10f) {
                                matrix.set(savedMatrix);

                                if (zoom) {
                                    tScale = newDist / dist;
                                    PhotoBoundsInfo.set(savephotoinfo.x, savephotoinfo.y);
                                    PhotoBoundsInfo.x = (int) (savephotoinfo.x * tScale);
                                    PhotoBoundsInfo.y = (int) (savephotoinfo.y * tScale);
                                    matrix.postScale(tScale, tScale, mid.x, mid.y);

                                } else {
                                    matrix.postScale(tScale, tScale, mid.x, mid.y);
                                }

                                matrix.postRotate(rotation, mid.x, mid.y);// rotate

                            }

                        } else if (mode == ROTATE) {

                            float rotation = rotation(event) - oldRotation;
                            float newDist = spacing(event);
                            tScale = newDist / dist;

                            if (newDist > 10f) {
                                matrix.set(savedMatrix);

                                if (IsPhotoScaleMin && tScale > 1.0f) {

                                    PhotoBoundsInfo.set(savephotoinfo.x, savephotoinfo.y);
                                    PhotoBoundsInfo.x = (int) (savephotoinfo.x * tScale);
                                    PhotoBoundsInfo.y = (int) (savephotoinfo.y * tScale);

                                    matrix.postScale(tScale, tScale, mid.x, mid.y);
                                    IsPhotoScaleMin = false;

                                    mode = ZOOM;
                                }

                                if (IsPhotoScaleMax && tScale < 1.0f) {

                                    PhotoBoundsInfo.set(savephotoinfo.x, savephotoinfo.y);
                                    PhotoBoundsInfo.x = (int) (savephotoinfo.x * tScale);
                                    PhotoBoundsInfo.y = (int) (savephotoinfo.y * tScale);

                                    matrix.postScale(tScale, tScale, mid.x, mid.y);
                                    IsPhotoScaleMax = false;
                                    mode = ZOOM;
                                }

                                matrix.postRotate(rotation, mid.x, mid.y);// rotate
                                LOG.I(TAG, "photo rotateing");
                            }
                        }
                    }

                    break;

            }// end switch

            PhotoBoundsInfo.set(PhotoBoundsInfo.x, PhotoBoundsInfo.y);
            mPhotoView.setImageMatrix(matrix);

            return true;
        }
    };

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }


    public float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }


    public void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private IApiEventListener m_GetFriendEventListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {

            if (isSuccess) {
                friends_outObj data = (friends_outObj) obj;

                if (data == null) {
                    LOG.V(TAG, "m_GetFriendEventListener - data is null");
                    return;
                }

                m_ChooseContactDialog = new ChooseContactDialog(mAct, data);
                m_ChooseContactDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        LOG.I(TAG, "choose a contact dialog dimiss!");
                        mMailButton.setEnabled(true);
                        mAct.SetLastButtonEnable(true);
                    }
                });

                m_ChooseContactDialog.addButtonListener(new IButtonClickListener() {

                    @Override
                    public void onButtonClicked(int buttonId, String viewName, Object[] args) {

                        if (buttonId == ChooseContactDialog.YES_BUTTON_ID) {
                            ArrayList<FriendData> friendList = (ArrayList<FriendData>) args[0];

                            // store friend list
                            m_CurrentSelectedFriendDataList = friendList;

                            if (friendList == null || friendList.size() == 0) {
                                m_ChooseContactDialog.dismiss();
                            } else {
                                // create target id list
                                //ArrayList<String> idList = new ArrayList<String>();
                                for (FriendData fdata : friendList) {
                                    idList.add(fdata.userID);
                                }

                                // hide sticker controls
                                if (mStickerPage != null)
                                    mStickerPage.HideAllStickerControl();

                                SendPhoto();

                            }

                        } else if (buttonId == ChooseContactDialog.CANCEL_BUTTON_ID) {
                            m_ChooseContactDialog.dismiss();
                        }
                    }
                });

                m_ChooseContactDialog.show();

            } else {
                LOG.V(TAG, "m_GetFriendEventListener - failed to get friend list");
                mMailButton.setEnabled(true);
                mAct.SetLastButtonEnable(true);
            }

        }
    };

    private IApiEventListener SharePhotoWithThumbEventListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {

            if (isSuccess) {

                LOG.I(TAG, " Send photo success ");
                String PhotoSent = mAct.getResources().getString(R.string.PhotoHasBeenSent);
                m_PhotoSendingAnimationDialog.sentSuccess(PhotoSent);

                // notify GCM server
                NabiNotificationManager notificationManager = mAct.getNabiNotificationManager();
                for (FriendData fData : m_CurrentSelectedFriendDataList) {
                    notificationManager.notifyServerByUserKey(
                            fData.osgUserKey,
                            fData.osgKidId,
                            UserName,
                            getString(R.string.notification_photo_description),
                            NabiNotificationManager.APPLICATION_NAME_PHOTO,
                            new GCMSenderEventCallback() {

                                @Override
                                public void onSendMessageSuccess() {
                                }

                                @Override
                                public void onMessgaeSendingError(int errorCode) {
                                }
                            });
                }

                Handler handler = new Handler();
                handler.postDelayed(runnable_dismiss, 1300);

            } else {

                LOG.E(TAG, "Send Photo failure");
                m_PhotoSendingAnimationDialog.dismiss();
                m_ChooseContactDialog.dismiss();

                final PhotoSentFailedDialog faileddialog = new PhotoSentFailedDialog(mAct);
                faileddialog.addButtonListener(new IButtonClickListener() {

                    @Override
                    public void onButtonClicked(int buttonId, String viewName, Object[] args) {
                        // TODO Auto-generated method stub


                        if (faileddialog != null && faileddialog.isShowing())
                            faileddialog.dismiss();

                        switch (buttonId) {
                            case PhotoSentFailedDialog.CLOSE_BUTTON_ID:
                            case PhotoSentFailedDialog.X_BUTTON_ID:
                                faileddialog.dismiss();

                                //tracking
                                Tracking.pushTrack(getActivity(), "dialog_photo_not_sent_close");

                                break;
                            case PhotoSentFailedDialog.OK_BUTTON_ID:
                                // send photo

                                SendPhoto();

                                //tracking
                                Tracking.pushTrack(getActivity(), "dialog_photo_not_sent_send_try_again");
                                break;
                        }

                    }
                });

                faileddialog.show();

            }

        }
    };

    final Runnable runnable_dismiss = new Runnable() {
        public void run() {

            m_PhotoSendingAnimationDialog.dismiss();
            m_ChooseContactDialog.dismiss();

            if (!PhotoCameraEditPhotoFragment.this.isResumed()) {
                return;
            }

            LOG.W(TAG, "mFragmentManager.getBackStackEntryCount() = " + PhotoActFragmentManager.getBackStackEntryCount());
            if (PhotoActFragmentManager.getBackStackEntryCount() > 1) {
                PhotoActFragmentManager.popBackStack(PhotoParameter.FRAGMENTTAG_INBOX, 0);
                LOG.W(TAG, "After send photo and back to inbox");
            }

			/*boolean nowmode = mode.getBoolean("MODE", false);
            String logonUserKey = mode.getString("LOGON_USER_KEY", "");
			LOG.V(TAG, " nowmode = " + nowmode);
			Intent intent = new Intent(mCtx, PhotoActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // close change
																// activity
																// animation
			intent.putExtra(KEY_IS_MOMMY_MODE, nowmode);
			intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
			startActivity(intent);*/
        }
    };

    private void SendPhoto() {

        Bitmap bitmap = loadBitmapFromView();
        Bitmap thumbnailbitmap = ResizeBitmap(bitmap, PhotoThumbnailWidth, PhotoThumbnailHeight);

        if (!mAct.getNetworkManager().checkWifiProcess())
            return;

        mAct.SharePhotoWithThumb(UserId,
                idList,
                Utils.saveToInternalSorage(mAct, bitmap),
                "photoName",
                Utils.savethumbnailToInternalSorage(mAct, thumbnailbitmap),
                GetBitmapSizeStr(thumbnailbitmap.getWidth(), thumbnailbitmap.getHeight()),
                GetBitmapSizeStr(bitmap.getWidth(), bitmap.getHeight()));


        m_PhotoSendingAnimationDialog = new PhotoSendingAnimationDialog(mAct);
        m_PhotoSendingAnimationDialog.setCancelable(false);
        m_PhotoSendingAnimationDialog.show();

    }

    // sid added start----------------------------------------------
    // merger bitmap
    public Bitmap loadBitmapFromView() {
        mShowPhotoLayout.measure(MeasureSpec.makeMeasureSpec(mNowPhotoWidth,
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                mNowPhotoHeight, MeasureSpec.EXACTLY));

        Bitmap allLayoutBitmap = Bitmap.createBitmap(
                mShowPhotoLayout.getWidth(), mShowPhotoLayout.getHeight(),
                Bitmap.Config.ARGB_8888);

        LOG.E(TAG, "ShowPhotoLayout size = " + mShowPhotoLayout.getWidth() + ","
                + mShowPhotoLayout.getHeight());
        LOG.E(TAG, "mNowPhotoWidth size = " + mNowPhotoWidth + ","
                + mNowPhotoHeight);
        LOG.E(TAG, "allLayoutBitmap size = " + allLayoutBitmap.getWidth() + ","
                + allLayoutBitmap.getHeight());
        Canvas rlCanvas = new Canvas(allLayoutBitmap);
        mShowPhotoLayout.draw(rlCanvas);
        return allLayoutBitmap;
    }
    // sid added end----------------------------------------------

    private Bitmap ResizeBitmap(Bitmap bmp, float sizewidth, float sizeheight) {
        float scale = 0f;
        if (bmp.getWidth() < bmp.getHeight() && PhotoExifOrientation == 0) {
            scale = (float) sizeheight / bmp.getWidth();
        } else if (PhotoExifOrientation == 90 || PhotoExifOrientation == 270) {
            scale = (float) sizeheight / bmp.getWidth();
        } else {
            scale = (float) sizewidth / bmp.getWidth();
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bmps = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        int resizePhotoW = bmps.getWidth();
        int resizePhotoH = bmps.getHeight();

        LOG.I(TAG, " resize bitmap w = " + resizePhotoW);
        LOG.I(TAG, " resize bitmap h = " + resizePhotoH);

        return bmps;
    }

    private String GetBitmapSizeStr(int w, int h) {
        return new String(w + "x" + h);
    }


    private void addApiEventListener() {
        LOG.V(TAG, "addApiEventListener() - start");

        mAct.onGetFriendList.addEventListener(m_GetFriendEventListener);
        mAct.onSharePhotoWithThumb.addEventListener(SharePhotoWithThumbEventListener);

        LOG.V(TAG, "addApiEventListener() - end");
    }

    private void removeApiEventListener() {
        LOG.V(TAG, "removeApiEventListener() - start");

        mAct.onGetFriendList.removeEventListener(m_GetFriendEventListener);
        mAct.onSharePhotoWithThumb.removeEventListener(SharePhotoWithThumbEventListener);

        LOG.V(TAG, "removeApiEventListener() - end");
    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        addApiEventListener();
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        removeApiEventListener();
        idList.clear();
        photoBitmap.recycle();
        photoBitmap = null;
    }
}
