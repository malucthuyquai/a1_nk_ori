package com.fuhu.nabiconnect.photo.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuhu.data.SharedPhotoData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.PhotoActivity.UserBehaviorListener;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager.OnTaskCompleted;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.getSharedPhotos_outObj;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotoMyGalleryFragment extends PhotoBaseFragment implements
        OnTaskCompleted, UserBehaviorListener {
    public static final String TAG = "PhotoMyGalleryFragment";

    private static final int GetSharedPhotoMaxNum = PhotoParameter.LoadphotofromServerNumber;
    private static String MyGalleryMode = PhotoParameter.MYGALLERY_LISTVIEW_MODE;

    private View rootView;
    private ImageButton mGridImgButton;
    private ImageButton mListImgButton;
    private ImageView UserImage;
    private ImageView MountainIcon;
    private TextView UserName;
    private TextView PhotosNum;
    private TextView FriendsNum;
    private TextView NoPhotoText;
    private FrameLayout RightFrame;

    private PhotoMyGalleryGridViewFragment GridViewPage;
    private PhotoMyGalleryListViewFragment ListViewPage;
    private ShowPhotoFragment ShowPhotofragment;

    private FragmentManager mFragmentManager = null;

    private ArrayList<Drawable> SharedPhotoDrawableCacheArray = new ArrayList<Drawable>();
    private ArrayList<String> SharedPhotoIdArray = new ArrayList<String>();
    private ArrayList<String> SharedPhotoBigUrlArray = new ArrayList<String>();
    private HashMap<Integer, Integer> ShowPhotoArrayPositionMap = new HashMap<Integer, Integer>();
    private ArrayList<SharedPhotoData> UserSharedPhotoData = new ArrayList<SharedPhotoData>();

    private Drawable SaveUserImage = null;

    private String UserId; // recode user id
    private String UserAvatarUrl = null;
    private String SaveUserName;
    private String NextPagePhotoUrl;

    private ImageDownLoadManager mImageDM;
    DatabaseAdapter db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        rootView = inflater.inflate(R.layout.photo_mygallery_fragment_layout, null);
        findView();
        mAct.SetLastButtonSelect(false, false, true);
        //mAct.SetLastButtonEnable(false);
        SetGridandListButtonEnable(false);

        db = mAct.getDatabaseAdapter();
        mImageDM = mAct.getImageDownloadManager();
        UserId = mAct.getUserId();
        UserAvatarUrl = mAct.getUserAvatarUrl();
        SaveUserName = mAct.getUserName();
        UserName.setText(mAct.getUserName());
        FriendsNum.setText(String.valueOf(mAct.getUserFriendsCount()));

        cleanArrayList();

        return rootView;
    }

    private void findView() {
        MountainIcon = (ImageView) rootView.findViewById(R.id.MyGallery_Mountain_Icon);
        NoPhotoText = (TextView) rootView.findViewById(R.id.MyGalllery_NoPhotoText);
        RightFrame = (FrameLayout) rootView.findViewById(R.id.RightFrame);

        UserImage = (ImageView) rootView.findViewById(R.id.Profile_Photo);
        UserName = (TextView) rootView.findViewById(R.id.Profile_Name);
        PhotosNum = (TextView) rootView.findViewById(R.id.MyGallery_PhotosNum);
        FriendsNum = (TextView) rootView.findViewById(R.id.MyGallery_FriendsNum);

        mGridImgButton = (ImageButton) rootView.findViewById(R.id.MyGallery_GridButton);
        mListImgButton = (ImageButton) rootView.findViewById(R.id.MyGallery_ListButton);
        mGridImgButton.setOnClickListener(clickListener);
        mListImgButton.setOnClickListener(clickListener);
        mListImgButton.setSelected(true);
    }

    private void addFragment(Fragment f, String tag) {
        if (mFragmentManager == null) {
            mFragmentManager = getFragmentManager();
        }

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.RightFrame, f, tag);
        transaction.commit();
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


    public void SetGridandListButtonEnable(boolean is) {
        mGridImgButton.setEnabled(is);
        mListImgButton.setEnabled(is);
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

    private void GetPhotoDataFromDataBase() {

        UserSharedPhotoData = db.getSharedPhoto(UserId);
        int DBdataSize = UserSharedPhotoData.size();
        LOG.W(TAG, "ReceivedPhotoDataFromDB size = " + DBdataSize);

        if (DBdataSize != 0) {

            LOG.W(TAG, "ReceivedPhotoDataFromDB----------start");

            if (DBdataSize <= GetSharedPhotoMaxNum) {

                IsShowMountainIcon(DBdataSize);
                PhotosNum.setText(String.valueOf(DBdataSize));

                for (SharedPhotoData photodata : UserSharedPhotoData) {
                    SharedPhotoIdArray.add(photodata.id);
                    SharedPhotoBigUrlArray.add(photodata.url);
                }

                SwitchToListFragment();

                LOG.W(TAG, " After load photo widget, start loading from server ");
                LoadingPhotoFromServer();

            } else {

                int size = UserSharedPhotoData.size();

                for (int i = GetSharedPhotoMaxNum; i < size; i++) {
                    UserSharedPhotoData.remove(GetSharedPhotoMaxNum);
                }

                LOG.W(TAG, "after remove, ReceivedPhotoDataFromDB size = " + UserSharedPhotoData.size());
                for (SharedPhotoData photodata : UserSharedPhotoData) {
                    SharedPhotoIdArray.add(photodata.id);
                    SharedPhotoBigUrlArray.add(photodata.url);
                }

                SwitchToListFragment();

            }
            LOG.W(TAG, "ReceivedPhotoDataFromDB----------end");
        } else {

            LoadingPhotoFromServer();
        }
        //-------------------------set database information
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.MyGallery_GridButton:

                    mGridImgButton.setSelected(true);
                    mListImgButton.setSelected(false);

                    if (GridViewPage != null && GridViewPage.isAdded()) {
                        LOG.I(TAG, "GridViewPage isadded !! ");
                        return;
                    }

                    SwitchToGridFragment();

                    break;

                case R.id.MyGallery_ListButton:

                    mGridImgButton.setSelected(false);
                    mListImgButton.setSelected(true);

                    LOG.I(TAG, "ListViewPage != null ");
                    if (ListViewPage != null && ListViewPage.isAdded()) {
                        LOG.I(TAG, "ListViewPage is added !!");
                        return;
                    }

                    SwitchToListFragment();

                    break;

            }
        }
    };

    private void SwitchToListFragment() {
        MyGalleryMode = PhotoParameter.MYGALLERY_LISTVIEW_MODE;
        ListViewPage = new PhotoMyGalleryListViewFragment();
        ListViewPage.SetUserAvatar(SaveUserImage);
        ListViewPage.SetPhotoListData(UserSharedPhotoData);
        ListViewPage.SetUserBehaviorListener(PhotoMyGalleryFragment.this);
        addFragment(ListViewPage, "ListViewPage");
    }

    private void SwitchToGridFragment() {
        MyGalleryMode = PhotoParameter.MYGALLERY_GRIDVIEW_MODE;
        GridViewPage = new PhotoMyGalleryGridViewFragment();
        GridViewPage.SetPhotoListData(UserSharedPhotoData);
        GridViewPage.SetUserBehaviorListener(PhotoMyGalleryFragment.this);
        addFragment(GridViewPage, "GridViewPage");
    }


    public void SwitchToShowPhotoFragment(int position) {
        SetSharedPhotoDrawableArrayFromCache();

        if (SharedPhotoIdArray.size() > SharedPhotoDrawableCacheArray.size()) {
            if (ShowPhotoArrayPositionMap.containsKey(position))
                position = ShowPhotoArrayPositionMap.get(position);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("photoposition", position);

        if (!SharedPhotoDrawableCacheArray.isEmpty()) {
            ShowPhotofragment = new ShowPhotoFragment();
            ShowPhotofragment.setArguments(bundle);
            ShowPhotofragment.SetPhotoDrawableArray(SharedPhotoDrawableCacheArray);
            ShowPhotofragment.SetPhotoDrawbleUrlAndIdArray(SharedPhotoBigUrlArray, SharedPhotoIdArray, mImageDM);
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.PhotoNewActivityLayout, ShowPhotofragment, "ShowPhotoFragment");
            transaction.commit();
        }

    }

    private void SetSharedPhotoDrawableArrayFromCache() {

        if (SharedPhotoIdArray.size() != 0) {
            SharedPhotoDrawableCacheArray.clear();
            int arrayposition = 0;
            int idposition = 0;

            for (String id : SharedPhotoIdArray) {
                Drawable img = mImageDM.GetMemoryCache().get(id);
                if (img != null) {
                    SharedPhotoDrawableCacheArray.add(img);
                    LOG.W(TAG, "idposition = " + idposition + " arrayposition = " + arrayposition);
                    ShowPhotoArrayPositionMap.put(idposition, arrayposition);
                    arrayposition++;
                }
                idposition++;
            }
        }
    }

    private IApiEventListener mOnGetSharedPhotoListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {

            if (isSuccess) {
                LOG.I(TAG, "OnGetSharedPhotoListener success");
                getSharedPhotos_outObj data = (getSharedPhotos_outObj) obj;
                data.dumpData();
                IsShowMountainIcon((int) data.getTotalPhotoCount());
                PhotosNum.setText(String.valueOf(data.getTotalPhotoCount()));

                cleanArrayList();
                mImageDM.ReSetAvatarFinishCount();
                mImageDM.ReSetPhotoFinishCount();

                boolean IsSuccess = db.updateSharedPhoto(UserId, SaveUserName,
                        UserAvatarUrl, data, true);
                LOG.I(TAG, "update database isSuccess = " + IsSuccess);

                UserSharedPhotoData = data.getPhotos();
                NextPagePhotoUrl = data.GetNextPageUrl();

                for (SharedPhotoData photodata : data.getPhotos()) {
                    SharedPhotoIdArray.add(photodata.id);
                    SharedPhotoBigUrlArray.add(photodata.url);
                }

                SwitchToListFragment();

            } else {
                LOG.E(TAG, "mOnGetSharedPhotoListener - failed to get conversation");
                PhotosNum.setText(String.valueOf(-1));
                mAct.showGeneralWarningDialog();
            }

            mAct.SetLastButtonEnable(true);
            SetGridandListButtonEnable(true);
        }
    };

    private IApiEventListener mOnCallUrlFromPaginatedResponseForSharePhotoListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {

            if (isSuccess) {
                LOG.I(TAG, "mOnCallUrlFromPaginatedResponseForSharePhotoListener success ");
                getSharedPhotos_outObj data = (getSharedPhotos_outObj) obj;

                int count = (int) data.getTotalPhotoCount();
                UserSharedPhotoData.addAll(data.getPhotos());
                NextPagePhotoUrl = data.GetNextPageUrl();


                if (count != 0) {

                    for (SharedPhotoData photodata : data.getPhotos()) {
                        SharedPhotoIdArray.add(photodata.id);
                        SharedPhotoBigUrlArray.add(photodata.url);
                    }

                    if (MyGalleryMode.equals(PhotoParameter.MYGALLERY_LISTVIEW_MODE)) {
                        ListViewPage.UpdateHistoryPhotos(data.getPhotos());
                    } else {
                        GridViewPage.UpdateHistoryPhotos(data.getPhotos());
                    }


                } else {


                }


            } else {
                LOG.E(TAG, "mOnCallUrlFromPaginatedResponseForSharePhotoListener - failed to get conversation");
                mAct.showGeneralWarningDialog();
            }

        }
    };

    private IApiEventListener mOnDeletePhotoListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                LOG.I(TAG, "mOnDeletePhotoListener - success");
                RightFrame.removeAllViews();
                cleanArrayList();

                ListViewPage = null;
                LoadingPhotoFromServer();

            } else {
                LOG.E(TAG, "mOnDeletePhotoListener - failed to get conversation");

                mAct.SetLastButtonEnable(true);
                SetGridandListButtonEnable(true);
                mAct.showGeneralWarningDialog();
            }

        }
    };

    private void cleanArrayList() {

        SharedPhotoDrawableCacheArray.clear();
        SharedPhotoIdArray.clear();
        SharedPhotoBigUrlArray.clear();
        ShowPhotoArrayPositionMap.clear();
        UserSharedPhotoData.clear();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        LOG.W(TAG, "onResuem");
        mImageDM.LoadImageFromServerForAvatar(UserAvatarUrl, UserId, UserId, null, UserImage, this);

        mAct.onDeletePhoto.addEventListener(mOnDeletePhotoListener);
        mAct.onGetSharedPhoto.addEventListener(mOnGetSharedPhotoListener);
        mAct.onCallUrlFromPaginatedResponseForSharePhoto.addEventListener(mOnCallUrlFromPaginatedResponseForSharePhotoListener);

    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        mAct.onDeletePhoto.removeEventListener(mOnDeletePhotoListener);
        mAct.onGetSharedPhoto.removeEventListener(mOnGetSharedPhotoListener);
        mAct.onCallUrlFromPaginatedResponseForSharePhoto.removeEventListener(mOnCallUrlFromPaginatedResponseForSharePhotoListener);
    }

    @Override
    public void onTaskCompleted() {
        // TODO Auto-generated method stub
        this.SaveUserImage = UserImage.getDrawable();
        SetGridandListButtonEnable(false);
        //LoadingPhotoFromServer();
        GetPhotoDataFromDataBase();
    }

    @Override
    public void RefreshScrollView() {
        // TODO Auto-generated method stub
        LOG.I(TAG, "RefreshScrollView");
        SetGridandListButtonEnable(false);
        LoadingPhotoFromServer();

    }

    @Override
    public void ClickPhoto(int position) {
        // TODO Auto-generated method stub
        SwitchToShowPhotoFragment(position);
    }

    @Override
    public void DeletePhoto(String targetphotoId) {
        // TODO Auto-generated method stub

        mAct.SetLastButtonEnable(false);
        SetGridandListButtonEnable(false);
        boolean Is = db.deletePhoto(UserId, targetphotoId); // delete photo from database cache
        LOG.I(TAG, "delete photo from database success is = " + Is);

        LOG.I(TAG, "when delete photo, is parent mode = " + mAct.IsMommyMode);
        //if (mAct.IsMommyMode) {
            //mAct.NSADeletePhoto(UserId, targetphotoId);
        //} else {
            mAct.DeleteOwnPhoto(targetphotoId);
        //}

    }

    @Override
    public void ScrollBottomLoading() {
        // TODO Auto-generated method stub
        LOG.W(TAG, "ScrollBottomLoading");
        LoadingNextPagePhoto(NextPagePhotoUrl);

    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();

        mImageDM.CancelTask();
        cleanArrayList();
        mAct.SetLastButtonEnable(true);
        SetGridandListButtonEnable(true);
        GridViewPage = null;
        ListViewPage = null;
    }

}
