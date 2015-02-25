package com.fuhu.nabiconnect.photo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.application.NabiconnectApplication;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.fragment.PhotoCameraFragment;
import com.fuhu.nabiconnect.photo.fragment.PhotoCameraGalleryFragment;
import com.fuhu.nabiconnect.photo.fragment.PhotoInBoxFragment;
import com.fuhu.nabiconnect.photo.fragment.PhotoMyGalleryFragment;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;
import com.fuhu.ndnslibsoutstructs.friends_outObj;

/**
 * @author Ricky
 */

public class PhotoActivity extends ApiBaseActivity {
    public final static String TAG = "PhotoNewActivity";

    public interface PhotoWidgetListener {
        void onGainIndex(int index);

        void onClickPhoto(int position);
    }

    public interface UserBehaviorListener {
        void RefreshScrollView();

        void ClickPhoto(int position);

        void DeletePhoto(String photoId);

        void ScrollBottomLoading();
    }

    public static final String INTENT_CONNECTION_DIALOG = "fuhu.action.nabiui.CONNECTWIFI";
    public static final String SAVE_MODE_INFO = "SaveModeInfo";
    public FragmentManager mFragmentManager = null;
    public FragmentTransaction transaction;
    private ImageDownLoadManager mImageDM;
    private ConnectivityManager m_Connectivity;

    protected ImageView NabiPhotoIcon;
    protected ImageButton HomeInboxBtn;
    protected ImageButton CameraBtn;
    protected ImageButton MyGalleryBtn;

    protected Context mCtx;
    public boolean IsMommyMode;
    private String m_LogonUserKey;
    private String UserId;
    private String UserName;
    private String UserAvatarUrl;
    private String ActionName = "";
    private int UserFriendsCount = -1;

    protected Display display;

    public int ScreenWidth;
    public int ScreenHeight;

    SharedPreferences mode;

    private PhotoInBoxFragment InboxFragment = null;
    private PhotoCameraFragment cameraFragment = null;
    private PhotoMyGalleryFragment mygalleryFragment = null;
    private PhotoCameraGalleryFragment cameragalleryFragment = null;

    private AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LOG.I(TAG, "onCreate ");

        mCtx = this;
        this.m_Connectivity = (ConnectivityManager) mCtx
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        setContentView(R.layout.photo_activity_main);
        mFragmentManager = getFragmentManager();

        // this action for Wings Challenge
        ActionName = getIntent().getComponent().getClassName();

        mode = this.getSharedPreferences(SAVE_MODE_INFO, Context.MODE_PRIVATE);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            IsMommyMode = extras.getBoolean(KEY_IS_MOMMY_MODE, false);
            m_LogonUserKey = extras.getString(KEY_LOGON_USER_KEY, "");
            mode.edit().putBoolean("MODE", IsMommyMode).commit();
            mode.edit().putString("LOGON_USER_KEY", m_LogonUserKey).commit();

        } else {
            IsMommyMode = false;
            mode.edit().putBoolean("MODE", IsMommyMode).commit();
            LOG.I(TAG, "No extras");
        }

        LOG.I(TAG, "set now mode = " + IsMommyMode);

        NabiconnectApplication mNabiconnectApplication = (NabiconnectApplication) mCtx.getApplicationContext();
        mImageDM = mNabiconnectApplication.mImageDM;  //for cache photo and avatar

        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        ScreenWidth = size.x;
        ScreenHeight = size.y;

        SetLastButtonBarViewAndClickListener();

        /**
         * hide all views until login returns
         */
        aq = new AQuery(PhotoActivity.this);
        hideAllView();

        CheckWifi();
        CheckMode();
        CheckModeIcon();
    }

    /*
     * ======================================================================
     * Check if Wifi is enabled
     * =======================================================================
     */
    protected boolean isWifiEnabled() {
        NetworkInfo info = this.m_Connectivity.getActiveNetworkInfo();
        if (info == null || !this.m_Connectivity.getBackgroundDataSetting()) {
            LOG.I(TAG, "No Wifi");
            return false;

        }
        int netType = info.getType();
        if (netType == ConnectivityManager.TYPE_WIFI) {
            LOG.I(TAG, "have Wifi");
            return info.isConnected();
        }
        return false;
    }

    protected void StartWifiSetDialog() {
        Intent intent = new Intent(INTENT_CONNECTION_DIALOG);
        sendBroadcast(intent);
    }

    public void CheckWifi() {
        boolean isWifi = isWifiEnabled(); // check WIFI
        LOG.I(TAG, "isWifi = " + isWifi);
        if (!isWifi)
            StartWifiSetDialog();
    }

    protected void CheckMode() {

        IsMommyMode = mode.getBoolean("MODE", false);
        m_LogonUserKey = mode.getString("LOGON_USER_KEY", "");

        if (IsMommyMode) {
            if (m_LogonUserKey != null && !m_LogonUserKey.isEmpty()) {
                LOG.V(TAG, "CheckMode() - there is logon userkey. Get data from cache.");
                UserData logonUser = getDatabaseAdapter().getUserData(
                        m_LogonUserKey);
                if (logonUser != null) {
                    LOG.V(TAG, "CheckMode() - use the cache as the logon data.");
                    loginUserSuccess(logonUser);
                } else {
                    LOG.V(TAG, "CheckMode() - there is no cache in database.");
                    loginAccountNoKid();
                }
            } else {
                LOG.V(TAG, "CheckMode() - there is no logon userkey in extra.");
                loginAccountNoKid();
            }
        } else
            loginAccount(); // for get user information
        LOG.V(TAG, "IsMommyMode = " + IsMommyMode);
    }

    private void CheckModeIcon() {
        if (IsMommyMode) {
            NabiPhotoIcon.setBackgroundResource(R.drawable.connect_bar_icon);
        } else {
            NabiPhotoIcon.setBackgroundResource(R.drawable.photo_nabiphoto);
        }
    }

    public ImageDownLoadManager getImageDownloadManager() {
        return this.mImageDM;
    }


    public void switchFragment(Fragment fragclass, String tag) {
        transaction = mFragmentManager.beginTransaction();
        transaction.addToBackStack(tag);
        transaction.replace(R.id.photo_fragment_container, fragclass, tag);
        transaction.commit();
    }

    public void switchFragment(Fragment fragclass, Bundle bundle, String tag) {
        fragclass.setArguments(bundle);
        transaction = mFragmentManager.beginTransaction();
        transaction.addToBackStack(tag);
        transaction.replace(R.id.photo_fragment_container, fragclass, tag);
        transaction.commit();
    }

    public String getUserId() {
        return this.UserId;
    }

    public String getUserName() {
        return this.UserName;
    }

    public String getUserAvatarUrl() {
        return this.UserAvatarUrl;
    }

    public int getUserFriendsCount() {
        return this.UserFriendsCount;
    }

    private boolean gotoCameraGalleryFragmentForWingsChallenge(String action) {
        if (action.equals(PhotoParameter.ACTIONAME_CAMERAGALLERY)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void accountNeedsCreate(String data) {
        super.accountNeedsCreate(data);
        callDialogForAskCreateUserName(IsMommyMode);
    }

    @Override
    public void loginUserSuccess(UserData data) {
        super.loginUserSuccess(data);
        showAllView();
        UserId = this.getCurrentUserData().userKey;
        UserName = this.getCurrentUserData().userName;
        UserAvatarUrl = this.getCurrentUserData().avatarURL;
        LOG.I(TAG, " action = " + ActionName);
        if (gotoCameraGalleryFragmentForWingsChallenge(ActionName)) {
            if (cameragalleryFragment == null) {
                CameraBtn.setSelected(true);
                SetLastButtonEnable(false);
                cameragalleryFragment = new PhotoCameraGalleryFragment();
                switchFragment(cameragalleryFragment, PhotoParameter.FRAGMENTTAG_CAMERAGALLERY);
            }
        } else {
            getFriendList(UserId);
            if (InboxFragment == null) {
                HomeInboxBtn.setSelected(true);
                InboxFragment = new PhotoInBoxFragment();
                switchFragment(InboxFragment, PhotoParameter.FRAGMENTTAG_INBOX);
            }
        }
    }

    @Override
    public void loginUserFailure(String data) {
        super.loginUserFailure(data);
        LOG.E(TAG, "login failure !!");
        showGeneralWarningDialog();
        finish();
    }

    @Override
    public void getFriendListSuccess(friends_outObj data) {
        super.getFriendListSuccess(data);
        LOG.V(TAG, "GetFriendListSuccess!!  total = " + data.getFriends().size());
        UserFriendsCount = data.getFriends().size();
    }


    @Override
    public void getFriendListFailure(String data) {
        super.getFriendListFailure(data);
        LOG.E(TAG, "get friendlist failure !!");
        showGeneralWarningDialog();
        finish();
    }

    public void SetLastButtonEnable(boolean is) {
        HomeInboxBtn.setEnabled(is);
        CameraBtn.setEnabled(is);
        MyGalleryBtn.setEnabled(is);
    }

    public void SetLastButtonSelect(boolean inbox, boolean camera, boolean mygallery) {
        HomeInboxBtn.setSelected(inbox);
        CameraBtn.setSelected(camera);
        MyGalleryBtn.setSelected(mygallery);
    }

    protected void SetLastButtonBarViewAndClickListener() {
        NabiPhotoIcon = (ImageView) findViewById(R.id.NabiPhotoIcon);
        HomeInboxBtn = (ImageButton) findViewById(R.id.HomeInboxButton);
        CameraBtn = (ImageButton) findViewById(R.id.CameraButton);
        MyGalleryBtn = (ImageButton) findViewById(R.id.MyGalleryButton);

        HomeInboxBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

				/*
                 * boolean nowmode = mode.getBoolean("MODE", false); String
				 * logonUserKey = mode.getString("LOGON_USER_KEY", "");
				 * LOG.V(TAG, " nowmode = " + nowmode); Intent intent = new
				 * Intent(mCtx, PhotoActivity.class);
				 * intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //close
				 * change activity animation intent.putExtra(KEY_IS_MOMMY_MODE,
				 * nowmode); intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
				 * startActivity(intent);
				 */

                HomeInboxBtn.setSelected(true);
                CameraBtn.setSelected(false);
                MyGalleryBtn.setSelected(false);

                LOG.W(TAG, "mFragmentManager.getBackStackEntryCount() = " + mFragmentManager.getBackStackEntryCount());
                if (mFragmentManager.getBackStackEntryCount() > 1) {
                    mFragmentManager.popBackStack(PhotoParameter.FRAGMENTTAG_INBOX, 0);
                    LOG.W(TAG, "inbox click and back stack");
                    cameraFragment = null;
                    mygalleryFragment = null;
                } else {
                    if (InboxFragment != null) {
                        LOG.I(TAG, "InboxFragment != null ");
                        if (InboxFragment.isAdded()) {
                            LOG.I(TAG, "InboxFragment isadded !! ");
                            return;
                        }
                        switchFragment(InboxFragment, PhotoParameter.FRAGMENTTAG_INBOX);
                    } else {
                        InboxFragment = new PhotoInBoxFragment();
                        switchFragment(InboxFragment, PhotoParameter.FRAGMENTTAG_INBOX);
                    }
                }
            }
        });

        CameraBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
				/*
                 * boolean nowmode = mode.getBoolean("MODE", false); String
				 * logonUserKey = mode.getString("LOGON_USER_KEY", "");
				 * LOG.V(TAG, " nowmode = " + nowmode); Intent intent = new
				 * Intent(mCtx, CameraActivity.class);
				 * intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //close
				 * change activity animation intent.putExtra(KEY_IS_MOMMY_MODE,
				 * nowmode); intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
				 * startActivity(intent);
				 */
                HomeInboxBtn.setSelected(false);
                CameraBtn.setSelected(true);
                MyGalleryBtn.setSelected(false);

                LOG.W(TAG, "mFragmentManager.getBackStackEntryCount() = " + mFragmentManager.getBackStackEntryCount());
                if (mFragmentManager.getBackStackEntryCount() > 2 && cameraFragment != null) {
                    mFragmentManager.popBackStack(PhotoParameter.FRAGMENTTAG_CAMERA, 0);
                    LOG.W(TAG, "CAMERA click and back stack");
                    mygalleryFragment = null;
                } else {
                    if (cameraFragment != null) {
                        LOG.I(TAG, "cameraFragment != null ");
                        if (cameraFragment.isAdded()) {
                            LOG.I(TAG, "cameraFragment isadded !! ");
                            return;
                        }
                        switchFragment(cameraFragment, PhotoParameter.FRAGMENTTAG_CAMERA);
                    } else {
                        cameraFragment = new PhotoCameraFragment();
                        switchFragment(cameraFragment, PhotoParameter.FRAGMENTTAG_CAMERA);
                    }
                }
            }
        });

        MyGalleryBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
				/*
                 * boolean nowmode = mode.getBoolean("MODE", false); String
				 * logonUserKey = mode.getString("LOGON_USER_KEY", "");
				 * LOG.V(TAG, " nowmode = " + nowmode);
				 * 
				 * Intent intent = new Intent(mCtx, MyGalleryActivity.class);
				 * intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //close
				 * change activity animation intent.putExtra(KEY_IS_MOMMY_MODE,
				 * nowmode); intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
				 * startActivity(intent);
				 */

                HomeInboxBtn.setSelected(false);
                CameraBtn.setSelected(false);
                MyGalleryBtn.setSelected(true);

                LOG.W(TAG, "mFragmentManager.getBackStackEntryCount() = " + mFragmentManager.getBackStackEntryCount());
                if (mFragmentManager.getBackStackEntryCount() > 2 && mygalleryFragment != null) {
                    mFragmentManager.popBackStack(PhotoParameter.FRAGMENTTAG_MYGALLERY, 0);
                    LOG.W(TAG, "MYGALLERY click and back stack");
                } else {
                    if (mygalleryFragment != null) {
                        LOG.I(TAG, "mygalleryFragment != null ");
                        if (mygalleryFragment.isAdded()) {
                            LOG.I(TAG, "mygalleryFragment isadded !! ");
                            return;
                        }
                        switchFragment(mygalleryFragment, PhotoParameter.FRAGMENTTAG_MYGALLERY);
                    } else {
                        mygalleryFragment = new PhotoMyGalleryFragment();
                        switchFragment(mygalleryFragment, PhotoParameter.FRAGMENTTAG_MYGALLERY);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        LOG.W(TAG, "click backbtn = " + mFragmentManager.getBackStackEntryCount());
        if (mFragmentManager.getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOG.I(TAG, "onDestroy");
        InboxFragment = null;
        cameraFragment = null;
        mygalleryFragment = null;
        //cleanCacheMap();
    }

    private void hideAllView() {
        aq.id(R.id.LastButtonBarWidget).invisible();
        aq.id(R.id.photo_fragment_container).invisible();
    }

    private void showAllView() {
        aq.id(R.id.LastButtonBarWidget).visible();
        aq.id(R.id.photo_fragment_container).visible();
    }
}
