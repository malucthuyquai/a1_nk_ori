package com.fuhu.nabiconnect.nsa.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.account.data.Kid;
import com.fuhu.data.FriendData;
import com.fuhu.data.conversationData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.nsa.NSAActivity;
import com.fuhu.nabiconnect.nsa.util.ImageAdapter;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.nsa.view.NSAGallery;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.chatPollMessage_outObj;
import com.fuhu.ndnslibsoutstructs.friends_outObj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FragmentChat extends FragmentNSA {
    final private String TAG = FragmentFriend.class.getSimpleName();

    /**
     * view
     */
    private AQuery aq;
    private ScrollView sv_root;
    private RelativeLayout rl_gallery;
    private RelativeLayout rl_mid;
    private TextView mTvName;

    /**
     * Gallery related
     */
    private NSAGallery mGlKids;
    private ArrayList<Kid> mKids = new ArrayList<Kid>();
    private ImageAdapter mImageAdapter;
    private Bitmap mDefaultAvatar;

    private ListView mLvChat;
    private ArrayList<FriendData> mFriends = new ArrayList<FriendData>();
    private ArrayList<conversationData> mConversationList = new ArrayList<conversationData>();
    private ChatAdapter mChatAdapter;

    private NSAActivity mActivity;
    private FragmentChatHistory mFragmentHistory;
    private Bundle mBundleHistory = new Bundle();

    private DatabaseAdapter db;

//    {
//        FragmentNSA.TRACKING_NAME = TrackingInfo.NSA_CHAT;
//    }


    @Override
    public String getTrack() {
        return "chat_home";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nsa_fragment_chat, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        aq = new AQuery(getActivity());
        db = mCallback.getDB();
        sv_root = (ScrollView) aq.id(R.id.sv_root).getView();
        rl_gallery = (RelativeLayout) aq.id(R.id.rl_gallery).getView();
        rl_mid = (RelativeLayout) aq.id(R.id.rl_mid).getView();

        NSAUtil.setTypeface(getActivity(), aq.id(R.id.tv_mid).getTextView(), getString(R.string.roboto_bold));

        mTvName = aq.id(R.id.tv_name).getTextView();
        NSAUtil.setTypeface(getActivity(), mTvName, getString(R.string.roboto_light));

        mGlKids = (NSAGallery) getView().findViewById(R.id.gl_kids);
        mImageAdapter = new ImageAdapter(getActivity(), mKids);
        mGlKids.setAdapter(mImageAdapter);
        mGlKids.setOnItemSelectedListener(oisl);

        mLvChat = aq.id(R.id.lv_chat).getListView();
        mChatAdapter = new ChatAdapter(getActivity(), 0, mFriends);
        mLvChat.setAdapter(mChatAdapter);
        mLvChat.setOnItemClickListener(oicl);
        mLvChat.setOnTouchListener(list_otl);

        aq.id(R.id.iv_left).clicked(kid_ocl);
        aq.id(R.id.iv_right).clicked(kid_ocl);

        mActivity = (NSAActivity) getActivity();
        mDefaultAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.chat_avatar_default);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.onGetFriendList.addEventListener(onFriendListReceived);
        mActivity.onGetChatPoll.addEventListener(onChatPollEvent);
        mKids.clear();
        mKids.addAll(mCallback.getKidList());
        mImageAdapter.notifyDataSetChanged();
        mGlKids.setSelection(getKidIdx(mKids, mCallback.getCurrentKid()));
        setViewHeight();
    }

    @Override
    public void onPause() {
        mActivity.onGetFriendList.removeEventListener(onFriendListReceived);
        mActivity.onGetChatPoll.removeEventListener(onChatPollEvent);
        sv_root.setScrollY(0);
        super.onPause();
    }

    private IApiEventListener onFriendListReceived = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                friends_outObj updatedList = (friends_outObj) obj;
                db.updateNSAFriendList(updatedList);
                if (mCallback.checkDataOwnership(updatedList.mUserKey)) {
                    mFriends.clear();
                    mFriends.addAll(db.getNSAFriendList(updatedList.mUserKey));
                }
            } else if (NSAActivity.DEBUG) {
                mFriends.addAll(NSAUtil.getFakeFriendList());
            } else {
                LOG.D(TAG, "friend list fail");
                if (mCallback.getUserData() != null) {
                    mCallback.refreshFriendList();
                }
                // TODO: friend list failed
            }
            int y = sv_root.getScrollY();
            mChatAdapter.notifyDataSetChanged();
            sv_root.setScrollY(y);
        }
    };

    private IApiEventListener onChatPollEvent = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                chatPollMessage_outObj data = (chatPollMessage_outObj) obj;
                db.updateConversationList(data);
                if (mCallback.checkDataOwnership(data.mUserKey)) {
                    mConversationList.clear();
                    mConversationList.addAll(data.getConversations());
                }
                insertLastTalkTimeAndSortFriendList(mFriends, mConversationList);
            } else if (NSAActivity.DEBUG) {
                mConversationList.addAll(NSAUtil
                        .getFakeConversationList(mCallback.getCurrentKid().getKidId(), mFriends));
            } else {
                LOG.D(TAG, "onChatPollEvent failed");
            }
            int y = sv_root.getScrollY();
            mChatAdapter.notifyDataSetChanged();
            sv_root.setScrollY(y);
        }
    };

    private class ChatAdapter extends ArrayAdapter<FriendData> {
        private LayoutInflater inflater;
        private AQuery aq;

        private ColorMatrixColorFilter blackAndWhiteFilter;
        private ColorMatrixColorFilter originalFilter;

        public ChatAdapter(Context context, int textViewResourceId, List<FriendData> objects) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.nsa_item_conversation, null);
                holder = new ViewHolder();
                holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                holder.iv_chat_icon = (ImageView) convertView.findViewById(R.id.iv_chat_icon);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                holder.tv_unread = (TextView) convertView.findViewById(R.id.tv_unread);
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

            if (data.blocked) {
                holder.iv_avatar.setColorFilter(blackAndWhiteFilter);
            } else {
                holder.iv_avatar.setColorFilter(originalFilter);
            }

            holder.tv_name.setText(data.userName);
            NSAUtil.setTypeface(getActivity(), holder.tv_name, getString(R.string.roboto_medium));
            NSAUtil.setTypeface(getActivity(), holder.tv_unread, getString(R.string.roboto_bold));
            int unreadCount = getUnreadCount(data.userID);
            holder.iv_chat_icon.setVisibility(unreadCount == 0 ? View.INVISIBLE : View.VISIBLE);
            holder.tv_unread.setVisibility(holder.iv_chat_icon.getVisibility());
            if (unreadCount > 99) {
                holder.tv_unread.setText("N");
            } else {
                holder.tv_unread.setText(Integer.toString(unreadCount));
            }
            return convertView;
        }
    }

    static private class ViewHolder {
        public ImageView iv_avatar, iv_chat_icon;
        public TextView tv_name, tv_unread;
    }

    /**
     * @param userId contact id
     * @return unread message count of conversation with userId
     */
    private int getUnreadCount(String userId) {
        for (conversationData cd : mConversationList) {
            if (cd.m_Actors.contains(userId)) {
                return cd.m_UnreadMessageCount;
            }
        }
        return 0;
    }

    /**
     * retrieve from database
     *
     * @param contactKey
     * @return
     */
    private String getConversationId(String contactKey) {
        return db.getConversationId(getUserKey(), contactKey);
    }

    /**
     * @param conversationId
     * @return ++timestamp because server query uses strictly less than
     * {timestamp}
     */
    private long getLastTimeStamp(String conversationId) {
        for (conversationData cd : mConversationList) {
            if (cd.m_ConversationId.equals(conversationId)) {
                return ++cd.m_LatestMessageTimeStamp;
            }
        }
        return 0;
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
                return mLvChat.onTouchEvent(event);
            } else {
                if (deltaY > 0) {
                    // want to scroll down list view
                    sv_root.requestDisallowInterceptTouchEvent(true);
                    return mLvChat.onTouchEvent(event);
                }
                sv_root.requestDisallowInterceptTouchEvent(false);
                return false;
            }
        }
    };

    private AdapterView.OnItemClickListener oicl = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FriendData data = mChatAdapter.getItem(position);
            String conversationId = getConversationId(data.userID);
            mBundleHistory.putString(FragmentNSA.KEY_USER_ID, getUserKey());
            mBundleHistory.putString(FragmentNSA.KEY_CONVERSATION_ID, conversationId);
            mBundleHistory.putString(FragmentNSA.KEY_TARGET_ID, data.userID);
            mBundleHistory.putString(FragmentNSA.KEY_TARGET_NAME, data.userName);
            mBundleHistory.putBoolean(FragmentNSA.KEY_BLOCKED, data.blocked);
            mBundleHistory.putLong(FragmentNSA.KEY_TIMESTAMP, getLastTimeStamp(conversationId));
            mBundleHistory.putString(FragmentNSA.KEY_AVATAR_URL, data.AvatarUrl);
            if (mFragmentHistory == null) {
                mFragmentHistory = new FragmentChatHistory();
            }

            //tracking
            Tracking.pushTrack(view.getContext(), "view_message_#" + data.userName);
            passTrackBackOnce();
            FragmentChatHistory.TRACK_CHAT_NAME = data.userName;

            mFragmentHistory.setData(mBundleHistory);
            mActivity.switchFragment(mFragmentHistory, true);
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

                    //tracking
                    Tracking.pushTrack(v.getContext(), Tracking.H_NSA_NABI_NSA, "choose_kid_left");

                    break;
                case R.id.iv_right:
                    if (idx == mImageAdapter.getCount() - 1) {
                        return;
                    } else {
                        idx++;
                    }

                    //tracking
                    Tracking.pushTrack(v.getContext(), Tracking.H_NSA_NABI_NSA, "choose_kid_right");

                    break;
            }
            // this will trigger onItemSelectedListener
            mGlKids.setSelection(idx);
        }
    };

    private AdapterView.OnItemSelectedListener oisl = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int y = sv_root.getScrollY();
            mFriends.clear();
            mConversationList.clear();
            String userKey = db.getUserKey(mKids.get(position).getKidId());
            mFriends.addAll(db.getNSAFriendList(userKey));
            mConversationList.addAll(db.getConversationList(userKey));
            mChatAdapter.notifyDataSetChanged();
            mLvChat.setSelection(0);
            mImageAdapter.setSelectItem(position);
            mGlKids.setSelection(position);
            mTvName.setText(mKids.get(position).getkidName());
            mCallback.onKidChanged(mKids.get(position));
            mImageAdapter.notifyDataSetChanged();
            sv_root.setScrollY(y);

            //tracking
            Tracking.pushTrack(getActivity(), Tracking.H_NSA_NABI_NSA, "select_choose_kid_" + mKids.get(position).getkidName());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    public void setViewHeight() {
        if (rl_gallery.getHeight() == 0) {
            aq.id(rl_gallery).height(NSAActivity.TOP_VIEW_HEIGHT, false);
        }
        if (mLvChat.getHeight() == 0) {
            aq.id(mLvChat).height(NSAActivity.BOT_VIEW_HEIGHT, false);
        }
    }

    private String getUserKey() {
        return db.getUserKey(((Kid) mGlKids.getSelectedItem()).getKidId());
    }

    private void insertLastTalkTimeAndSortFriendList(ArrayList<FriendData> friends, ArrayList<conversationData> chatList) {
        conversationData cd;
        for (FriendData fd : mFriends) {
            cd = findChatByFriendId(fd.userID, mConversationList);
            if (cd != null) {
                // found
                fd.mLastTalkTime = cd.m_LatestMessageTimeStamp;
            } else {
                fd.mLastTalkTime = -1;
            }
        }
        Collections.sort(mFriends, Collections.reverseOrder(mFriendComparator));
    }

    private conversationData findChatByFriendId(String friendId, ArrayList<conversationData> chatList) {
        for (conversationData cd : chatList) {
            if (cd.m_Actors.contains(friendId)) {
                return cd;
            }
        }
        return null;
    }

    private Comparator<FriendData> mFriendComparator = new Comparator<FriendData>() {
        @Override
        public int compare(FriendData lhs, FriendData rhs) {
            long diff = lhs.mLastTalkTime - rhs.mLastTalkTime;
            if (diff == 0) {
                if (lhs.mLastTalkTime == -1) {
                    // sort using contact user name, in reverse order
                    return rhs.userName.compareToIgnoreCase(lhs.userName);
                } else {
                    return 0;
                }
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        }
    };
}
