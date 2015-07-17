package com.fuhu.nabiconnect.photo.fragment;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.data.ReceivedPhotoData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.PhotoActivity.PhotoWidgetListener;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager.OnTaskCompleted;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;
import com.fuhu.nabiconnect.photo.widget.BaseScrollView;
import com.fuhu.nabiconnect.photo.widget.PhotoWidgetNew;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.getReceivedPhotos_outObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PhotoInBoxFragment extends PhotoBaseFragment implements OnTaskCompleted {
    private static String TAG = "PhotoInBoxFragment";
    private FragmentManager mFragmentManager = null;
    private View rootView;

    private ImageView MountainIcon;
    private TextView NoPhotoText;
    private TextView RefreshText;
    private BaseScrollView scrollView;
    private RelativeLayout RightFrame;

    private static int previous;  // save the photowidget X-Button choose state
    private static int PhotoWedgitWidth = 850; //nabi XD is 600
    private static int TopProgressHeight = 40;
    private static int PhotoWedgitMargeParam = 10;
    private static final int GetReceivedPhotoMaxNum = PhotoParameter.LoadphotofromServerNumber;
    private static final String SERVERMODE = "servermode";
    private static final String DATABASEMODE = "databasemode";
    private String LoadMode = SERVERMODE;
    private int NowPhotoPage = 1; //first page is 1
    private boolean IsUpdateBySlipDown = false;
    private boolean IsSlipEnd = false;
    private boolean IsDeleteing = false;

    private boolean IsTopProgressShow = false;
    private Point PhotoWedgitBounds_Vertical; //x = width, y = height
    private Point PhotoWedgitBounds_Horizontal; //x = width, y = height

    private int[] PhotoWedgitType = {1, 0, 0, 0, 1, 1, 0, 1};    // 0 = Horizontal, 1 = vertical

    private ArrayList<ReceivedPhotoData> UserReceivedPhotoData = new ArrayList<ReceivedPhotoData>();
    private ArrayList<ReceivedPhotoData> ReceivedPhotoDataFromDB = new ArrayList<ReceivedPhotoData>();
    private ArrayList<ReceivedPhotoData> UserReceivedNextPagePhotoData = new ArrayList<ReceivedPhotoData>();
    private ArrayList<PhotoWidgetNew> PhotoWidgetArray = new ArrayList<PhotoWidgetNew>();
    private ArrayList<Drawable> ReceivedPhotoDrawableArray = new ArrayList<Drawable>();
    private ArrayList<String> ReceivedPhotoIdArray = new ArrayList<String>();
    private ArrayList<String> ReceivedPhotoBigUrlArray = new ArrayList<String>();

    private HashMap<String, String> FriendAvatarUrlMap = new HashMap<String, String>();
    private HashMap<Integer, Integer> ShowPhotoArrayPositionMap = new HashMap<Integer, Integer>();

    private String UserId;
    private String NextPagePhotoUrl;

    private ImageDownLoadManager mImageDM;
    private ShowPhotoFragment ShowPhotofragment;

    private AnimationListener m_PictureLayoutAnimationListener;

    DatabaseAdapter db;

    @Override
    public String getTrack() {
        return "home_feed";
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

        rootView = inflater.inflate(R.layout.photo_inbox_fragment_layout, null);
        mAct.SetLastButtonSelect(true, false, false);
        mFragmentManager = getFragmentManager();
        findView();
        SetResolutionParameter();

        scrollView.getView(); // it have to call function by this scrollview
        scrollView.setScrollViewListener(scrollstatelistener);

        previous = -1; //no icon selected
        db = mAct.getDatabaseAdapter();
        mImageDM = mAct.getImageDownloadManager();
        UserId = mAct.getUserId();
        GetPhotoDataFromDataBase();
        //LoadingPhotoFromServer();
        return rootView;
    }

    private void findView() {
        // TODO Auto-generated method stub
        RightFrame = (RelativeLayout) rootView.findViewById(R.id.RightFrame);
        scrollView = (BaseScrollView) rootView.findViewById(R.id.PhotoActivityScrollView);
        MountainIcon = (ImageView) rootView.findViewById(R.id.Photo_Mountain_Icon);
        NoPhotoText = (TextView) rootView.findViewById(R.id.Photo_NoPhotoText);
        RefreshText = (TextView) rootView.findViewById(R.id.Photo_UpdateText);
    }

    private void InitParameter() {
        LOG.V(TAG, "Init parameter");
        mImageDM.ReSetAvatarFinishCount();
        mImageDM.ReSetPhotoFinishCount();

        NowPhotoPage = 1; // first page is 1
        IsUpdateBySlipDown = false;
        IsSlipEnd = false;
        IsDeleteing = false;

        PhotoWidgetArray.clear();
        ReceivedPhotoDrawableArray.clear();
        ReceivedPhotoIdArray.clear();
        ReceivedPhotoBigUrlArray.clear();
        FriendAvatarUrlMap.clear();
    }

    private void SetResolutionParameter() {
        LOG.I(TAG, "screen width = " + ScreenWidth);
        switch (ScreenWidth) {
            case 1920:
                PhotoWedgitWidth = 850;
                TopProgressHeight = 40;
                PhotoWedgitMargeParam = 10;

                PhotoWedgitBounds_Vertical = new Point(PhotoWedgitWidth, 1000); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(PhotoWedgitWidth, 800); // x = width, y = height
                break;
            case 1280:

                PhotoWedgitWidth = 565;
                TopProgressHeight = 25;
                PhotoWedgitMargeParam = 5;

                PhotoWedgitBounds_Vertical = new Point(PhotoWedgitWidth, 655); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(PhotoWedgitWidth, 475); // x = width, y = height
                break;
            case 1600:

                PhotoWedgitWidth = 705;
                TopProgressHeight = 35;
                PhotoWedgitMargeParam = 8;

                PhotoWedgitBounds_Vertical = new Point(PhotoWedgitWidth, 800); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(PhotoWedgitWidth, 610); // x = width, y = height
                break;
            case 1024:

                PhotoWedgitWidth = 450;
                TopProgressHeight = 20;
                PhotoWedgitMargeParam = 5;

                PhotoWedgitBounds_Vertical = new Point(PhotoWedgitWidth, 500); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(PhotoWedgitWidth, 400); // x = width, y = height
                break;
            case 800:

                PhotoWedgitWidth = 342;
                TopProgressHeight = 20;
                PhotoWedgitMargeParam = 5;

                PhotoWedgitBounds_Vertical = new Point(PhotoWedgitWidth, 365); // x = width, y = height
                PhotoWedgitBounds_Horizontal = new Point(PhotoWedgitWidth, 295); // x = width, y = height
                break;
            default:
                break;
        }
    }

    private void LoadingPhotoFromServer() {
        LoadMode = SERVERMODE;

        ArrayList<String> needserverPhotoInfoParameter = new ArrayList<String>();
        needserverPhotoInfoParameter.add("url");
        needserverPhotoInfoParameter.add("createdTime");
        needserverPhotoInfoParameter.add("fromAvatarUrl");
        needserverPhotoInfoParameter.add("fromName");
        needserverPhotoInfoParameter.add("fromId");
        needserverPhotoInfoParameter.add("url_tn");
        needserverPhotoInfoParameter.add("size_tn");
        needserverPhotoInfoParameter.add("size");
        mAct.GetReceivedPhoto(UserId, needserverPhotoInfoParameter, 0, 0, GetReceivedPhotoMaxNum);
    }

    private void LoadingNextPagePhoto(String nexturl) {
        mAct.CallUrlFromPaginatedResponseForReceivedPhoto(nexturl);
    }

    private void GetPhotoDataFromDataBase() {

        LoadMode = DATABASEMODE;
        ReceivedPhotoDataFromDB.clear();
        ReceivedPhotoDataFromDB = db.getReceivedPhoto(UserId);
        LOG.W(TAG, "ReceivedPhotoDataFromDB  size = " + ReceivedPhotoDataFromDB.size());

        if (ReceivedPhotoDataFromDB.size() != 0) {
            RightFrame.removeAllViews();
            InitParameter();

            LOG.W(TAG, "ReceivedPhotoDataFromDB ");
            MountainIcon.setVisibility(View.INVISIBLE);
            NoPhotoText.setVisibility(View.INVISIBLE);


            if (ReceivedPhotoDataFromDB.size() <= GetReceivedPhotoMaxNum) {

                SetPhotoWedgitDataFromDB(ReceivedPhotoDataFromDB);

                if (PhotoWidgetArray.size() != 0)
                    SetPhotoWedgitList(PhotoWidgetArray);
            } else {
                int size = ReceivedPhotoDataFromDB.size();

                for (int i = GetReceivedPhotoMaxNum; i < size; i++) {
                    ReceivedPhotoDataFromDB.remove(GetReceivedPhotoMaxNum);
                }

                LOG.W(TAG, "after remove, ReceivedPhotoDataFromDB size = " + ReceivedPhotoDataFromDB.size());
                SetPhotoWedgitDataFromDB(ReceivedPhotoDataFromDB);

                if (PhotoWidgetArray.size() != 0)
                    SetPhotoWedgitList(PhotoWidgetArray);
            }

        } else {
            //LOG.W(TAG, "ReceivedPhotoDataFromServer ");
            LoadingPhotoFromServer();
        }
        //-------------------------set database information
    }


    private void SetPhotoWedgitData(ArrayList<ReceivedPhotoData> DataList) {

        for (ReceivedPhotoData data : DataList) {

            String photoUrl = data.url.replaceAll("https", "http");
            String photoThumbnail = data.tn_url;
            String photoId = data.id;
            String fromavatar = data.fromAvatarUrl.replaceAll("https", "http");
            String fromname = data.fromName;
            String fromId = String.valueOf(data.fromId);
            String posttime = ChangePhotoTimeFormatAndCalculateToString(data.createdTime);

            PhotoWidgetNew mPhotoWidget = new PhotoWidgetNew(mAct, fromavatar,
                    photoThumbnail, photoUrl, fromname, photoId, UserId,
                    posttime, fromId);
            mPhotoWidget.SetPhotoIdArrayList(ReceivedPhotoIdArray);
            mPhotoWidget.SetPhotoWidgetListener(pWidgetListener);
            PhotoWidgetArray.add(mPhotoWidget);
            ReceivedPhotoIdArray.add(photoId);
            ReceivedPhotoBigUrlArray.add(photoUrl);

            if (!FriendAvatarUrlMap.containsKey(fromId)) {
                FriendAvatarUrlMap.put(fromId, fromavatar);
                LOG.I(TAG, " FriendAvatarUrlMap size = " + FriendAvatarUrlMap.size());
            }
        }


        LOG.W(TAG, " PhotoWidgetArray size = " + PhotoWidgetArray.size());
        int index;
        index = mImageDM.GetAvatarFinishCount();
        LOG.W(TAG, " mImageDM.GetAvatarFinishCount() = " + index);
        PhotoWidgetArray.get(index).ExecuteAvatar(mImageDM, this);

    }

    private void SetPhotoWedgitDataFromDB(ArrayList<ReceivedPhotoData> DataList) {

        for (ReceivedPhotoData data : DataList) {

            String photoId = data.id;
            String fromname = data.fromName;
            String fromId = String.valueOf(data.fromId);
            String posttime = ChangePhotoTimeFormatAndCalculateToString(data.createdTime);

            PhotoWidgetNew mPhotoWidget = new PhotoWidgetNew(mAct, "", "", "",
                    fromname, photoId, UserId, posttime, fromId);
            mPhotoWidget.SetPhotoIdArrayList(ReceivedPhotoIdArray);
            mPhotoWidget.SetPhotoWidgetListener(pWidgetListener);

            PhotoWidgetArray.add(mPhotoWidget);
            ReceivedPhotoIdArray.add(photoId);

        }

        int index;
        index = mImageDM.GetAvatarFinishCount();
        PhotoWidgetArray.get(index).ExecuteAvatar(mImageDM, this);

    }

    private void SetPhotoWedgitList(ArrayList<PhotoWidgetNew> photowidgetArray) {
        int preId = 0;
        int befPreId = 0;

        LOG.I(TAG, " set photowidgetArray size = " + photowidgetArray.size());
        for (int i = 0; i < photowidgetArray.size(); i++) {

            photowidgetArray.get(i).setId(i + 100);
            LOG.I(TAG, " PhotoWidgetArray (" + i + ") id = " + photowidgetArray.get(i).getId());
        }

        for (int i = 0; i < photowidgetArray.size(); i++) {

            if ((ViewGroup) photowidgetArray.get(i).getParent() != null)
                ((ViewGroup) photowidgetArray.get(i).getParent())
                        .removeAllViews();

            photowidgetArray.get(i).SetX_ButtonClickListener(clickListener);
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

                rlp.setMargins(0, 0, PhotoWedgitMargeParam,
                        PhotoWedgitMargeParam);
                RightFrame.addView(photowidgetArray.get(i), rlp);

            } else {

                if (i == 1) {
                    rlp.setMargins(0, 0, 0, PhotoWedgitMargeParam);
                    rlp.addRule(RelativeLayout.RIGHT_OF, preId);
                    // LOG.I("homeinbox"," i = "+ i + " preId = "+ preId);
                } else {
                    rlp.setMargins(0, 0, 0, PhotoWedgitMargeParam);
                    rlp.addRule(RelativeLayout.ALIGN_LEFT, befPreId);
                    rlp.addRule(RelativeLayout.BELOW, befPreId);
                    // LOG.I("homeinbox"," i = "+ i + " preId = "+ preId +
                    // " befPreId = " + befPreId);
                }

                RightFrame.addView(photowidgetArray.get(i), rlp);

                befPreId = preId;
            }

            preId = photowidgetArray.get(i).getId();

        }

        LOG.E(TAG, "PhotoFinishCount = " + mImageDM.GetPhotoFinishCount());

    }


    private PhotoWidgetListener pWidgetListener = new PhotoWidgetListener() {

        @Override
        public void onGainIndex(int index) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onClickPhoto(int position) {
            // TODO Auto-generated method stub
            SwitchToShowPhotoFragment(position);

            //tracking
            Tracking.pushTrack(getActivity(), "fullscreen_view_" + position);
        }
    };

    public void SwitchToShowPhotoFragment(int position) {

        if (!IsTopProgressShow) {
            SetReceivedPhotoDrawableArrayFromCache();

            if (ReceivedPhotoIdArray.size() > ReceivedPhotoDrawableArray.size()) {
                if (ShowPhotoArrayPositionMap.containsKey(position))
                    position = ShowPhotoArrayPositionMap.get(position);
            }

            Bundle bundle = new Bundle();
            bundle.putInt("photoposition", position);


            LOG.W(TAG, "ReceivedPhotoDrawableArray size:" + ReceivedPhotoDrawableArray.size());
            LOG.W(TAG, "ReceivedPhotoBigUrlArray size:" + ReceivedPhotoBigUrlArray.size());
            LOG.W(TAG, "ReceivedPhotoIdArray size:" + ReceivedPhotoIdArray.size());


            if (!ReceivedPhotoDrawableArray.isEmpty()) {

                ShowPhotofragment = new ShowPhotoFragment();
                ShowPhotofragment.setArguments(bundle);
                ShowPhotofragment.SetPhotoDrawableArray(ReceivedPhotoDrawableArray);
                ShowPhotofragment.SetPhotoDrawbleUrlAndIdArray(ReceivedPhotoBigUrlArray, ReceivedPhotoIdArray, mImageDM);
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.PhotoNewActivityLayout, ShowPhotofragment, "ShowPhotoFragment");
                transaction.commit();
            }
        }
    }

    private void SetReceivedPhotoDrawableArrayFromCache() {

        if (ReceivedPhotoIdArray.size() != 0) {
            ReceivedPhotoDrawableArray.clear();
            int arrayposition = 0;
            int idposition = 0;
            for (String id : ReceivedPhotoIdArray) {
                Drawable img = mImageDM.GetMemoryCache().get(id);
                if (img != null) {
                    ReceivedPhotoDrawableArray.add(img);
                    LOG.W(TAG, "idposition = " + idposition + " arrayposition = " + arrayposition);
                    ShowPhotoArrayPositionMap.put(idposition, arrayposition);
                    arrayposition++;
                }
                idposition++;
            }
        }
    }


    private void UpdateScrollViewBySlipDown() {
        NowPhotoPage++;
        this.IsUpdateBySlipDown = true;
        LoadingNextPagePhoto(NextPagePhotoUrl);
    }

    private void AfterUpdateListView() {

        if (IsUpdateBySlipDown) {
            IsUpdateBySlipDown = false;
            //scrollView.setScrollViewListener(mAct);
            scrollView.setScrollViewListener(scrollstatelistener);
        }
    }


    private void ShwoTopProgress() {
        TranslateAnimation slideDown = new TranslateAnimation(0, 0, 0, TopProgressHeight);
        slideDown.setDuration(500);
        slideDown.setFillEnabled(true);
        slideDown.setAnimationListener(m_PictureLayoutAnimationListener);
        RightFrame.startAnimation(slideDown);

    }

    private void CloseTopProgress() {
        RefreshText.setVisibility(View.GONE);
        TranslateAnimation slideUp = new TranslateAnimation(0, 0, 0, -TopProgressHeight);
        slideUp.setDuration(500);
        slideUp.setFillEnabled(true);
        slideUp.setAnimationListener(m_PictureLayoutAnimationListener);
        RightFrame.startAnimation(slideUp);

    }

    private void IsShowMountainIcon(int photoCount) {
        if (photoCount < 1) {
            MountainIcon.setVisibility(View.VISIBLE);
            NoPhotoText.setVisibility(View.VISIBLE);
        } else {
            MountainIcon.setVisibility(View.INVISIBLE);
            NoPhotoText.setVisibility(View.INVISIBLE);
        }
    }


    private IApiEventListener mGetReceivePhotoListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            // TODO Auto-generated method stub

            if (IsTopProgressShow)
                CloseTopProgress();

            if (isSuccess) {
                LOG.V(TAG, "GetReceivedPhotoSuccess!!");
                getReceivedPhotos_outObj data = (getReceivedPhotos_outObj) obj;
                data.dumpData();

                boolean isdbSuccess = db.updateReceivedPhoto(UserId, data, true);
                LOG.V(TAG, "update database isSuccess = " + isdbSuccess);

                UserReceivedPhotoData = data.getPhotos();
                NextPagePhotoUrl = data.GetNextPageUrl();
                int count = UserReceivedPhotoData.size();
                IsShowMountainIcon(count);
                RightFrame.removeAllViews();

                if (count > 0) {

                    InitParameter();


                    if (UserReceivedPhotoData.size() <= GetReceivedPhotoMaxNum) {

                        SetPhotoWedgitData(UserReceivedPhotoData);

                        if (PhotoWidgetArray.size() != 0)
                            SetPhotoWedgitList(PhotoWidgetArray);
                        // LoadingNextPagePhoto(NextPagePhotoUrl);

                    } else {

                        for (int i = GetReceivedPhotoMaxNum; i < count; i++) {
                            UserReceivedPhotoData.remove(GetReceivedPhotoMaxNum);
                        }

                        LOG.W(TAG, "after remove, UserReceivedPhotoData size = " + UserReceivedPhotoData.size());
                        SetPhotoWedgitData(UserReceivedPhotoData);

                        if (PhotoWidgetArray.size() != 0)
                            SetPhotoWedgitList(PhotoWidgetArray);

                    }

                }

            } else {
                LOG.V(TAG, "GetReceivedPhotoFailure!!");
                mAct.showGeneralWarningDialog();
            }

            mAct.SetLastButtonEnable(true);

        }

    };

    private IApiEventListener onCallUrlFromPaginatedResponseForReceivedPhotoListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            // TODO Auto-generated method stub
            if (isSuccess) {

                LOG.V(TAG, "CallUrlFromPaginatedResponseForReceivedPhotoSuccess");
                getReceivedPhotos_outObj data = (getReceivedPhotos_outObj) obj;
                data.dumpData();

                UserReceivedNextPagePhotoData = data.getPhotos();

                if (UserReceivedNextPagePhotoData.size() != 0) {

                    NextPagePhotoUrl = data.GetNextPageUrl();
                    LOG.V(TAG, " PhotoWidgetArray size = " + PhotoWidgetArray.size());
                    LOG.V(TAG, " UserReceivedNextPagePhotoData size = " + UserReceivedNextPagePhotoData.size());
                    SetPhotoWedgitData(UserReceivedNextPagePhotoData);

                    SetPhotoWedgitList(PhotoWidgetArray);
                    AfterUpdateListView();

                } else {
                    LOG.I(TAG, "slip end = " + IsSlipEnd);

                    IsSlipEnd = true;
                    IsUpdateBySlipDown = false;
                    scrollView.setScrollViewListener(scrollstatelistener);

                }

            } else {
                LOG.V(TAG, "CallUrlFromPaginatedResponseForReceivedPhotoFailure!!");
                mAct.showGeneralWarningDialog();
            }
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            int id = v.getId();
            //LOG.I(TAG, Integer.toString(id));

            if (id < 100 && !IsDeleteing) {   // click X button
                if (previous < 0) {
                    v.setBackgroundResource(R.drawable.photo_x_button_select);
                    previous = v.getId();
                    LOG.I(TAG, "get id = " + v.getId());

                    //tracking
                    Tracking.pushTrack(getActivity(), "delete_photo_select_#" + PhotoWidgetArray.get(previous).GetPhotoId());

                } else {
                    if (v.getId() == previous) {  //execute delete photo

                        String targetphotoId = PhotoWidgetArray.get(previous).GetPhotoId();


                        mImageDM.CancelTask();
                        IsDeleteing = true;
                        boolean Is = db.deletePhoto(UserId, targetphotoId); //delete photo from database cache
                        LOG.I(TAG, "delete photo from database success is = " + Is);

                        LOG.I(TAG, "when delete photo, is parent mode = " + mAct.IsMommyMode);
//                        if (mAct.IsMommyMode) {
//                            mAct.NSADeleteReceivedPhoto(UserId, targetphotoId);
//                        } else {
                            mAct.DeletePhoto(UserId, targetphotoId);    //delete photo from server
//                        }

                    } else {

                        PhotoWidgetArray.get(previous).SetX_ButtonBackgroundResource(R.drawable.photo_x_button_unselect);
                        v.setBackgroundResource(R.drawable.photo_x_button_select);
                        previous = v.getId();

                        LOG.I(TAG, "get id = " + v.getId());
                    }

                    //tracking
                    Tracking.pushTrack(getActivity(), "delete_photo_#" + PhotoWidgetArray.get(previous).GetPhotoId());
                }
            }


        }
    };

    private IApiEventListener onDeletePhotoListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            // TODO Auto-generated method stub
            if (isSuccess) {
                RightFrame.removeAllViews();
                previous = -1;
                NowPhotoPage = 1;

                LoadingPhotoFromServer();
            } else {
                mAct.showGeneralWarningDialog();
            }
        }
    };

    private BaseScrollView.BaseScrollStateListener scrollstatelistener = new BaseScrollView.BaseScrollStateListener() {

        @Override
        public void onBottom() {
            // TODO Auto-generated method stub
            LOG.I(TAG, "Scroll onBottom ");
            if (mImageDM.GetPhotoFinishCount() >= (GetReceivedPhotoMaxNum * NowPhotoPage)) {
                scrollView.removeScrollViewListener();
                UpdateScrollViewBySlipDown();
            }

        }

        @Override
        public void onTop() {
            // TODO Auto-generated method stub
            LOG.I(TAG, "Scroll onTop ");
            if (!IsTopProgressShow) {
                mImageDM.CancelTask();
                ShwoTopProgress();
            }
        }

        @Override
        public void onScroll() {
            // TODO Auto-generated method stub
            LOG.I(TAG, "Scroll onScroll ");
        }

    };

    private void addApiEventListener() {
        LOG.V(TAG, "addApiEventListener() - start");

        mAct.onGetReceivedPhoto.addEventListener(mGetReceivePhotoListener);
        mAct.onCallUrlFromPaginatedResponseForReceivedPhoto.addEventListener(onCallUrlFromPaginatedResponseForReceivedPhotoListener);
        mAct.onDeletePhoto.addEventListener(onDeletePhotoListener);

        LOG.V(TAG, "addApiEventListener() - end");
    }

    private void removeApiEventListener() {
        LOG.V(TAG, "removeApiEventListener() - start");

        mAct.onGetReceivedPhoto.removeEventListener(mGetReceivePhotoListener);
        mAct.onCallUrlFromPaginatedResponseForReceivedPhoto.removeEventListener(onCallUrlFromPaginatedResponseForReceivedPhotoListener);
        mAct.onDeletePhoto.addEventListener(onDeletePhotoListener);
        LOG.V(TAG, "removeApiEventListener() - end");
    }

    @Override
    public void onTaskCompleted() {
        // TODO Auto-generated method stub

        int index;
        index = mImageDM.GetAvatarFinishCount();
        LOG.I(TAG, " Avatar onTaskCompleted and index = " + index);

        if (index < PhotoWidgetArray.size())
            PhotoWidgetArray.get(index).ExecuteAvatar(mImageDM, this);
        else {
            LOG.W(TAG, " load mode = " + this.LoadMode);

            LOG.I(TAG, " Start load photo and index = " + index);

            for (PhotoWidgetNew PW : PhotoWidgetArray)
                PW.ExecutePhoto(mImageDM);

            if (this.LoadMode.equals(SERVERMODE)) {
                LOG.W(TAG, " After load photo widget, start check and update avatar image ");

                Iterator iter = FriendAvatarUrlMap.entrySet().iterator();
                while (iter.hasNext()) {

                    Map.Entry<String, String> entry = (Map.Entry) iter.next();
                    String key = entry.getKey();
                    String url = entry.getValue();
                    mImageDM.LoadToDataBaseAndUpdateAvatarView(url, key, UserId, null);

                }
            } else if (this.LoadMode.equals(DATABASEMODE)) {
                LOG.W(TAG, " After load photo widget, start loading from server ");
                LoadingPhotoFromServer();

            } else {
                LOG.W(TAG, " After load history photo widget,  there is nothing ");

            }
        }

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        addApiEventListener();
        m_PictureLayoutAnimationListener = new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                mAct.SetLastButtonEnable(false);
                previous = -1;
                LOG.I(TAG, "AnimationStart");

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
                    mImageDM.CancelTask();
                    LoadingPhotoFromServer();
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

    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        removeApiEventListener();
        mImageDM.CancelTask();
    }
}
