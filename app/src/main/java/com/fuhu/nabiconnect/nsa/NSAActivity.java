package com.fuhu.nabiconnect.nsa;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.account.data.Kid;
import com.fuhu.data.FriendData;
import com.fuhu.data.InboxesData;
import com.fuhu.data.MailData;
import com.fuhu.data.OutboxesData;
import com.fuhu.data.ReceivedPhotoData;
import com.fuhu.data.UserData;
import com.fuhu.data.conversationData;
import com.fuhu.data.messageData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.nsa.fragment.FragmentChat;
import com.fuhu.nabiconnect.nsa.fragment.FragmentFriend;
import com.fuhu.nabiconnect.nsa.fragment.FragmentMail;
import com.fuhu.nabiconnect.nsa.fragment.FragmentMailViewer;
import com.fuhu.nabiconnect.nsa.fragment.FragmentMailbox;
import com.fuhu.nabiconnect.nsa.fragment.FragmentNSA;
import com.fuhu.nabiconnect.nsa.fragment.FragmentNSA.NSAEventListener;
import com.fuhu.nabiconnect.nsa.fragment.FragmentPhoto;
import com.fuhu.nabiconnect.nsa.util.ApiHelper;
import com.fuhu.nabiconnect.nsa.util.NSAPhotoUtil;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.nabiconnect.utils.LibraryUtils;
import com.fuhu.ndnslibsoutstructs.chatHistory_outObj;
import com.fuhu.ndnslibsoutstructs.chatPollMessage_outObj;
import com.fuhu.ndnslibsoutstructs.friends_outObj;
import com.fuhu.ndnslibsoutstructs.getInboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getMail_outObj;
import com.fuhu.ndnslibsoutstructs.getOutboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getOutgoingMail_outObj;
import com.fuhu.ndnslibsoutstructs.getSharedAndReceivedPhotos_outObj;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class NSAActivity extends ApiBaseActivity implements NSAEventListener {

    // TODO: remove before release
    final public static boolean DEBUG = false;
    final private static boolean DB_DUMP = false;
    public static int TOP_VIEW_HEIGHT;
    public static int MID_VIEW_HEIGHT;
    public static int BOT_VIEW_HEIGHT;
    /**
     * maps kid id to kid userkey
     */
    private static HashMap<String, String> mKidKeys = new HashMap<String, String>();
    final private String TAG = NSAActivity.class.getSimpleName();
    final private int LOGIN_WHAT = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            if (m.what == LOGIN_WHAT && this.hasMessages(LOGIN_WHAT)) {
                // do nothing and execute the latter one
            } else {
                Kid kid = (Kid) m.obj;
                childLogin(kid.getKidId(), false);
            }
        }
    };
    final private long LOGIN_DELAY = 1000;
    /**
     * used only in chat history delete
     */
    final private String KEY_PARENT_KEY = "parentKey";
    private int count = 0;
    private String mApiKey;
    private String mApiHost;
    private String mParentKey;
    private String mParentSessionKey;
    private AQuery aq;
    /**
     * used to hold last clicked tab
     */
    private ImageView iv_prev = null;
    /**
     * Fragments
     */
    private FragmentManager mFragmentManager;
    private FragmentFriend mFragmentFriend;
    private FragmentChat mFragmentChat;
    private FragmentMail mFragmentMail;
    private FragmentPhoto mFragmentPhoto;
    private View.OnClickListener menu_ocl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_debug) {
                try {
                    LOG.D(TAG, "versionName: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                    LOG.D(TAG, "versionCode: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
                    LOG.D(TAG, "server: " + getPackageManager().getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA).metaData.getString("server"));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                if (DB_DUMP) {
                    if (count < 9) {
                        count++;
                    } else {
                        DatabaseAdapter.getInstance(NSAActivity.this).dumpDatabase();
                        count = 0;
                    }
                }
                return;
            }

            int id = v.getId();
            if (iv_prev.getId() == id) {
                return;
            } else {
                switch (id) {
                    case R.id.iv_friend:
                        mFragmentFriend = null;
                        mFragmentFriend = new FragmentFriend();
                        switchFragment(mFragmentFriend);
                        break;
                    case R.id.iv_chat:
                        mFragmentChat = null;
                        mFragmentChat = new FragmentChat();
                        switchFragment(mFragmentChat);
                        break;
                    case R.id.iv_mail:
                        mFragmentMail = null;
                        mFragmentMail = new FragmentMail();
                        switchFragment(mFragmentMail);
                        break;
                    case R.id.iv_photo:
                        mFragmentPhoto = null;
                        mFragmentPhoto = new FragmentPhoto();
                        switchFragment(mFragmentPhoto);
                        break;
                }
                iv_prev.setSelected(false);
                v.setSelected(true);
                iv_prev = (ImageView) v;
            }
        }
    };
    private FragmentMailViewer mFragmentMailViewer;
    /**
     * local flag
     */
    private boolean onCreate = false;
    private DatabaseAdapter db;
    /**
     * blocked friend cache
     */
    private ArrayList<FriendData> mFriendBuffer = new ArrayList<FriendData>();
    private HashMap<String, HashMap<String, Boolean>> mBlockedMap = new HashMap<String, HashMap<String, Boolean>>();
    private FragmentMailbox mFragmentMailbox;
    private ArrayList<Kid> mFakeKidList = new ArrayList<Kid>();
    private Kid mFakeKid;
    private Comparator<FriendData> mFriendComparator = new Comparator<FriendData>() {

        @Override
        public int compare(FriendData lhs, FriendData rhs) {
            return lhs.userID.compareTo(rhs.userID);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BitmapAjaxCallback.clearCache();
        setContentView(R.layout.nsa_activity);
        getApiParams();
        aq = new AQuery(this);
        BitmapAjaxCallback.setNetworkLimit(FragmentNSA.NETWORK_LIMIT);
        iv_prev = aq.id(R.id.iv_friend).clicked(menu_ocl).getImageView();
        iv_prev.setSelected(true);

        aq.id(R.id.iv_chat).clicked(menu_ocl);
        aq.id(R.id.iv_mail).clicked(menu_ocl);
        aq.id(R.id.iv_photo).clicked(menu_ocl);
        aq.id(R.id.iv_debug).clicked(menu_ocl);

        // login parent
        db = DatabaseAdapter.getInstance(this);

        mFragmentManager = getFragmentManager();
        mFragmentMailViewer = (FragmentMailViewer) mFragmentManager.findFragmentById(R.id.ft_mail_viewer);
        mFragmentFriend = new FragmentFriend();
        mFragmentManager.beginTransaction().hide(mFragmentMailViewer).replace(R.id.fl_content, mFragmentFriend)
                .commit();
        onCreate = true;

        parentLogin();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && onCreate) {
            onCreate = false;
            int height = aq.id(R.id.rl_root).getView().getHeight();
            MID_VIEW_HEIGHT = (int) getResources().getDimension(R.dimen.nsa_mid_height);
            TOP_VIEW_HEIGHT = (int) ((height - MID_VIEW_HEIGHT) / 2);
            BOT_VIEW_HEIGHT = (int) (height - MID_VIEW_HEIGHT);
        }
        checkFragmentViewHeight();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_PARENT_KEY, mParentKey);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mFragmentMailViewer.isVisible()) {
            mFragmentManager.beginTransaction().hide(mFragmentMailViewer).commit();
            return;
        }
        super.onBackPressed();
    }

	/*
     * ======================================================================
	 * NSAEventListener methods
	 * ======================================================================
	 */

    private void switchFragment(Fragment fragment) {
        mFragmentManager.popBackStack();
        mFragmentManager.beginTransaction().replace(R.id.fl_content, fragment).commit();
    }

    public void switchFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.fl_content, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    public void showMailbox(FragmentMailbox fragment, boolean addToBackStack) {
        mFragmentMailbox = fragment;
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.fl_content, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    public void onMailDeleted(String mailId, boolean success) {
        if (!success) {
            showErrorDialog(false);
        }
        if (mFragmentMailbox != null && mFragmentMailbox.isVisible()) {
            long id = -1;
            try {
                id = Long.valueOf(mailId);
                mFragmentMailbox.deleteLocalMail(id, success);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public UserData getUserData() {
        if (NSAActivity.DEBUG) {
            return NSAUtil.getFakeUserData();
        } else {
            // return getCurrentUserData();
            return getNSACurrentUserData();
        }
    }

    @Override
    public ArrayList<Kid> getKidList() {
        mKidKeys.clear();
        if (NSAActivity.DEBUG) {
            if (mFakeKidList.isEmpty()) {
                mFakeKidList.addAll(NSAUtil.getFakeKidList());
                mFakeKid = mFakeKidList.get(0);
            }
            return mFakeKidList;
        } else {
            ArrayList<Kid> kids = m_KidAccountManager.getKidList();
            for (Kid k : kids) {
                mKidKeys.put(Long.toString(k.getKidId()), "");
            }
            return kids;
        }
    }

    @Override
    public Kid getCurrentKid() {
        if (NSAActivity.DEBUG) {
            return mFakeKid;
        } else {
            return m_KidAccountManager.getCurrentKid();
        }
    }

    @Override
    public void onKidChanged(final Kid kid) {
        if (NSAActivity.DEBUG) {
            mFakeKid = kid;
            refreshFragment();
        } else {
            m_KidAccountManager.setCurrentKid(kid);
            /**
             * delay login
             */
            // NSALogin(kid.getKidId());
            /**
             *
             */
            Message m = mHandler.obtainMessage(LOGIN_WHAT);
            m.obj = kid;
            mHandler.sendMessageDelayed(m, LOGIN_DELAY);
        }
    }

    @Override
    public String getKidUserKey(String KidId) {
        return mKidKeys.get(KidId);
    }

    @Override
    public boolean checkDataOwnership(String receivedKey) {
        UserData data = getUserData();
        Kid kid = getCurrentKid();
        if (data == null || kid == null) {
            return false;
        } else {
            return receivedKey.equals(data.userKey) && receivedKey.equals(getKidUserKey(Long.toString(kid.getKidId())));
        }
    }

    @Override
    public void sendFriendReq(String friendCode) {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            makeFriend(data.userKey, friendCode);
        } else {
            makeFriend(null, friendCode);
        }
    }

    @Override
    public void deleteFriend(String targetId) {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            removeFriend(data.userKey, targetId);
        } else if (NSAActivity.DEBUG) {
            removeFriend(null, null);
        }
    }

    @Override
    public void refreshFriendList() {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            getNSAFriendList(getNSACurrentUserData().userKey);
        } else if (DEBUG) {
            getFriendListSuccess(NSAUtil.getFakeFriendList());
        } else {
            super.onGetFriendList.raise(false);
            LOG.E(TAG, "user data null");
        }
    }

    @Override
    public void getFriendListSuccess(friends_outObj data) {
        updateBlockedMap(data);
        if (mFragmentFriend != null && mFragmentFriend.isVisible()) {
            onGetFriendList.raise(true, data);
        } else if (mFragmentChat != null && mFragmentChat.isVisible()) {
            onGetFriendList.raise(true, data);
            getConversationList();
        } else if (mFragmentMail != null && mFragmentMail.isVisible()) {
            loadInbox();
            loadOutbox();
        } else if (mFragmentPhoto != null && mFragmentPhoto.isVisible()) {
            loadAllPhoto(0, 0, FragmentNSA.PHOTO_POLL_LIMIT, false);
        }
    }

    /**
     * for development only
     */
    public void getFriendListSuccess(ArrayList<FriendData> data) {
        if (!DEBUG) {
            throw new RuntimeException("do not use this debug function");
        }
        HashMap<String, Boolean> map = mBlockedMap.get("dev");

        map = new HashMap<String, Boolean>();
        mBlockedMap.put("dev", map);

        mFriendBuffer.clear();
        mFriendBuffer.addAll(data);
        for (FriendData fd : mFriendBuffer) {
            map.put(fd.userID, fd.blocked);
        }
        if (mFragmentFriend != null && mFragmentFriend.isVisible()) {
            onGetFriendList.raise(false);
        } else if (mFragmentChat != null && mFragmentChat.isVisible()) {
            onGetFriendList.raise(false);
            getConversationList();
        } else if (mFragmentMail != null && mFragmentMail.isVisible()) {
            loadInbox();
            loadOutbox();
        } else if (mFragmentPhoto != null && mFragmentPhoto.isVisible()) {
            loadAllPhoto(0, 0, FragmentNSA.PHOTO_POLL_LIMIT, false);
        }
    }

    @Override
    public void getConversationList() {
        // NOTE: ========== new code ==========
        UserData data = getNSACurrentUserData();
        if (data != null) {
            getNSAConversation(data.userKey);
        } else if (DEBUG) {
            getChatPollMessage(null);
        }
        // ==============================
        // NOTE: ========== old code ==========
        // UserData data = getNSACurrentUserData();
        // if (data != null) {
        // getChatPollMessage(data.userKey, false);
        // } else if (NSAActivity.DEBUG) {
        // getChatPollMessage(null);
        // } else {
        // super.onGetChatPoll.raise(false);
        // }
    }

    @Override
    public void loadChatHistory(String conversationId, int limit, long since, long until, boolean showDialog) {
        // NOTE: new code
        UserData data = getNSACurrentUserData();
        if (data != null) {
            getNSAChatHistory(conversationId, data.userKey, limit, since, until, showDialog);
        } else if (DEBUG) {
            getNSAChatHistory(null, null, 0, 0, 0, false);
        }
        // ========================
        // NOTE: old code
        // getChatHistory(conversationId, limit, since, until, showDialog);
    }

    @Override
    public void delChatMessage(String childKey, String messageId, Handler handler) {
        ApiHelper helper = ApiHelper.getInstance(NSAActivity.this, handler);
        helper.setCredential(mParentSessionKey, mParentKey);
        helper.nsaDeleteChatMessage(childKey, messageId);
    }

    @Override
    public void loadInbox() {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            getNSAInbox(data.userKey);
        } else if (DEBUG) {
            getNSAInbox(null);
        }
    }

    @Override
    public void loadOutbox() {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            getNSAOutbox(data.userKey);
        } else if (DEBUG) {
            getNSAOutbox(null);
        }
    }

    @Override
    public void loadInboxMessage(String boxId) {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            getNSAMail(data.userKey, boxId);
        } else if (DEBUG) {
            getNSAMail(null, boxId);
        }
    }

    @Override
    public void loadOutboxMessage(String boxId) {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            getNSASentMail(data.userKey, boxId);
        } else if (DEBUG) {
            getNSASentMail(null, boxId);
        }
    }

    @Override
    public void deleteMailMessage(String boxId, String mailId, boolean isIncoming, Handler handler) {
        ApiHelper helper = ApiHelper.getInstance(NSAActivity.this, handler);
        ApiHelper.setParentCredential(mParentSessionKey, mParentKey);
        if (isIncoming) {
            helper.deleteInMail(boxId, mailId);
            // deleteInMailMessage(boxId, mailId);
        } else {
            helper.deleteOutMail(boxId, mailId);
            // deleteOutMailMessage(boxId, mailId);
        }
    }

    @Override
    public void showMailContent(String userKey, String mailId, String url) {
        mFragmentMailViewer.setData(userKey, mailId, url);
        mFragmentManager.beginTransaction().show(mFragmentMailViewer).commit();
    }

    /**
     * {receivedPhotos}
     */
    @Override
    public void removeReceivedPhoto(String photoId, Handler handler) {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            ApiHelper helper = ApiHelper.getInstance(NSAActivity.this, handler);
            helper.setCredential(mParentSessionKey, mParentKey);
            helper.nsaDeleteReceivedPhoto(photoId, getNSACurrentUserData().userKey);
        } else if (NSAActivity.DEBUG) {
            NSADeleteReceivedPhoto(null, null);
        }
    }

    /**
     * {delete} this works
     */
    @Override
    public void deleteSharedPhoto(String photoId, Handler handler) {
        UserData data = getNSACurrentUserData();
        if (data != null) {
            ApiHelper helper = ApiHelper.getInstance(NSAActivity.this, handler);
            helper.setCredential(mParentSessionKey, mParentKey);
            helper.nsaDeleteSharedPhoto(photoId, getNSACurrentUserData().userKey);
        } else if (NSAActivity.DEBUG) {
            NSADeletePhoto(null, null);
        }
    }

    @Override
    public void loadAllPhoto(long since, long until, long limit, boolean showDialog) {
        UserData data = getNSACurrentUserData();
        if (data != null) {
//            getAllPhoto(data.userKey, since, until, limit, showDialog);
            getNSAPhoto(data.userKey, since, until, limit, showDialog);
        } else if (DEBUG) {
            getNSAPhoto(null, 0, 0, 0, false);
            //getAllPhoto(null, 0, 0, 0, false);
        }
    }

    @Override
    public DatabaseAdapter getDB() {
        return getDatabaseAdapter();
    }

    @Override
    public void showErrorDialog(boolean shouldExit) {
        showGeneralWarningDialog();
        if (shouldExit) {
            finish();
        }
    }

    @Override
    public void unblockFriend(final FriendData fd) {
        final ProgressDialog dialog = new ProgressDialog(NSAActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.util_connecting));
        dialog.show();

        final HttpPut httpPut;
        httpPut = new HttpPut(mApiHost + "/user/" + getNSACurrentUserData().userKey + "/friendunblock/" + fd.userID);
        httpPut.addHeader("apiKey", mApiKey);
        httpPut.addHeader("Content-Type", "application/json");
        httpPut.addHeader("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        httpPut.addHeader("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        httpPut.addHeader("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        httpPut.addHeader("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        httpPut.addHeader("sessionKey", mParentSessionKey);
        httpPut.addHeader("userId", mParentKey);

        LOG.D(TAG, "url: " + httpPut.getURI().toASCIIString());
        for (Header h : httpPut.getAllHeaders()) {
            LOG.D(TAG, "header: " + h.getName() + " " + h.getValue());
        }

        JSONObject data = new JSONObject();
        StringEntity se = null;
        try {
            se = new StringEntity(data.toString());
            se.setContentType("application/json;charset=UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        httpPut.setEntity(se);
        final HttpClient client = new DefaultHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResponse response = client.execute(httpPut);
                    dialog.dismiss();
                    if (response != null) {
                        InputStream stream = response.getEntity().getContent();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(stream, writer, "utf-8");
                        String theString = writer.toString();
                        LOG.D(TAG, "response: " + theString);
                        try {
                            JSONObject obj = new JSONObject(theString);
                            if (obj.getString("status").equals("0")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setFriendBlockedState(getNSACurrentUserData().userKey, fd.userID, false);
                                        if (mFragmentFriend != null && mFragmentFriend.isVisible()) {
                                            mFragmentFriend.onFriendUnblocked(fd);
                                        }
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showErrorDialog(false);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showErrorDialog(false);
                                }
                            });
                        }
                    } else {
                        LOG.D(TAG, "response is null");
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * This method returns true when one of the following is
     * <strong>true</strong>:<br>
     * 1. Local child is blocked by this friend.<br>
     * 2. Local child has blocked this friend.<br>
     *
     * @param userKey   current userKey
     * @param friendKey
     * @return
     */
    @Override
    public boolean isFriendBlocked(String userKey, String friendKey) {
        if (friendKey == null || friendKey.isEmpty()) {
            return false;
        }
        if (userKey == null || userKey.isEmpty()) {
            UserData data = getUserData();
            if (data == null) {
                return false;
            } else {
                userKey = data.userKey;
            }
        }
        if (mBlockedMap.containsKey(userKey)) {
            Boolean b = mBlockedMap.get(userKey).get(friendKey);
            if (b != null) {
                return b;
            } else {
                return db.isFriendBlocked(userKey, friendKey);
            }
        } else {
            return false;
        }
    }

	/*
     * ======================================================================
	 * end NSAEventListener methods
	 * ======================================================================
	 */
    // @Override
    // public void loginUserSuccess(UserData data) {
    // super.loginUserSuccess(data);
    // if (data.kidID.equals("invalid")) {
    // mParentKey = data.userKey;
    // mParentSessionKey = data.sessionKey;
    // ApiHelper.setParentCredential(data.sessionKey, data.userKey);
    // } else {
    // ApiHelper.setChildCredential(data.sessionKey, data.userKey);
    // mKidKeys.put(data.kidID, data.userKey);
    // refreshFragment();
    // getDatabaseAdapter().updateUserData(data);
    // }
    // }

    // @Override
    // public void loginUserFailure(String data) {
    // super.loginUserFailure(data);
    // showGeneralWarningDialog();
    // finish();
    // }

    // @Override
    // public void accountNeedsCreate(String data) {
    // super.accountNeedsCreate(data);
    // callDialogForAskCreateUserName(true);
    // }

    @Override
    public void setFriendBlockedState(String userKey, String friendKey, boolean blocked) {
        if (!mBlockedMap.containsKey(userKey)) {
            return;
        }
        mBlockedMap.get(userKey).put(friendKey, blocked);
    }

    @Override
    public NabiNotificationManager getNotificationManager() {
        return super.getNabiNotificationManager();
    }

    @Override
    public void onParentLogin(UserData data) {
        // call super method to register gcm
        super.onParentLogin(data);
        mParentKey = data.userKey;
        mParentSessionKey = data.sessionKey;
        ApiHelper.setParentCredential(data.sessionKey, data.userKey);
    }

    @Override
    public void onParentLoginFail(String data) {
        // NOTE: super method does nothing
        showGeneralWarningDialog();
        finish();
    }

    @Override
    public void onParentAccountNotFound(String data) {
        // NOTE: super method does nothing
        callDialogForAskCreateUserName(true);
    }

    @Override
    public void onChildLogin(UserData data) {
        super.onChildLogin(data);
        // NOTE: call super to put data in memory
        ApiHelper.setChildCredential(data.sessionKey, data.userKey);
        mKidKeys.put(data.kidID, data.userKey);
        refreshFragment();
        getDatabaseAdapter().updateUserData(data);
    }

    @Override
    public void onChildLoginFail(String data) {
        super.onChildLoginFail(data);
        // NOTE: it is okay for child login to fail.
        // calling super sets nsa current user data to null
    }

    @Override
    public void onChildAccountNotFound(String data) {
        super.onChildAccountNotFound(data);
        // NOTE: it is okay for child login to fail.
        // calling super sets nsa current user data to null
    }

    private void updateBlockedMap(friends_outObj data) {
        HashMap<String, Boolean> map = mBlockedMap.get(data.mUserKey);
        if (map == null) {
            map = new HashMap<String, Boolean>();
            mBlockedMap.put(data.mUserKey, map);
        }

        mFriendBuffer.clear();
        mFriendBuffer.addAll(data.getFriends());
        for (FriendData fd : mFriendBuffer) {
            map.put(fd.userID, fd.blocked);
        }
    }

    /**
     * @param userKey - kid userkey
     */
    private void getNSAFriendList(final String userKey) {
        if (mParentKey == null || mParentKey.isEmpty()) {
            return;
        }
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
                LOG.D(TAG, "callback: " + url);
                if (data != null) {
                    try {
                        LOG.D(TAG, data.toString(5));
                        ArrayList<FriendData> list = new ArrayList<FriendData>();
                        JSONArray array = data.getJSONArray("friends");
                        int len = array.length();
                        for (int i = 0; i < len; i++) {
                            LOG.D(TAG, "adding: " + array.getJSONObject(i).toString(5));
                            list.add(new FriendData(array.getJSONObject(i)));
                        }
                        Collections.sort(list, mFriendComparator);
                        friends_outObj friendData = new friends_outObj();
                        friendData.mUserKey = userKey;
                        friendData.addFriendData(list);
                        getFriendListSuccess(friendData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showGeneralWarningDialog();
                    }
                } else {
                    showGeneralWarningDialog();
                }
            }
        };
        callback.url(mApiHost + "/user/" + userKey + "/friendsnsa/" + mParentKey);
        callback.header("apiKey", mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        callback.header("userId", mParentKey);

        LOG.D(TAG, callback.getUrl());
        LOG.D(TAG, "apiKey: " + mApiKey);
        LOG.D(TAG, "deviceKey: " + com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        LOG.D(TAG, "deviceType: " + com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        LOG.D(TAG, "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        LOG.D(TAG, "deviceEdition: " + com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        LOG.D(TAG, "sessionKey: " + mParentSessionKey);
        LOG.D(TAG, "userId: " + mParentKey);

        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    private void getNSAConversation(final String kidUserKey) {
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
                LOG.D(TAG, "callback: " + url);
                if (data != null) {
                    LOG.D(TAG, "data: " + data.toString());
                    try {
                        chatPollMessage_outObj obj = new chatPollMessage_outObj();
                        obj.mUserKey = kidUserKey;

                        // top level objects
                        JSONArray array = data.getJSONArray("conversations");
                        JSONObject object;
                        int len = array.length();
                        conversationData cd;

                        ArrayList<String> actors = new ArrayList<String>();
                        JSONArray actorsArray;

                        ArrayList<messageData> messages = new ArrayList<messageData>();
                        JSONArray messageArray;
                        // sub objects
                        JSONObject message;
                        messageData md;

                        for (int i = 0; i < len; i++) {
                            object = array.getJSONObject(i);
                            cd = new conversationData();
                            //
                            actorsArray = object.getJSONArray("actors");
                            int actorLen = actorsArray.length();
                            for (int k = 0; k < actorLen; k++) {
                                actors.add(actorsArray.getString(k));
                            }
                            //
                            messageArray = object.getJSONArray("msgs");
                            int messageLen = messageArray.length();
                            for (int m = 0; m < messageLen; m++) {
                                message = messageArray.getJSONObject(m);
                                md = new messageData();
                                md.setData(message.getString("id"), message.getString("s"), message.getLong("t"),
                                        message.getString("m"));
                                messages.add(md);
                            }
                            //
                            cd.setData(object.getString("cid"), object.getString("lm"), object.getLong("lt"),
                                    object.getInt("u"), actors, messages, object.getLong("latest_time"));

                            obj.AddConversation(cd);
                            actors.clear();
                            messages.clear();
                        }
                        onGetChatPoll.raise(true, obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onGetChatPoll.raise(false);
                    }
                } else {
                    // returned null data
                    onGetChatPoll.raise(false);
                }
            }
        };

        callback.url(mApiHost + "/chatuser/" + kidUserKey + "/nsaconversations");
        LOG.D(TAG, callback.getUrl());
        callback.header("apiKey", mApiKey);
        LOG.D(TAG, mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        LOG.D(TAG, com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        LOG.D(TAG, com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        LOG.D(TAG, "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        LOG.D(TAG, com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        LOG.D(TAG, mParentSessionKey);
        // callback.header("userId", mParentKey);
        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    private void getNSAChatHistory(final String conversationId, String userKey, int limit, long since, long until,
                                   boolean showDialog) {
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
                LOG.D(TAG, "callback: " + url);
                if (data != null) {
                    try {
                        LOG.D(TAG, "data: " + data.toString(5));
                        chatHistory_outObj historyData = new chatHistory_outObj();
                        historyData.mConversationId = conversationId;
                        if (!data.isNull("messages") && data.getString("status").equals("0")) {
                            JSONArray array = data.getJSONObject("messages").getJSONArray("data");
                            int len = array.length();
                            JSONObject chatMessage;
                            for (int i = 0; i < len; i++) {
                                chatMessage = array.getJSONObject(i);
                                historyData.AddMessage(chatMessage.getString("id"), chatMessage.getString("s"),
                                        chatMessage.getLong("t"), chatMessage.getString("m"));
                            }
                            onGetChatHistory.raise(true, historyData);
                        } else {
                            onGetChatHistory.raise(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onGetChatHistory.raise(false);
                    }
                } else {
                    // null response
                    onGetChatHistory.raise(false);
                }
            }
        };

        callback.url(mApiHost + "/nsaconversation/" + conversationId + "/messages?actor_id=" + userKey + "&until="
                + until + "&limit=" + limit);
        callback.header("apiKey", mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        callback.header("userId", mParentKey);

        LOG.D(TAG, callback.getUrl());
        LOG.D(TAG, "apiKey: " + mApiKey);
        LOG.D(TAG, "deviceKey: " + com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        LOG.D(TAG, "deviceType: " + com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        LOG.D(TAG, "androidVersionl: Android API Level: " + android.os.Build.VERSION.SDK_INT);
        LOG.D(TAG, "deviceEdition: " + com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        LOG.D(TAG, "sessionKey: " + mParentSessionKey);
        LOG.D(TAG, "userId: " + mParentKey);

        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    /**
     * @param userKey - kid userkey
     */
    private void getNSAInbox(final String userKey) {
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
                LOG.D(TAG, "callback: " + url);
                if (data != null) {
                    LOG.D(TAG, "data: " + data.toString());
                    try {
                        JSONArray array = data.getJSONArray("inboxes");
                        int len = array.length();

                        ArrayList<InboxesData> list = new ArrayList<InboxesData>();
                        JSONObject obj;
                        InboxesData inbox;

                        for (int i = 0; i < len; i++) {
                            obj = array.getJSONObject(i);
                            inbox = new InboxesData();
                            inbox.addInboxesData(obj.getString("inboxId"), obj.getString("userName"),
                                    obj.getLong("lastTimeOfNewReceive"), obj.getLong("newReceiveCount"),
                                    obj.getString("avatarImageUrl"), isFriendBlocked(userKey, obj.getString("inboxId")));
                            list.add(inbox);
                        }
                        getInboxes_outObj inboxData = new getInboxes_outObj();
                        inboxData.mUserKey = userKey;
                        inboxData.addData(list);
                        onGetMailInboxes.raise(true, inboxData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onGetMailInboxes.raise(false);
                    }
                } else {
                    onGetMailInboxes.raise(false);
                }
            }
        };

        callback.url(mApiHost + "/mailuser/" + userKey + "/NSAinbox");
        callback.header("apiKey", mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        callback.header("userId", mParentKey);
        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    private void getNSAMail(final String userKey, final String boxId) {
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
                LOG.D(TAG, "callback: " + url);
                if (data != null) {
                    LOG.D(TAG, "data: " + data.toString());
                    try {
                        if (data.getString("status").equals("0")) {
                            JSONArray array = data.getJSONArray("mail");
                            int len = array.length();

                            ArrayList<MailData> list = new ArrayList<MailData>();
                            JSONObject obj;
                            MailData mail;

                            for (int i = 0; i < len; i++) {
                                obj = array.getJSONObject(i);
                                mail = new MailData();
                                mail.addMailData(obj.getLong("mailId"), obj.getString("fileUrl"),
                                        getString(R.string.empty), obj.getLong("timeRecieved"), // server
                                        // side
                                        // typo,
                                        // leave
                                        // as
                                        // is
                                        obj.getString("size"), obj.getString("url_tn"), obj.getString("size_tn"));
                                list.add(mail);
                            }
                            getMail_outObj maildata = new getMail_outObj();
                            maildata.mBoxId = boxId;
                            maildata.addData(list);
                            onGetMailContent.raise(true, maildata);
                        } else {
                            onGetMailContent.raise(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onGetMailContent.raise(false);
                    }
                } else {
                    onGetMailContent.raise(false);
                }
            }
        };

        callback.url(mApiHost + "/mailuser/" + userKey + "/NSAinbox/" + boxId);
        callback.header("apiKey", mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        callback.header("userId", mParentKey);
        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    private void getNSAOutbox(final String userKey) {
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
                LOG.D(TAG, "callback: " + url);
                if (data != null) {
                    LOG.D(TAG, "data: " + data.toString());
                    try {
                        JSONArray array = data.getJSONArray("outboxes");
                        int len = array.length();

                        ArrayList<OutboxesData> list = new ArrayList<OutboxesData>();
                        JSONObject obj;
                        OutboxesData outbox;

                        for (int i = 0; i < len; i++) {
                            obj = array.getJSONObject(i);
                            outbox = new OutboxesData();
                            outbox.addOutboxesData(obj.getString("outboxId"), obj.getString("userName"),
                                    obj.getLong("lastTimeOfNewReceive"), obj.getString("avatarImageUrl"),
                                    isFriendBlocked(userKey, obj.getString("outboxId")));
                            list.add(outbox);
                        }
                        getOutboxes_outObj outboxData = new getOutboxes_outObj();
                        outboxData.mUserKey = userKey;
                        outboxData.addData(list);
                        onGetMailOutboxes.raise(true, outboxData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onGetMailOutboxes.raise(false);
                    }
                } else {
                    onGetMailOutboxes.raise(false);
                }
            }
        };

        callback.url(mApiHost + "/mailuser/" + userKey + "/NSAoutbox");
        callback.header("apiKey", mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        callback.header("userId", mParentKey);
        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    private void getNSASentMail(final String userKey, final String boxId) {
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject data, AjaxStatus status) {
                LOG.D(TAG, "callback: " + url);
                if (data != null) {
                    LOG.D(TAG, "data: " + data.toString());
                    try {
                        JSONArray array = data.getJSONArray("mail");
                        int len = array.length();

                        ArrayList<MailData> list = new ArrayList<MailData>();
                        JSONObject obj;
                        MailData mail;

                        for (int i = 0; i < len; i++) {
                            obj = array.getJSONObject(i);
                            mail = new MailData();
                            mail.addMailData(obj.getLong("mailId"), obj.getString("fileUrl"),
                                    getString(R.string.empty), obj.getLong("timeRecieved"), // server
                                    // side
                                    // typo,
                                    // leave
                                    // as
                                    // is
                                    obj.getString("size"), obj.getString("url_tn"), obj.getString("size_tn"));
                            list.add(mail);
                        }
                        getOutgoingMail_outObj maildata = new getOutgoingMail_outObj();
                        maildata.mBoxId = boxId;
                        maildata.addData(list);
                        onGetSentMailContent.raise(true, maildata);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onGetSentMailContent.raise(false);
                    }
                } else {
                    onGetSentMailContent.raise(false);
                }
            }
        };

        callback.url(mApiHost + "/mailuser/" + userKey + "/NSAoutbox/" + boxId);
        callback.header("apiKey", mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        callback.header("userId", mParentKey);
        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    private void getNSAPhoto(final String userKey, long since, long until, long limit, boolean showDialog) {
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String requestUrl, JSONObject data, AjaxStatus ajaxStatus) {
                LOG.D(TAG, "callback: " + requestUrl);
                if (data != null) {
                    LOG.D(TAG, "data: " + data.toString());

                    getSharedAndReceivedPhotos_outObj outObj = new getSharedAndReceivedPhotos_outObj();
                    outObj.mUserKey = userKey;
                    ArrayList<ReceivedPhotoData> pdta = new ArrayList<ReceivedPhotoData>();
                    try {
                        Iterator<?> keys2 = data.keys();
                        while (keys2.hasNext()) {
                            String key2 = (String) keys2.next();
                            if (key2.equals("data")) {
                                JSONArray jArray = data.getJSONArray("data");
                                for (int i = 0; i < jArray.length(); i++) {
                                    JSONObject achobj = jArray.getJSONObject(i);

                                    if(achobj.length() == 0){
                                        // empty object
                                        onGetAllPhoto.raise(true, userKey);
                                        return;
                                    }

                                    if (achobj.has("id") == false || achobj.has("createTime") == false) {
                                        onGetAllPhoto.raise(false, data);
                                        return;
                                    }
                                    ReceivedPhotoData pd = new ReceivedPhotoData();
                                    String id = "";
                                    String tl = "";
                                    String url = "";
                                    String fn = "";
                                    long fid = 0;
                                    long ct = 0;
                                    String aurl = "";
                                    String size = "";
                                    String tnurl = "";
                                    String tnsize = "";
                                    String status = "";
                                    if (achobj.has("id")) {
                                        id = achobj.getLong("id") + "";
                                    }
                                    if (achobj.has("title")) {
                                        tl = achobj.getString("title");
                                    }
                                    if (achobj.has("url")) {
                                        url = achobj.getString("url");
                                    }
                                    if (achobj.has("fromName")) {
                                        fn = achobj.getString("fromName");
                                    }
                                    if (achobj.has("fromId")) {
                                        fid = achobj.getLong("fromId");
                                    }
                                    if (achobj.has("createTime")) {
                                        ct = achobj.getLong("createTime");
                                    }
                                    if (achobj.has("fromAvatarUrl")) {
                                        aurl = achobj.getString("fromAvatarUrl");
                                    }
                                    if (achobj.has("size")) {
                                        size = achobj.getString("size");
                                    }
                                    if (achobj.has("url_tn")) {
                                        tnurl = achobj.getString("url_tn");
                                    }
                                    if (achobj.has("size_tn")) {
                                        tnsize = achobj.getString("size_tn");
                                    }
                                    if (achobj.has("status")) {
                                        status = achobj.getString("status");
                                    }
                                    pd.SetPhotoData(id, tl, url, fn, fid, ct, aurl, size, tnurl, tnsize, status);
                                    pdta.add(pd);
                                }
                            } else if (key2.equals("paging")) {
                                JSONObject jsonObject3 = (JSONObject) data.get("paging");
                                if (jsonObject3.has("previous") && jsonObject3.has("next")) {
                                    outObj.SetNextPageURL(jsonObject3.getString("next"));
                                    outObj.SetPreviousPageURL(jsonObject3.getString("previous"));
                                } else {
                                    onGetAllPhoto.raise(false, data);
                                    return;
                                }
                            }
                        }
                        outObj.addData(pdta);
                        onGetAllPhoto.raise(true, outObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        String url = mApiHost + "/photouser/" + userKey + "/photos?fields=";
        int count = 0;
        ArrayList<String> fields = NSAPhotoUtil.getAllPhotoQueryField();
        for (String s : fields) {
            url += s;
            if (++count < fields.size()) {
                url += ",";
            } else {
                if (since > 0 && until == 0) {
                    url += "&since=" + since;
                } else if (until > 0 && since == 0) {
                    url += "&until=" + until;
                }
                url += "&limit=" + limit;
            }
        }

        callback.url(url);
        callback.header("apiKey", mApiKey);
        callback.header("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
        callback.header("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
        callback.header("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
        callback.header("deviceEdition", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
        callback.header("sessionKey", mParentSessionKey);
        callback.header("userId", mParentKey);
        callback.method(AQuery.METHOD_GET);
        callback.type(JSONObject.class);
        aq.ajax(callback);
    }

    private void refreshFragment() {
        refreshFriendList();
    }

    private void checkFragmentViewHeight() {
        if (mFragmentFriend != null && mFragmentFriend.isVisible()) {
            mFragmentFriend.setViewHeight();
        } else if (mFragmentChat != null && mFragmentChat.isVisible()) {
            mFragmentChat.setViewHeight();
        } else if (mFragmentMail != null && mFragmentMail.isVisible()) {
            mFragmentMail.setViewHeight();
        } else if (mFragmentPhoto != null && mFragmentPhoto.isVisible()) {
            mFragmentPhoto.setViewHeight();
        }
    }

    /**
     * called only once in onCreate()
     *
     * @return
     */
    private void getApiParams() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData.getString("server").contains("prod")) {
                mApiKey = LibraryUtils.PRODUCTION_NSA_API_KEY;
                mApiHost = "http://" + LibraryUtils.PRODUCTION_BASE_URL;
            } else {
                mApiKey = LibraryUtils.STAGING_NSA_API_KEY;
                mApiHost = "http://" + LibraryUtils.STAGING_BASE_URL;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            mApiKey = LibraryUtils.STAGING_NSA_API_KEY;
            mApiHost = "http://" + LibraryUtils.STAGING_BASE_URL;
        }
    }
}