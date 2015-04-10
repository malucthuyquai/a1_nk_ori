package com.fuhu.nabiconnect.nsa.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.account.data.Kid;
import com.fuhu.data.FriendData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.friend.dialog.AddFriendDialog;
import com.fuhu.nabiconnect.friend.dialog.BlockedDialog;
import com.fuhu.nabiconnect.friend.dialog.FriendRequestSent;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.nsa.NSAActivity;
import com.fuhu.nabiconnect.nsa.util.ImageAdapter;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.nsa.view.BlockedFriendConfirmDialog;
import com.fuhu.nabiconnect.nsa.view.BlockedFriendDialog;
import com.fuhu.nabiconnect.nsa.view.NSAGallery;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.friends_outObj;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMSenderEventCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class FragmentFriend extends FragmentNSA {

    final private String TAG = FragmentFriend.class.getSimpleName();

    private AQuery aq;
    private ScrollView sv_root;
    private RelativeLayout rl_gallery;
    private RelativeLayout rl_mid;

    /**
     * Gallery related
     */
    private NSAGallery mGlKids;
    private TextView mTvName;
    private ArrayList<Kid> mKids = new ArrayList<Kid>();
    private ImageAdapter mImageAdapter;
    private Bitmap mDefaultAvatar;

    /**
     * Friend related
     */
    private GridView mGridView;
    private ArrayList<FriendData> mFriends = new ArrayList<FriendData>();
    private ArrayList<FriendData> mAllFriends = new ArrayList<FriendData>();
    private ArrayList<FriendData> mBlockedFriends = new ArrayList<FriendData>();
    private GridViewAdapter mGridViewAdapter;
    private HashSet<String> mDeleteFlag = new HashSet<String>();

    /**
     * internal references
     */
    private NSAActivity mActivity;
    private AddFriendDialog mFriendDialog;
    private FriendRequestSent mRequestSentDialog;

    private DatabaseAdapter db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDeleteFlag.clear();
        return inflater.inflate(R.layout.nsa_fragment_friend, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        aq = new AQuery(getActivity());
        db = mCallback.getDB();
        sv_root = (ScrollView) aq.id(R.id.sv_root).getView();
        rl_gallery = (RelativeLayout) aq.id(R.id.rl_gallery).getView();
        rl_mid = (RelativeLayout) aq.id(R.id.rl_mid).getView();

        // set mid TextView
        NSAUtil.setTypeface(getActivity(), aq.id(R.id.tv_mid).getTextView(), getString(R.string.roboto_bold));
        mTvName = aq.id(R.id.tv_name).getTextView();
        NSAUtil.setTypeface(getActivity(), mTvName, getString(R.string.roboto_light));

        // init kid select Gallery
        mGlKids = (NSAGallery) getView().findViewById(R.id.gl_kids);
        mImageAdapter = new ImageAdapter(getActivity(), mKids);
        mGlKids.setAdapter(mImageAdapter);
        mGlKids.setOnItemSelectedListener(oisl);

        // register kid select buttons
        aq.id(R.id.iv_left).clicked(kid_ocl);
        aq.id(R.id.iv_right).clicked(kid_ocl);

        // init friend GridView
        mGridView = (GridView) getView().findViewById(R.id.gv_friend);
        mGridViewAdapter = new GridViewAdapter(getActivity(), 0, mFriends);
        mGridView.setAdapter(mGridViewAdapter);
        mGridView.setOnTouchListener(list_otl);

        // init add friend dialog
        mFriendDialog = new AddFriendDialog(getActivity());
        mFriendDialog.setIsForAccept(false);
        mFriendDialog.addButtonListener(dialog_ocl);
        mFriendDialog.setOnDismissListener(dialog_odl);

        aq.id(R.id.iv_add_friend).clicked(add_ocl);
        aq.id(R.id.iv_blocked_friend).clicked(block_ocl);
        mActivity = (NSAActivity) getActivity();
        mDefaultAvatar = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.chat_avatar_default), 316, 316, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.onGetFriendList.addEventListener(onFriendListReceived);
        mActivity.onRemoveFriend.addEventListener(onFriendDeleted);
        mActivity.onMakeFriend.addEventListener(onFriendRequestSent);
        mKids.clear();
        mKids.addAll(mCallback.getKidList());
        mImageAdapter.notifyDataSetChanged();
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            long kidId = bundle.getLong("KidId");
            if (kidId > 0) {
                // from widget
                int idx = -1;
                for (int i = 0; i < mKids.size(); i++) {
                    Kid kid = mKids.get(i);
                    if (kid.getKidId() == kidId) {
                        idx = i;
                        break;
                    }
                }
                if (idx != -1) {
                    mGlKids.setSelection(idx);
                } else {
                    mGlKids.setSelection(getKidIdx(mKids, mCallback.getCurrentKid()));
                }
            }
        } else {
            mGlKids.setSelection(getKidIdx(mKids, mCallback.getCurrentKid()));
            oisl.onItemSelected(mGlKids, null, getKidIdx(mKids, mCallback.getCurrentKid()), 0);
        }
        setViewHeight();
        mRefreshHandler.sendEmptyMessageDelayed(FragmentNSA.REFRESH_WHAT, FragmentNSA.REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        mFriends.clear();
        mDeleteFlag.clear();
        mAllFriends.clear();
        mBlockedFriends.clear();
        mGridViewAdapter.notifyDataSetChanged();

        mActivity.onGetFriendList.removeEventListener(onFriendListReceived);
        mActivity.onRemoveFriend.removeEventListener(onFriendDeleted);
        mActivity.onMakeFriend.removeEventListener(onFriendRequestSent);
        sv_root.setScrollY(0);
        mRefreshHandler.removeMessages(FragmentNSA.REFRESH_WHAT);
        super.onPause();
    }

    private IApiEventListener onFriendListReceived = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            int y = sv_root.getScrollY();
            if (isSuccess) {
                friends_outObj updatedList = (friends_outObj) obj;
                db.updateNSAFriendList(updatedList);
                if (mCallback.checkDataOwnership(updatedList.mUserKey)) {
                    if (aq.id(R.id.iv_add_friend).getView().getVisibility() == View.VISIBLE) {
                        // don't animate
                    } else {
                        aq.id(R.id.iv_add_friend).animate(R.anim.fade_in).visible();
                    }
                    if (aq.id(R.id.iv_blocked_friend).getView().getVisibility() == View.VISIBLE) {
                        // don't animate
                    } else {
                        aq.id(R.id.iv_blocked_friend).animate(R.anim.fade_in).visible();
                    }
                    mAllFriends.clear();
                    mAllFriends.addAll(updatedList.getFriends());
                    mFriends.clear();
                    mBlockedFriends.clear();
                    for (FriendData fd : mAllFriends) {
                        mFriends.add(fd);
                        if (fd.blocked && fd.deleted) {
                            // deleted | blocked by us, put contact in blocked
                            // list
                            mBlockedFriends.add(fd);
                        }
                    }
                    Collections.sort(mFriends, mFriendComparator);
                    // move parent to first spot
                    moveParentToFront(mFriends);
                }
            } else if (NSAActivity.DEBUG) {
                mFriends.addAll(NSAUtil.getFakeFriendList());
            } else {
                // TODO: friend list failed
                LOG.D(TAG, "friend list fail");
                if (mCallback.getUserData() != null) {
                    mCallback.refreshFriendList();
                }
            }
            mGridViewAdapter.notifyDataSetChanged();
            sv_root.setScrollY(y);
        }
    };

    private IApiEventListener onFriendDeleted = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                String userKey = getUserKey();
                mCallback.refreshFriendList();
                mDeleteFlag.remove(mDeleteFriendId);
                db.blockFriend(userKey, mDeleteFriendId);
                findFriendById(mDeleteFriendId).blocked = true;
                mGridViewAdapter.notifyDataSetChanged();
                mDeleteFriendId = "";
                mCallback.setFriendBlockedState(userKey, mDeleteFriendId, true);
            } else if (NSAActivity.DEBUG) {
                // removeFriend(mDeleteFriendId);
                mCallback.refreshFriendList();
                mDeleteFlag.remove(mDeleteFriendId);
                mDeleteFriendId = "";
            } else {
                mCallback.showErrorDialog(false);
                // TODO: notify delete failed
                LOG.D(TAG, "friend delete failed");
            }
        }
    };

    private IApiEventListener onFriendRequestSent = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                // notify GCM server
                NabiNotificationManager notificationManager = mCallback.getNotificationManager();
                notificationManager.notifyServerByFriendCode(mInputFriendCode, mCallback.getUserData().userName,
                        getString(R.string.notification_friend_description),
                        NabiNotificationManager.APPLICATION_NAME_FRIEND, new GCMSenderEventCallback() {

                            @Override
                            public void onSendMessageSuccess() {
                                LOG.I(TAG, "gcm send message success");
                            }

                            @Override
                            public void onMessgaeSendingError(int errorCode) {
                                LOG.E(TAG, "gcm send message fail with code: " + errorCode);
                            }
                        });

                // dismiss dialog
                mFriendDialog.dismiss();
                mRequestSentDialog = new FriendRequestSent(mActivity);
                mRequestSentDialog.setCancelable(false);
                mRequestSentDialog.addButtonListener(new IButtonClickListener() {
                    @Override
                    public void onButtonClicked(int buttonId, String viewName, Object[] args) {
                        if (buttonId == FriendRequestSent.OK_BUTTON_ID) {
                        }
                        mRequestSentDialog.dismiss();
                    }
                });
                mRequestSentDialog.show();
            } else {
                try {
                    JSONObject jobj = new JSONObject(obj.toString());
                    if (jobj.getString("status").equals("8085")) {
                        // on block list
                        mFriendDialog.dismiss();
                        new BlockedDialog(getActivity()).show();
                    } else if (jobj.getString("status").equals("8043")) {
                        // already sent
                        mFriendDialog.setRequestAlreadySent(true);
                    } else {
                        // invalid friend code
                        mFriendDialog.setIsInvalid(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    LOG.V(TAG, "m_MakeFriendEventListener - failed to make friend");
                    mFriendDialog.setIsInvalid(true);
                }
            }
        }
    };

    private class GridViewAdapter extends ArrayAdapter<FriendData> {
        private LayoutInflater inflater;
        private AQuery aq;

        private ColorMatrixColorFilter blackAndWhiteFilter;
        private ColorMatrixColorFilter originalFilter;

        public GridViewAdapter(Context context, int textViewResourceId, List<FriendData> objects) {
            super(context, textViewResourceId, objects);
            inflater = LayoutInflater.from(context);
            aq = new AQuery(context);
            ColorMatrix matrix = new ColorMatrix();
            matrix = new ColorMatrix();
            matrix.setSaturation(0);
            blackAndWhiteFilter = new ColorMatrixColorFilter(matrix);
            matrix.setSaturation(1);
            originalFilter = new ColorMatrixColorFilter(matrix);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.nsa_item_friend, parent, false);
                holder = new ViewHolder();
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                holder.iv_delete.setOnClickListener(delete_ocl);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final FriendData data = getItem(position);
            // load avatar
            final String avatarUrl = data.AvatarUrl.replace("https", "http");
            aq.id(holder.iv_avatar).tag(AQuery.TAG_1, avatarUrl);
            if (BitmapAjaxCallback.getMemoryCached(data.userID, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
                holder.iv_avatar.setImageBitmap(BitmapAjaxCallback.getMemoryCached(data.userID,
                        FragmentNSA.AVATAR_TARGET_WIDTH));
            } else {
                final String userKey = getUserKey();
                // fetch newest from network
                final Bitmap cache = db.getFriendAvatar(userKey, data.userID, FragmentNSA.AVATAR_TARGET_WIDTH);
                final boolean hasCache = cache != null;
                BitmapAjaxCallback callback = new BitmapAjaxCallback() {
                    @Override
                    protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
                        if (bmp != null) {
                            // save once download is finished
                            db.saveAvatarAsync(userKey, data.userID, bmp);
                            this.memPut(data.userID, bmp);
                            if (imageView.getTag(AQuery.TAG_1).equals(avatarUrl)) {
                                if (hasCache) {
                                    imageView.setImageBitmap(bmp);
                                } else {
                                    Drawable[] drawables = new Drawable[2];
                                    drawables[0] = new BitmapDrawable(getResources(), mDefaultAvatar);
                                    drawables[1] = new BitmapDrawable(getResources(), bmp);
                                    TransitionDrawable td = new TransitionDrawable(drawables);
                                    td.setCrossFadeEnabled(true);
                                    td.startTransition(300);
                                    imageView.setImageDrawable(td);
                                }
                            }
                        } else {
                            // fallback
                            if (hasCache) {
                                imageView.setImageBitmap(cache);
                            } else {
                                imageView.setImageBitmap(mDefaultAvatar);
                            }
                        }
                    }
                };
                callback.memCache(false);
                callback.fileCache(false);
                callback.targetWidth(FragmentNSA.AVATAR_TARGET_WIDTH);
                callback.url(avatarUrl);
                callback.imageView(aq.getImageView());
                callback.preset(hasCache ? cache : mDefaultAvatar);
                aq.image(callback);
            }

            if (data.blocked || data.deleted) {
                holder.iv_avatar.setColorFilter(blackAndWhiteFilter);
            } else {
                holder.iv_avatar.setColorFilter(originalFilter);
            }

            holder.tv_name.setText(data.userName);
            NSAUtil.setTypeface(getActivity(), holder.tv_name, getString(R.string.roboto_light));
            holder.iv_delete.setTag(data);
            holder.iv_delete.setSelected(mDeleteFlag.contains(data.userID));
            holder.iv_delete
                    .setVisibility(data.relationship == FriendData.FRIEND && (!data.blocked && !data.deleted) ? View.VISIBLE
                            : View.INVISIBLE);
            return convertView;
        }
    }

    private String mDeleteFriendId;
    private View.OnClickListener delete_ocl = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            FriendData data = (FriendData) v.getTag();
            if (v.isSelected()) {
                // already pressed once, ok to delete
                mDeleteFriendId = data.userID;
                mCallback.deleteFriend(mDeleteFriendId);
            } else {
                // not yet selected, just mark item as delete pending
                v.setSelected(true);
                mDeleteFlag.add(data.userID);
            }
        }
    };

    private class ViewHolder {
        public ImageView iv_avatar, iv_delete;
        public TextView tv_name;
    }

    /**
     * touch event callbacks
     */

    private View.OnTouchListener list_otl = new View.OnTouchListener() {
        int[] loc = new int[2];
        float lastY;
        float deltaY = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    deltaY = event.getY() - lastY;
                    lastY = event.getY();
                    break;
            }

            rl_mid.getLocationOnScreen(loc);
            if (loc[1] <= 0) {
                sv_root.requestDisallowInterceptTouchEvent(true);
                return mGridView.onTouchEvent(event);
            } else {
                if (deltaY > 0) {
                    // want to scroll down list view
                    sv_root.requestDisallowInterceptTouchEvent(true);
                    return mGridView.onTouchEvent(event);
                }
                sv_root.requestDisallowInterceptTouchEvent(false);
                return false;
            }
        }
    };

    private View.OnClickListener kid_ocl = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int idx = mImageAdapter.getSelectedIdx();
            switch (v.getId()) {
                case R.id.iv_left:
                    if (idx == 0) {
                        return;
                    } else {
                        idx--;
                    }
                    break;
                case R.id.iv_right:
                    if (idx == mImageAdapter.getCount() - 1) {
                        return;
                    } else {
                        idx++;
                    }
                    break;
            }
            mGlKids.setSelection(idx);
            mCallback.onKidChanged(mKids.get(idx));
            mImageAdapter.notifyDataSetChanged();
        }
    };

    private View.OnClickListener add_ocl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFriendDialog.show();
        }
    };

    private BlockedFriendDialog mBlockedDialog;
    private View.OnClickListener block_ocl = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mBlockedDialog = new BlockedFriendDialog(getActivity(), unblock_ocl);
            mBlockedDialog.setFriendList(mBlockedFriends);
            mBlockedDialog.show();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(mBlockedDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            mBlockedDialog.getWindow().setAttributes(lp);
        }
    };

    public void onFriendUnblocked(FriendData fd) {
        mBlockedDialog.removeFriend(fd);
        mBlockedFriends.remove(fd);
        fd.blocked = false;
        fd.deleted = false;
        mGridViewAdapter.notifyDataSetChanged();
        mCallback.setFriendBlockedState(getUserKey(), fd.userID, false);
        db.unblockFriend(getUserKey(), fd.userID);
    }

    private BlockedFriendConfirmDialog mConfirmDialog;
    private View.OnClickListener unblock_ocl = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            FriendData fd = (FriendData) v.getTag();
            switch (v.getId()) {
                case R.id.btn_unblock:
                    mConfirmDialog = new BlockedFriendConfirmDialog(getActivity(), this, fd);
                    mConfirmDialog.show();
                    break;
                case R.id.btn_confirm:
                    mConfirmDialog.dismiss();
                    mCallback.unblockFriend(fd);
                    break;
            }
        }
    };

    private String mInputFriendCode = "";
    private IButtonClickListener dialog_ocl = new IButtonClickListener() {
        @Override
        public void onButtonClicked(int buttonId, String viewName, Object[] args) {
            switch (buttonId) {
                case AddFriendDialog.YES_BUTTON_ID:
                    mInputFriendCode = (String) args[0];
                    if (mFriendDialog.isForAccept()) {
                        // accept friend request
                    } else {
                        // send friend request
                        mCallback.sendFriendReq(mInputFriendCode);
                    }
                    break;
                case AddFriendDialog.CANCEL_BUTTON_ID:
                    mFriendDialog.dismiss();
                    break;
            }
        }
    };

    private DialogInterface.OnDismissListener dialog_odl = new DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((AddFriendDialog) dialog).clearFriendCode();
        }
    };

    private AdapterView.OnItemSelectedListener oisl = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            aq.id(R.id.iv_add_friend).invisible();
            aq.id(R.id.iv_blocked_friend).invisible();
            mFriends.clear();
            mDeleteFlag.clear();
            String userKey = getUserKey();
            mAllFriends.clear();
            mAllFriends.addAll(db.getNSAFriendList(userKey));
            mFriends.clear();
            mBlockedFriends.clear();
            // NOTE: need not to extract blocked friends since blocked friends
            // dialog can't be opened until login succeeds.
            for (FriendData fd : mAllFriends) {
                mFriends.add(fd);
                // if (fd.blocked) {
                // mBlockedFriends.add(fd);
                // }
            }
            Collections.sort(mFriends, mFriendComparator);
            // move parent to first spot
            moveParentToFront(mFriends);
            int y = sv_root.getScrollY();
            mGridViewAdapter.notifyDataSetChanged();
            sv_root.setScrollY(y);
            mImageAdapter.setSelectItem(position);
            mGlKids.setSelection(position);
            mTvName.setText(mKids.get(position).getkidName());
            mCallback.onKidChanged(mKids.get(position));
            mImageAdapter.notifyDataSetChanged();
            // sv_root.setScrollY(0);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    public void setViewHeight() {
        if (rl_gallery.getHeight() == 0) {
            aq.id(rl_gallery).height(NSAActivity.TOP_VIEW_HEIGHT, false);
        }
        if (mGridView.getHeight() == 0) {
            aq.id(mGridView).height(NSAActivity.BOT_VIEW_HEIGHT, false);
        }
        sv_root.setScrollY(0);
    }

    /**
     * @param friendKey
     * @return FriendData of matching userKey in mFriends.<br>
     * null if not found.
     */
    private FriendData findFriendById(String friendKey) {
        for (FriendData fd : mFriends) {
            if (fd.userID.equals(friendKey)) {
                return fd;
            }
        }
        return null;
    }

    private String getUserKey() {
        return db.getUserKey(((Kid) mGlKids.getSelectedItem()).getKidId());
    }

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FragmentNSA.REFRESH_WHAT) {
                this.removeMessages(FragmentNSA.REFRESH_WHAT);
                if (mFriends.size() > 0) {
                    // currently have friend in display
                    mCallback.refreshFriendList();
                }
                this.sendEmptyMessageDelayed(FragmentNSA.REFRESH_WHAT, FragmentNSA.REFRESH_INTERVAL);
            }
        }
    };

    private Comparator<FriendData> mFriendComparator = new Comparator<FriendData>() {
        @Override
        public int compare(FriendData lhs, FriendData rhs) {
            return lhs.userName.compareToIgnoreCase(rhs.userName);
        }
    };

    private void moveParentToFront(ArrayList<FriendData> friendList) {
        FriendData temp = null;
        for (FriendData fd : friendList) {
            if (fd.isParent && fd.relationship == FriendData.PARENT) {
                temp = fd;
                break;
            }
        }
        if (temp != null) {
            friendList.remove(temp);
            friendList.add(0, temp);
        }
        mGridViewAdapter.notifyDataSetChanged();
    }
}