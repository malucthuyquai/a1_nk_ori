package com.fuhu.nabiconnect.photo.fragment;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.data.SharedPhotoData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.PhotoActivity;
import com.fuhu.nabiconnect.photo.PhotoActivity.PhotoWidgetListener;
import com.fuhu.nabiconnect.photo.PhotoActivity.UserBehaviorListener;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;
import com.fuhu.nabiconnect.photo.widget.BaseScrollView;
import com.fuhu.nabiconnect.photo.widget.PhotoWidgetNew;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;

import java.util.ArrayList;

public class PhotoMyGalleryListViewFragment extends PhotoBaseFragment {
    public final static String TAG = "MyGalleryListViewFragment";

    private static final int GetSharedPhotoMaxNum = PhotoParameter.LoadphotofromServerNumber;
    private static int previous = -1;
    private static int TopProgressHeight = 40;
    private static int MyGallery_PhotoWidgetMargeParam = 10;
    private static int MyGallery_PhotoWidgetWidth = 560;

    private int NowPhotoPage = 1;
    private boolean IsUpdateBySlipDown = false;
    private boolean IsUpdateBySlipUp = false;
    private boolean IsSlipEnd = false;
    private boolean IsTopProgressShow = false;
    private boolean IsDeleteing = false;

    private Point PhotoWedgitBounds_Vertical;
    private Point PhotoWedgitBounds_Horizontal;
    private int[] PhotoWedgitType = {1, 0, 0, 0, 1, 1, 0, 1};    // 0 = Horizontal, 1 = vertical

    private UserBehaviorListener userBehaviorListener;

    private ArrayList<PhotoWidgetNew> mPhotoWidgetArray = new ArrayList<PhotoWidgetNew>();
    private ArrayList<SharedPhotoData> UserSharedPhotoData = new ArrayList<SharedPhotoData>();
    private ArrayList<SharedPhotoData> UserSharedPhotoDataFromDB = new ArrayList<SharedPhotoData>();
    private ArrayList<Drawable> SharedPhotoDrawableArray = new ArrayList<Drawable>();
    private ArrayList<String> SharedPhotoIdArray = new ArrayList<String>();
    private ArrayList<String> SharedPhotoBigUrlArray = new ArrayList<String>();

    private PhotoActivity mAct;

    private String UserId;
    private String UserName;
    private String NextPagePhotoUrl;
    private String PreviousPagePhotoUrl;

    private Drawable UserImage;
    //private FrameLayout FrameLayout;

    private RelativeLayout RightFrame;
    private AnimationListener m_PictureLayoutAnimationListener;

    private BaseScrollView scrollView;
    private TextView RefreshText;
    private ImageDownLoadManager mImageDM;

    View view;
    DatabaseAdapter db;

    public void SetUserAvatar(Drawable userimage) {
        LOG.I(TAG, "SetUserAvatar");
        this.UserImage = userimage;
    }

    public void SetPhotoListData(ArrayList<SharedPhotoData> SharedPhotoData) {
        this.UserSharedPhotoData = SharedPhotoData;
        LOG.I(TAG, "UserSharedPhotoData size = " + UserSharedPhotoData.size());
    }

    public void SetUserBehaviorListener(UserBehaviorListener listener) {
        this.userBehaviorListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mAct = (PhotoActivity) getActivity();
        db = mAct.getDatabaseAdapter();
        mImageDM = mAct.getImageDownloadManager();
        UserId = mAct.getUserId();
        UserName = mAct.getUserName();

        mImageDM.ReSetPhotoFinishCount();
        SetResolutionParameter();
        IsSlipEnd = false;
        LOG.I(TAG, "onCreat");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        LOG.I(TAG, "onCreateView");
        view = inflater.inflate(R.layout.photo_mygallerylist_layout, null);
        scrollView = (BaseScrollView) view.findViewById(R.id.MyGalleryScrollView);
        RightFrame = (RelativeLayout) view.findViewById(R.id.MyGalleryRightFrame);
        RefreshText = (TextView) view.findViewById(R.id.MyGallery_UpdateText);

        if ((ViewGroup) scrollView.getParent() != null)
            ((ViewGroup) scrollView.getParent()).removeAllViews();

        scrollView.setScrollViewListener(scrollstatelistener);
        scrollView.getView();

        NowPhotoPage = 1;
        previous = -1;
        mPhotoWidgetArray.clear();
        RightFrame.removeAllViews();

        LOG.W(TAG, "onCreateView------UserSharedPhotoData size = " + UserSharedPhotoData.size());
        SetPhotoWedgitData(UserSharedPhotoData);
        if (mPhotoWidgetArray.size() != 0)
            SetPhotoWedgitList(mPhotoWidgetArray);

        return scrollView;
    }

    private void SetResolutionParameter() {
        LOG.I(TAG, "screen width = " + ScreenWidth);
        switch (ScreenWidth) {
            case 1920:

                MyGallery_PhotoWidgetWidth = 560; // nabi XD is 400
                TopProgressHeight = 40;
                MyGallery_PhotoWidgetMargeParam = 10;

                PhotoWedgitBounds_Vertical = new Point(MyGallery_PhotoWidgetWidth, 600); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(MyGallery_PhotoWidgetWidth, 500); // x = width, y = height

                break;
            case 1280:
                MyGallery_PhotoWidgetWidth = 372; // nabi XD is 400
                TopProgressHeight = 25;
                MyGallery_PhotoWidgetMargeParam = 5;

                PhotoWedgitBounds_Vertical = new Point(MyGallery_PhotoWidgetWidth, 420); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(MyGallery_PhotoWidgetWidth, 320); // x = width, y = height

                break;
            case 1600:
                MyGallery_PhotoWidgetWidth = 464; // nabi XD is 400
                TopProgressHeight = 30;
                MyGallery_PhotoWidgetMargeParam = 8;

                PhotoWedgitBounds_Vertical = new Point(MyGallery_PhotoWidgetWidth, 500); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(MyGallery_PhotoWidgetWidth, 428); // x = width, y = height

                break;
            case 1024:
                MyGallery_PhotoWidgetWidth = 295;
                TopProgressHeight = 20;
                MyGallery_PhotoWidgetMargeParam = 5;

                PhotoWedgitBounds_Vertical = new Point(MyGallery_PhotoWidgetWidth, 340); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(MyGallery_PhotoWidgetWidth, 250); // x = width, y = height

                break;

            case 800:
                MyGallery_PhotoWidgetWidth = 226;
                TopProgressHeight = 20;
                MyGallery_PhotoWidgetMargeParam = 5;

                PhotoWedgitBounds_Vertical = new Point(MyGallery_PhotoWidgetWidth, 245); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(MyGallery_PhotoWidgetWidth, 205); // x = width, y = height

                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        LOG.I(TAG, "onResume");


        m_PictureLayoutAnimationListener = new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                mAct.SetLastButtonEnable(false);
                previous = -1;
                LOG.I(TAG, "setlastbutton false");
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) RightFrame.getLayoutParams();

                if (IsTopProgressShow) {
                    IsTopProgressShow = false;
                    params.setMargins(0, 0, 0, 0);
                    RightFrame.setLayoutParams(params);
                    mAct.SetLastButtonEnable(true);

                } else {
                    IsTopProgressShow = true;
                    RefreshText.setVisibility(View.VISIBLE);
                    userBehaviorListener.RefreshScrollView();
                    params.setMargins(0, TopProgressHeight, 0, 0);
                    RightFrame.setLayoutParams(params);

                }

            }
        };

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        LOG.I(TAG, "onPause");
        mImageDM.ReSetPhotoFinishCount();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        LOG.I(TAG, "onStop");
        mImageDM.CancelTask();

    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();

        mPhotoWidgetArray.clear();

    }

    private void LoadingPhotoFromServer() {

        ArrayList<String> needserverPhotoInfoParameter = new ArrayList<String>();
        needserverPhotoInfoParameter.add("url");
        needserverPhotoInfoParameter.add("to");
        needserverPhotoInfoParameter.add("url_tn");
        needserverPhotoInfoParameter.add("size");
        needserverPhotoInfoParameter.add("size_tn");
        mAct.GetSharedPhoto(UserId, needserverPhotoInfoParameter, 0, 0, GetSharedPhotoMaxNum);

    }

    private void LoadingNextPagePhoto(String nexturl) {

        mAct.CallUrlFromPaginatedResponseForSharePhoto(nexturl);
    }

    private void LoadingPreviousPagePhoto(String previousurl) {

        mAct.CallUrlFromPaginatedResponseForSharePhoto(previousurl);
    }

    private void GetPhotoDataFromDataBase() {

        UserSharedPhotoDataFromDB.clear();
        UserSharedPhotoDataFromDB = db.getSharedPhoto(UserId);
        LOG.W(TAG, "ReceivedPhotoDataFromDB size = " + UserSharedPhotoDataFromDB.size());

        if (UserSharedPhotoDataFromDB.size() != 0) {
            RightFrame.removeAllViews();
            mPhotoWidgetArray.clear();
            SharedPhotoDrawableArray.clear();
            SharedPhotoIdArray.clear();
            SharedPhotoBigUrlArray.clear();

            //mAct.PhotosNum.setText(String.valueOf(UserSharedPhotoDataFromDB.size()));
            LOG.W(TAG, "ReceivedPhotoDataFromDB ");
            //mAct.CloseMountainIcon();

            if (UserSharedPhotoDataFromDB.size() <= GetSharedPhotoMaxNum) {

                SetPhotoWedgitDataFromDB(UserSharedPhotoDataFromDB);

                if (mPhotoWidgetArray.size() != 0)
                    SetPhotoWedgitList(mPhotoWidgetArray);
            } else {

                int size = UserSharedPhotoDataFromDB.size();

                for (int i = GetSharedPhotoMaxNum; i < size; i++) {
                    UserSharedPhotoDataFromDB.remove(GetSharedPhotoMaxNum);
                }

                LOG.W(TAG, "after remove, ReceivedPhotoDataFromDB size = " + UserSharedPhotoDataFromDB.size());
                SetPhotoWedgitDataFromDB(UserSharedPhotoDataFromDB);

                if (mPhotoWidgetArray.size() != 0)
                    SetPhotoWedgitList(mPhotoWidgetArray);

            }

        } else {

            LoadingPhotoFromServer();
        }
        //-------------------------set database information
    }

    private void UpdateScrollViewBySlipDown() {
        NowPhotoPage++;
        this.IsUpdateBySlipDown = true;
        //this.IsFirstCallApi = true;
        LoadingNextPagePhoto(NextPagePhotoUrl);

    }

    private void UpdateScrollViewBySlipUp() {
        NowPhotoPage--;
        this.IsUpdateBySlipUp = true;

        if (IsSlipEnd)
            IsSlipEnd = false;

        LoadingPreviousPagePhoto(PreviousPagePhotoUrl);

    }

    private void AfterUpdateListView() {

        if (IsUpdateBySlipDown) {
            //LOG.I(TAG, "scrollTo = " + (MyGallery_PhotoWidgitWidth + MyGallery_PhotoWidgitWidth/2));
            //scrollView.scrollTo(0, (MyGallery_PhotoWidgitWidth + MyGallery_PhotoWidgitWidth/2));
            IsUpdateBySlipDown = false;
            scrollView.setScrollViewListener(scrollstatelistener);
        } else if (IsUpdateBySlipUp) {

            //scrollView.scrollTo(0, (MyGallery_PhotoWidgitWidth*3 + MyGallery_PhotoWidgitWidth/2));
            IsUpdateBySlipUp = false;
            scrollView.setScrollViewListener(scrollstatelistener);
        }
    }

    private void ShowTopProgress() {
        TranslateAnimation slideDown = new TranslateAnimation(0, 0, 0, TopProgressHeight);
        slideDown.setDuration(500);
        slideDown.setFillEnabled(true);
        //slideDown.setFillAfter(true);
        slideDown.setAnimationListener(m_PictureLayoutAnimationListener);
        RightFrame.startAnimation(slideDown);
    }

    private void CloseTopProgress() {
        RefreshText.setVisibility(View.GONE);
        TranslateAnimation slideUp = new TranslateAnimation(0, 0, 0, -TopProgressHeight);
        slideUp.setDuration(500);
        slideUp.setFillEnabled(true);
        //slideUp.setFillAfter(true);
        slideUp.setAnimationListener(m_PictureLayoutAnimationListener);
        RightFrame.startAnimation(slideUp);

    }

    private void SetPhotoWedgitData(ArrayList<SharedPhotoData> DataList) {
        if (DataList == null || DataList.isEmpty()) {
            return;
        }

        for (SharedPhotoData spdata : DataList) {

            String photoUrl = spdata.url;
            String thumbnailurl = spdata.url_tn;
            String photoId = spdata.id;
            String posttime = ChangePhotoTimeFormatAndCalculateToString(spdata.createdTime);
            String sharetouserid = null;
            if (spdata.m_ToList != null && !spdata.m_ToList.isEmpty())
                sharetouserid = spdata.m_ToList.get(0).id;


            LOG.V(TAG, "GetSharedPhotoInfo, photoId = " + photoId + " sharetouserid = " + sharetouserid);

            PhotoWidgetNew mPhotoWidget = new PhotoWidgetNew(mAct, UserImage,
                    thumbnailurl, photoUrl, UserName, photoId, UserId,
                    posttime, sharetouserid);
            mPhotoWidget.ExecutePhoto(mImageDM);
            mPhotoWidget.SetPhotoWidgetListener(pWidgetListener);
            //mPhotoWidget.SetDrawableArrayList(SharedPhotoDrawableArray);
            mPhotoWidgetArray.add(mPhotoWidget);
            //SharedPhotoIdArray.add(photoId);
            //SharedPhotoBigUrlArray.add(photoUrl);
        }
    }


    private void SetPhotoWedgitDataFromDB(ArrayList<SharedPhotoData> DataList) {

        for (SharedPhotoData spdata : DataList) {

            String photoId = spdata.id;
            String posttime = ChangePhotoTimeFormatAndCalculateToString(spdata.createdTime);
            String sharetouserid = null;
            if (!spdata.m_ToList.isEmpty())
                sharetouserid = spdata.m_ToList.get(0).id;

            LOG.V(TAG, "GetSharedPhotoInfo, photoId = " + photoId + " sharetouserid = " + sharetouserid);

            PhotoWidgetNew mPhotoWidget = new PhotoWidgetNew(mAct, UserImage, null,
                    null, UserName, photoId, UserId, posttime, sharetouserid);
            mPhotoWidget.ExecutePhoto(mImageDM);
            mPhotoWidget.SetPhotoWidgetListener(pWidgetListener);
            //mPhotoWidget.SetDrawableArrayList(SharedPhotoDrawableArray);
            mPhotoWidgetArray.add(mPhotoWidget);
            //SharedPhotoIdArray.add(photoId);

        }

        LOG.W(TAG, " After load photo widget, start loading from server ");
        LoadingPhotoFromServer();

    }

    //Arrange all photowedgit views in layout
    private void SetPhotoWedgitList(ArrayList<PhotoWidgetNew> photowidgetArray) {
        int preId = 0;
        int befPreId = 0;


        if (IsTopProgressShow) {
            CloseTopProgress();
        }


        for (int i = 0; i < photowidgetArray.size(); i++) {

            photowidgetArray.get(i).setId(i + 100);

            LOG.I(TAG, " PhotoWidgetArray (" + i + ") id = " + photowidgetArray.get(i).getId());
        }

        for (int i = 0; i < photowidgetArray.size(); i++) {

            if ((ViewGroup) photowidgetArray.get(i).getParent() != null)
                ((ViewGroup) photowidgetArray.get(i).getParent()).removeAllViews();

            photowidgetArray.get(i).SetX_ButtonClickListener(clickListener);
            //mPhotoWidgetArray.get(i).SetPhotoViewClickListener(clickListener);
            photowidgetArray.get(i).SetX_ButtonId(i);


            RelativeLayout.LayoutParams rlp;


            int c = i % PhotoWedgitType.length;

            if (PhotoWedgitType[c] == 0) {
                rlp = new RelativeLayout.LayoutParams(
                        PhotoWedgitBounds_Horizontal.x,
                        PhotoWedgitBounds_Horizontal.y);
            } else {
                rlp = new RelativeLayout.LayoutParams(
                        PhotoWedgitBounds_Vertical.x,
                        PhotoWedgitBounds_Vertical.y);
            }

            if (i == 0) {

                rlp.setMargins(0, 0, MyGallery_PhotoWidgetMargeParam, MyGallery_PhotoWidgetMargeParam);
                RightFrame.addView(photowidgetArray.get(i), rlp);
                LOG.I(TAG, "add view");
            } else {

                if (i == 1) {
                    rlp.setMargins(0, 0, 0, MyGallery_PhotoWidgetMargeParam);
                    rlp.addRule(RelativeLayout.RIGHT_OF, preId);

                } else {
                    rlp.setMargins(0, 0, 0, MyGallery_PhotoWidgetMargeParam);
                    rlp.addRule(RelativeLayout.ALIGN_LEFT, befPreId);
                    rlp.addRule(RelativeLayout.BELOW, befPreId);
                }

                RightFrame.addView(photowidgetArray.get(i), rlp);

                befPreId = preId;
            }

            preId = photowidgetArray.get(i).getId();
            LOG.I(TAG, " i = " + i + " preId = " + preId + " befPreId = " + befPreId);

        }
        LOG.E(TAG, "PhotoFinishCount = " + mImageDM.GetPhotoFinishCount());
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            int id = v.getId();

            if (id < 100 && !IsDeleteing) {   // click X button
                if (previous < 0) {
                    v.setBackgroundResource(R.drawable.photo_x_button_select);
                    previous = v.getId();
                    LOG.I(TAG, "get id = " + v.getId());
                } else {
                    if (v.getId() == previous) {  //execute delete photo
                        IsDeleteing = true;
                        String targetphotoId = mPhotoWidgetArray.get(previous).GetPhotoId();
                        userBehaviorListener.DeletePhoto(targetphotoId);
                        /*IsDeleteing = true;
                        boolean Is = db.deletePhoto(UserId, targetphotoId); //delete photo from database cache
						LOG.I(TAG, "delete photo from database success is = " + Is);
						
						LOG.I(TAG, "when delete photo, is parent mode = " + mAct.IsMommyMode);
						if (mAct.IsMommyMode) {
							mAct.NSADeletePhoto(UserId, targetphotoId);
						} else {
							mAct.DeleteOwnPhoto(targetphotoId);
						}					*/

                    } else {

                        mPhotoWidgetArray.get(previous).SetX_ButtonBackgroundResource(R.drawable.photo_x_button_unselect);
                        v.setBackgroundResource(R.drawable.photo_x_button_select);
                        previous = v.getId();

                        LOG.I(TAG, "get id = " + v.getId());
                    }
                }
            }
        }
    };

    private PhotoWidgetListener pWidgetListener = new PhotoWidgetListener() {

        @Override
        public void onGainIndex(int index) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onClickPhoto(int position) {
            // TODO Auto-generated method stub
            userBehaviorListener.ClickPhoto(position);
        }
    };

    private BaseScrollView.BaseScrollStateListener scrollstatelistener = new BaseScrollView.BaseScrollStateListener() {

        @Override
        public void onBottom() {
            // TODO Auto-generated method stub
            LOG.I(TAG, "Scroll onBottom mImageDM.GetPhotoFinishCount() = " + mImageDM.GetPhotoFinishCount());
            if (mImageDM.GetPhotoFinishCount() >= (GetSharedPhotoMaxNum * NowPhotoPage) && !IsDeleteing) {
                scrollView.removeScrollViewListener();
                userBehaviorListener.ScrollBottomLoading();
                //UpdateScrollViewBySlipDown();
            }

        }

        @Override
        public void onTop() {
            // TODO Auto-generated method stub
            LOG.I(TAG, "Scroll onTop ");
            if (!IsTopProgressShow && !IsDeleteing) {
                ShowTopProgress();
                //userBehaviorListener.RefreshScrollView();
            }
        }

        @Override
        public void onScroll() {
            // TODO Auto-generated method stub
            LOG.I(TAG, "Scroll onScroll ");
        }
    };

    public void UpdateHistoryPhotos(ArrayList<SharedPhotoData> list) {
        LOG.W(TAG, "UpdateHistoryPhotos");
        SetPhotoWedgitData(list);
        SetPhotoWedgitList(mPhotoWidgetArray);
        AfterUpdateListView();
    }


}
