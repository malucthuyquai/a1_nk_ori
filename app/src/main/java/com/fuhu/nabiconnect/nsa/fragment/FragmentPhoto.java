package com.fuhu.nabiconnect.nsa.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.fuhu.account.data.Kid;
import com.fuhu.data.ReceivedPhotoData;
import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.nsa.NSAActivity;
import com.fuhu.nabiconnect.nsa.util.ApiHelper;
import com.fuhu.nabiconnect.nsa.util.ImageAdapter;
import com.fuhu.nabiconnect.nsa.util.NSAPhotoUtil;
import com.fuhu.nabiconnect.nsa.util.NSAPhotoUtil.PhotoGroup;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.nsa.view.NSAGallery;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.getSharedAndReceivedPhotos_outObj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * TODO: optimize
 */
public class FragmentPhoto extends FragmentNSA {

    final private String TAG = FragmentPhoto.class.getSimpleName();
    final private int PHOTO_GROUP_SIZE = 8;

    private AQuery aq;
    private ScrollView sv_root;
    private RelativeLayout rl_gallery;
    private RelativeLayout rl_mid;
    private TextView mTvName;

    /**
     * Gallery Related
     */
    private NSAGallery mGlKids;
    private ArrayList<Kid> mKids = new ArrayList<Kid>();
    private ImageAdapter mImageAdapter;
    private Bitmap mDefaultAvatar;

    /**
     * Photo related
     */
    private ListView mLvPhoto;
    private ArrayList<ReceivedPhotoData> mPhotos = new ArrayList<ReceivedPhotoData>();
    private ArrayList<PhotoGroup> mPhotoGroups = new ArrayList<PhotoGroup>();
    private ListAdapter mLVAdapter;
    private HashSet<String> mDeleteFlag = new HashSet<String>();
    /**
     * used to store ids of photo whose delete request is already sent
     */
    private HashSet<String> mDeletePool = new HashSet<String>();
    private LinearLayout ll_footer, ll_left, ll_right;

    /**
     * Internal reference
     */
    private NSAActivity mActivity;
    private DatabaseAdapter db;

    private ColorMatrixColorFilter mBlackAndWhiteFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorMatrix matrix = new ColorMatrix();
        matrix = new ColorMatrix();
        matrix.setSaturation(0);
        mBlackAndWhiteFilter = new ColorMatrixColorFilter(matrix);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDeleteFlag.clear();
        ll_footer = (LinearLayout) inflater.inflate(R.layout.nsa_footer_photo, null, false);
        ll_left = (LinearLayout) ll_footer.findViewById(R.id.ll_left);
        ll_right = (LinearLayout) ll_footer.findViewById(R.id.ll_right);
        return inflater.inflate(R.layout.nsa_fragment_photo, null);
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
        mLvPhoto = (ListView) getView().findViewById(R.id.lv_photo);
        mLvPhoto.addFooterView(ll_footer);
        mLVAdapter = new ListAdapter(getActivity(), 0, mPhotoGroups);
        mLvPhoto.setAdapter(mLVAdapter);
        mLvPhoto.setOnTouchListener(otl);

        mActivity = (NSAActivity) getActivity();
        mDefaultAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.chat_avatar_default);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.onGetAllPhoto.addEventListener(onPhotoLoaded);
        mKids.clear();
        mKids.addAll(mCallback.getKidList());
        mImageAdapter.notifyDataSetChanged();
        mGlKids.setSelection(getKidIdx(mKids, mCallback.getCurrentKid()));
        setViewHeight();
        mRefreshHandler.sendEmptyMessageDelayed(FragmentNSA.REFRESH_WHAT, FragmentNSA.REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        ApiHelper.getInstance(getActivity(), mHandler).cancel();
        mActivity.onGetAllPhoto.removeEventListener(onPhotoLoaded);
        sv_root.setScrollY(0);
        mRefreshHandler.removeMessages(FragmentNSA.REFRESH_WHAT);
        super.onPause();
    }

    private IApiEventListener onPhotoLoaded = new IApiEventListener() {
        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                if (obj instanceof String) {
                    // returned empty set, clear local cache
                    LOG.D(TAG, "obj: " + obj.toString());
                    int affectedRows = db.deleteAllPhoto(obj.toString());
                    LOG.D(TAG, "affected rows: " + affectedRows);
                    mPhotos.clear();
                    regroupPhotos();
                    mLvPhoto.post(mRefreshPhotobox);
                    return;
                }

                getSharedAndReceivedPhotos_outObj data = (getSharedAndReceivedPhotos_outObj) obj;
                addBlockFlag(data);
                LOG.D(TAG, "updating data for: " + data.mUserKey);
                // 04-10 15:29:52.810: D/FragmentPhoto(12411): updating data for: 1480835548493907065
                db.updateAllPhoto(data.mUserKey, data, showingLocalHistory);
                if (mCallback.checkDataOwnership(data.mUserKey)) {
                    mBuffer.addAll(data.getPhotos());
                    if (mBuffer.size() > 0) {
                        mLvPhoto.post(mRefreshPhotobox);
                    }
                }
            } else if (NSAActivity.DEBUG) {
                mBuffer.addAll(NSAUtil.getFakePhoto());
                mLvPhoto.post(mRefreshPhotobox);
            } else {
                loadFinished = true;
                LOG.E(TAG, "photo delete failed");
                int y = sv_root.getScrollY();
                mLVAdapter.notifyDataSetChanged();
                sv_root.setScrollY(y);
            }
        }
    };

    private class ListAdapter extends ArrayAdapter<PhotoGroup> {
        private Context context;
        private AQuery aq;

        public ListAdapter(Context context, int textViewResourceId, List<PhotoGroup> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            aq = new AQuery(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return NSAPhotoUtil.getPhotoView(context, getItem(position), convertView, aq, mDefaultAvatar, mDeleteFlag,
                    delete_ocl, db, context.getResources().getInteger(R.integer.photo_target_width),
                    mBlackAndWhiteFilter);
        }
    }

    // private String mDeletePhotoId;
    private View.OnClickListener delete_ocl = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ReceivedPhotoData data = (ReceivedPhotoData) v.getTag();
            if(data == null){
                LOG.E(TAG, "received photo data is null, check view has tag set");
                mCallback.showErrorDialog(false);
                return;
            }
            if (v.isSelected()) {
                // already pressed once, ok to delete

                // check if logged in
                UserData userData = mCallback.getUserData();
                if (userData == null) {
                    //not logged in, return
                    LOG.E(TAG, "parent login failed previously, cant delete photo");
                    mCallback.showErrorDialog(false);
                    return;
                }

                // check which api to call
                if (Long.toString(data.fromId).equals(userData.userKey)) {
                    if (mDeletePool.add(data.id)) {
                        // photo that we sent out, delete it completely
                        mCallback.deleteSharedPhoto(data.id, mHandler);
                    }
                } else {
                    if (mDeletePool.add(data.id)) {
                        // photo that we received, just remove it
                        mCallback.removeReceivedPhoto(data.id, mHandler);
                    }
                }
            } else {
                // not yet selected, just mark item as delete pending
                v.setSelected(true);
                mDeleteFlag.add(data.id);
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message m) {
            switch (m.what) {
                case ApiHelper.NSA_PHOTO_DELETE:
                    mDeletePool.remove(m.obj.toString());
                    if (m.arg1 == 0 || NSAActivity.DEBUG) {
                        deleteLocalPhoto(m.obj.toString());
                    } else {
                        mCallback.showErrorDialog(false);
                    }
                    break;
            }
        }
    };

    private ArrayList<ReceivedPhotoData> mBuffer = new ArrayList<ReceivedPhotoData>();
    private Thread mHistoryLoaderThread;
    volatile boolean loadFinished = true;
    private View.OnTouchListener otl = new View.OnTouchListener() {
        int[] loc = new int[2];
        float lastY;
        float deltaY = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mLvPhoto.getLastVisiblePosition() == mLvPhoto.getCount() - 1) {
                if (loadFinished) {
                    try {
                        loadFinished = false;
                        if (mHistoryLoaderThread == null
                                || mHistoryLoaderThread.getState().equals(Thread.State.TERMINATED)) {
                            mHistoryLoaderThread = new Thread(mHistoryLoader);
                            mHistoryLoaderThread.start();
                        }
                    } catch (IllegalThreadStateException e) {
                        e.printStackTrace();
                    }
                }
            }
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
                return mLvPhoto.onTouchEvent(event);
            } else {
                if (deltaY > 0) {
                    // want to scroll down list view
                    sv_root.requestDisallowInterceptTouchEvent(true);
                    return mLvPhoto.onTouchEvent(event);
                }
                sv_root.requestDisallowInterceptTouchEvent(false);
                return false;
            }
        }
    };

    private long mLastTimestamp = 0;
    private boolean mShouldLoadHistory = false;
    private Runnable mHistoryLoader = new Runnable() {
        @Override
        public void run() {
            if (showingLocalHistory) {
                // havnen't heard back from server even once, do not load
                // history
                return;
            }
            if (!mShouldLoadHistory) {
                if (mPhotos.size() > 0) {
                    mLastTimestamp = mPhotos.get(0).createdTime;
                    mShouldLoadHistory = true;
                }
                return;
            }
            mCallback.loadAllPhoto(0, mLastTimestamp, FragmentNSA.PHOTO_POLL_LIMIT, false);
        }
    };

    private Runnable mRefreshPhotobox = new Runnable() {
        @Override
        public void run() {
            if (showingLocalHistory) {
                showingLocalHistory = false;
                mPhotos.clear();
            }
            int scrollPos = sv_root.getScrollY();
            if (mBuffer.size() > 0) {
                if (mLastTimestamp == 0 || mLastTimestamp >= mBuffer.get(0).createdTime) {
                    // old photo
                    mLastTimestamp = mBuffer.get(mBuffer.size() - 1).createdTime;
                    mPhotos.addAll(mBuffer);
                } else {
                    // new photo, don't update last time stamp, and insert to
                    // front
                    mPhotos.addAll(0, mBuffer);
                }
                regroupPhotos();
                mBuffer.clear();
            }
            mLVAdapter.notifyDataSetChanged();
            loadFinished = true;
            mShouldLoadHistory = true;
            sv_root.setScrollY(scrollPos);
        }
    };

    private void deleteLocalPhoto(String photoId) {
        for (ReceivedPhotoData rpd : mPhotos) {
            if (rpd.id.equals(photoId)) {
                mPhotos.remove(rpd);
                break;
            }
        }
        db.deletePhoto(getUserKey(), photoId);
        mDeleteFlag.remove(photoId);
        regroupPhotos();
        mLVAdapter.notifyDataSetChanged();
    }

    /**
     * touch event callbacks
     */

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

    private AdapterView.OnItemSelectedListener oisl = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mBuffer.clear();
            mPhotos.clear();
            mPhotoGroups.clear();
            mDeleteFlag.clear();
            mLastTimestamp = 0;
            ll_left.setVisibility(View.GONE);
            ll_right.setVisibility(View.GONE);
            String userKey = getUserKey();
            mBuffer.addAll(db.getAllPhoto(userKey));
            int y = sv_root.getScrollY();

            if (mBuffer.size() > 0) {
                showingLocalHistory = true;
                mPhotos.addAll(mBuffer);
                regroupPhotos();
                mBuffer.clear();
                // loadFinished = true;
                // mShouldLoadHistory = true;
            }
            sv_root.setScrollY(y);
            mImageAdapter.setSelectItem(position);
            mGlKids.setSelection(position);
            mTvName.setText(mKids.get(position).getkidName());
            mCallback.onKidChanged(mKids.get(position));
            mImageAdapter.notifyDataSetChanged();
            mLVAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    public void setViewHeight() {
        if (rl_gallery.getHeight() == 0) {
            aq.id(rl_gallery).height(NSAActivity.TOP_VIEW_HEIGHT, false);
        }
        if (mLvPhoto.getHeight() == 0) {
            aq.id(mLvPhoto).height(NSAActivity.BOT_VIEW_HEIGHT, false);
        }
    }

    private void regroupPhotos() {
        mPhotoGroups.clear();
        int idx = 0;
        int size = mPhotos.size();
        final int LONGHEIGHT = (int) Math.round(getResources().getDimension(R.dimen.nsa_photo_long_height));
        final int SHORTHEIGHT = (int) Math.round(getResources().getDimension(R.dimen.nsa_photo_short_height));
        String userKey = getUserKey();
        // fill listview with photo sets of 8
        while (size - idx > PHOTO_GROUP_SIZE - 1) {
            PhotoGroup photoGroup = new PhotoGroup(userKey, mPhotos.get(idx++), mPhotos.get(idx++), mPhotos.get(idx++),
                    mPhotos.get(idx++), mPhotos.get(idx++), mPhotos.get(idx++), mPhotos.get(idx++), mPhotos.get(idx++));
            photoGroup.photo01.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo01.fromId));
            photoGroup.photo02.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo02.fromId));
            photoGroup.photo03.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo03.fromId));
            photoGroup.photo04.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo04.fromId));
            photoGroup.photo05.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo05.fromId));
            photoGroup.photo06.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo06.fromId));
            photoGroup.photo07.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo07.fromId));
            photoGroup.photo08.blocked = mCallback.isFriendBlocked(userKey, Long.toString(photoGroup.photo08.fromId));
            mPhotoGroups.add(photoGroup);
        }

        if (idx < size) {
            // still have photos in mPhoto
            AQuery aq = new AQuery(ll_footer);
            // hide everything
            aq.id(R.id.rl_photo07).visibility(View.GONE).id(R.id.fl_photo07).visibility(View.GONE).id(R.id.rl_photo06)
                    .visibility(View.GONE).id(R.id.fl_photo06).visibility(View.GONE).id(R.id.rl_photo05)
                    .visibility(View.GONE).id(R.id.fl_photo05).visibility(View.GONE).id(R.id.rl_photo04)
                    .visibility(View.GONE).id(R.id.fl_photo04).visibility(View.GONE).id(R.id.rl_photo03)
                    .visibility(View.GONE).id(R.id.fl_photo03).visibility(View.GONE).id(R.id.rl_photo02)
                    .visibility(View.GONE).id(R.id.fl_photo02).visibility(View.GONE).id(R.id.rl_photo01)
                    .visibility(View.GONE).id(R.id.fl_photo01).visibility(View.GONE);

            ReceivedPhotoData photo;
            ll_left.setVisibility(View.VISIBLE);
            ll_right.setVisibility(View.VISIBLE);
            switch (size - idx) {
                case 7:
                    photo = mPhotos.get(--size);
                    aq.id(R.id.rl_photo07).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo07).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo07).height(LONGHEIGHT, false);
                    aq.id(R.id.iv_delete07).tag(photo).clicked(delete_ocl);
                    NSAPhotoUtil.loadAvatar(getActivity(), aq, userKey, photo, aq.id(R.id.iv_avatar07).getImageView(),
                            mDefaultAvatar, db, mCallback.isFriendBlocked(userKey, Long.toString(photo.fromId)),
                            mBlackAndWhiteFilter);
                    // NSAPhotoUtil.loadPhoto(aq, userKey, photo,
                    // aq.id(R.id.pb_loading07).getProgressBar(),
                    // aq.id(R.id.iv_photo07).getImageView(), db);
                    NSAPhotoUtil.loadPhoto(aq, userKey, photo, null, aq.id(R.id.iv_photo07).getImageView(), db,
                            getActivity().getResources().getInteger(R.integer.photo_target_width));
                    aq.id(R.id.tv_name07).text(photo.fromName);
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_medium));
                    aq.id(R.id.tv_timestamp07).text(NSAPhotoUtil.getEllapsedTimeString(photo.createdTime));
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_regular));
                    aq.id(R.id.iv_delete07).tag(photo).getView().setSelected(mDeleteFlag.contains(photo.id));
                case 6:
                    photo = mPhotos.get(--size);
                    aq.id(R.id.rl_photo06).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo06).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo06).height(LONGHEIGHT, false);
                    aq.id(R.id.iv_delete06).tag(photo).clicked(delete_ocl);
                    NSAPhotoUtil.loadAvatar(getActivity(), aq, userKey, photo, aq.id(R.id.iv_avatar06).getImageView(),
                            mDefaultAvatar, db, mCallback.isFriendBlocked(userKey, Long.toString(photo.fromId)),
                            mBlackAndWhiteFilter);
                    // NSAPhotoUtil.loadPhoto(aq, userKey, photo,
                    // aq.id(R.id.pb_loading06).getProgressBar(),
                    // aq.id(R.id.iv_photo06).getImageView(), db);
                    NSAPhotoUtil.loadPhoto(aq, userKey, photo, null, aq.id(R.id.iv_photo06).getImageView(), db,
                            getActivity().getResources().getInteger(R.integer.photo_target_width));
                    aq.id(R.id.tv_name06).text(photo.fromName);
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_medium));
                    aq.id(R.id.tv_timestamp06).text(NSAPhotoUtil.getEllapsedTimeString(photo.createdTime));
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_regular));
                    aq.id(R.id.iv_delete06).tag(photo).getView().setSelected(mDeleteFlag.contains(photo.id));
                case 5:
                    photo = mPhotos.get(--size);
                    aq.id(R.id.rl_photo05).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo05).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo05).height(LONGHEIGHT, false);
                    aq.id(R.id.iv_delete05).tag(photo).clicked(delete_ocl);
                    NSAPhotoUtil.loadAvatar(getActivity(), aq, userKey, photo, aq.id(R.id.iv_avatar05).getImageView(),
                            mDefaultAvatar, db, mCallback.isFriendBlocked(userKey, Long.toString(photo.fromId)),
                            mBlackAndWhiteFilter);
                    // NSAPhotoUtil.loadPhoto(aq, userKey, photo,
                    // aq.id(R.id.pb_loading05).getProgressBar(),
                    // aq.id(R.id.iv_photo05).getImageView(), db);
                    NSAPhotoUtil.loadPhoto(aq, userKey, photo, null, aq.id(R.id.iv_photo05).getImageView(), db,
                            getActivity().getResources().getInteger(R.integer.photo_target_width));
                    aq.id(R.id.tv_name05).text(photo.fromName);
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_medium));
                    aq.id(R.id.tv_timestamp05).text(NSAPhotoUtil.getEllapsedTimeString(photo.createdTime));
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_regular));
                    aq.id(R.id.iv_delete05).tag(photo).getView().setSelected(mDeleteFlag.contains(photo.id));
                case 4:
                    photo = mPhotos.get(--size);
                    aq.id(R.id.rl_photo04).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo04).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo04).height(SHORTHEIGHT, false);
                    aq.id(R.id.iv_delete04).tag(photo).clicked(delete_ocl);
                    NSAPhotoUtil.loadAvatar(getActivity(), aq, userKey, photo, aq.id(R.id.iv_avatar04).getImageView(),
                            mDefaultAvatar, db, mCallback.isFriendBlocked(userKey, Long.toString(photo.fromId)),
                            mBlackAndWhiteFilter);
                    // NSAPhotoUtil.loadPhoto(aq, userKey, photo,
                    // aq.id(R.id.pb_loading04).getProgressBar(),
                    // aq.id(R.id.iv_photo04).getImageView(), db);
                    NSAPhotoUtil.loadPhoto(aq, userKey, photo, null, aq.id(R.id.iv_photo04).getImageView(), db,
                            getActivity().getResources().getInteger(R.integer.photo_target_width));
                    aq.id(R.id.tv_name04).text(photo.fromName);
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_medium));
                    aq.id(R.id.tv_timestamp04).text(NSAPhotoUtil.getEllapsedTimeString(photo.createdTime));
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_regular));
                    aq.id(R.id.iv_delete04).tag(photo).getView().setSelected(mDeleteFlag.contains(photo.id));
                case 3:
                    photo = mPhotos.get(--size);
                    aq.id(R.id.rl_photo03).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo03).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo03).height(SHORTHEIGHT, false);
                    aq.id(R.id.iv_delete03).tag(photo).clicked(delete_ocl);
                    NSAPhotoUtil.loadAvatar(getActivity(), aq, userKey, photo, aq.id(R.id.iv_avatar03).getImageView(),
                            mDefaultAvatar, db, mCallback.isFriendBlocked(userKey, Long.toString(photo.fromId)),
                            mBlackAndWhiteFilter);
                    // NSAPhotoUtil.loadPhoto(aq, userKey, photo,
                    // aq.id(R.id.pb_loading03).getProgressBar(),
                    // aq.id(R.id.iv_photo03).getImageView(), db);
                    NSAPhotoUtil.loadPhoto(aq, userKey, photo, null, aq.id(R.id.iv_photo03).getImageView(), db,
                            getActivity().getResources().getInteger(R.integer.photo_target_width));
                    aq.id(R.id.tv_name03).text(photo.fromName);
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_medium));
                    aq.id(R.id.tv_timestamp03).text(NSAPhotoUtil.getEllapsedTimeString(photo.createdTime));
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_regular));
                    aq.id(R.id.iv_delete03).tag(photo).getView().setSelected(mDeleteFlag.contains(photo.id));
                case 2:
                    photo = mPhotos.get(--size);
                    aq.id(R.id.rl_photo02).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo02).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo02).height(SHORTHEIGHT, false);
                    aq.id(R.id.iv_delete02).tag(photo).clicked(delete_ocl);
                    NSAPhotoUtil.loadAvatar(getActivity(), aq, userKey, photo, aq.id(R.id.iv_avatar02).getImageView(),
                            mDefaultAvatar, db, mCallback.isFriendBlocked(userKey, Long.toString(photo.fromId)),
                            mBlackAndWhiteFilter);
                    // NSAPhotoUtil.loadPhoto(aq, userKey, photo,
                    // aq.id(R.id.pb_loading02).getProgressBar(),
                    // aq.id(R.id.iv_photo02).getImageView(), db);
                    NSAPhotoUtil.loadPhoto(aq, userKey, photo, null, aq.id(R.id.iv_photo02).getImageView(), db,
                            getActivity().getResources().getInteger(R.integer.photo_target_width));
                    aq.id(R.id.tv_name02).text(photo.fromName);
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_medium));
                    aq.id(R.id.tv_timestamp02).text(NSAPhotoUtil.getEllapsedTimeString(photo.createdTime));
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_regular));
                    aq.id(R.id.iv_delete02).tag(photo).getView().setSelected(mDeleteFlag.contains(photo.id));
                case 1:
                    photo = mPhotos.get(--size);
                    aq.id(R.id.rl_photo01).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo01).visibility(View.VISIBLE);
                    aq.id(R.id.fl_photo01).height(LONGHEIGHT, false);
                    aq.id(R.id.iv_delete01).tag(photo).clicked(delete_ocl);
                    NSAPhotoUtil.loadAvatar(getActivity(), aq, userKey, photo, aq.id(R.id.iv_avatar01).getImageView(),
                            mDefaultAvatar, db, mCallback.isFriendBlocked(userKey, Long.toString(photo.fromId)),
                            mBlackAndWhiteFilter);
                    // NSAPhotoUtil.loadPhoto(aq, userKey, photo,
                    // aq.id(R.id.pb_loading01).getProgressBar(),
                    // aq.id(R.id.iv_photo01).getImageView(), db);
                    NSAPhotoUtil.loadPhoto(aq, userKey, photo, null, aq.id(R.id.iv_photo01).getImageView(), db,
                            getActivity().getResources().getInteger(R.integer.photo_target_width));
                    aq.id(R.id.tv_name01).text(photo.fromName);
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_medium));
                    aq.id(R.id.tv_timestamp01).text(NSAPhotoUtil.getEllapsedTimeString(photo.createdTime));
                    NSAUtil.setTypeface(getActivity(), aq.getTextView(), getString(R.string.roboto_regular));
                    aq.id(R.id.iv_delete01).tag(photo).getView().setSelected(mDeleteFlag.contains(photo.id));
            }
        } else {
            // nothing left , hide footer
            ll_left.setVisibility(View.GONE);
            ll_right.setVisibility(View.GONE);
        }
    }

    private boolean showingLocalHistory = false;

    // private void loadLocalPhoto() {
    // mBuffer.addAll(db.getAllPhoto(getUserKey()));
    // int scrollPos = sv_root.getScrollY();
    // if (mBuffer.size() > 0) {
    // showingLocalHistory = true;
    // mPhotos.addAll(mBuffer);
    // regroupPhotos();
    // mLVAdapter.notifyDataSetChanged();
    // mBuffer.clear();
    // }
    // loadFinished = true;
    // mShouldLoadHistory = true;
    // sv_root.setScrollY(scrollPos);
    // }

    private String getUserKey() {
        return db.getUserKey(((Kid) mGlKids.getSelectedItem()).getKidId());
    }

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FragmentNSA.REFRESH_WHAT) {
                this.removeMessages(FragmentNSA.REFRESH_WHAT);
                if (mPhotoGroups.size() > 0) {
                    // currently have photo in display
                    mCallback.loadAllPhoto(mPhotoGroups.get(0).photo01.createdTime, 0, FragmentNSA.PHOTO_POLL_LIMIT,
                            false);
                }
                this.sendEmptyMessageDelayed(FragmentNSA.REFRESH_WHAT, FragmentNSA.REFRESH_INTERVAL);
            }
        }
    };

    private void addBlockFlag(getSharedAndReceivedPhotos_outObj data) {
        ArrayList<ReceivedPhotoData> list = data.getPhotos();
        for (ReceivedPhotoData rpd : list) {
            String fromId = Long.toString(rpd.fromId);
            if (fromId.equals(data.mUserKey)) {
                // is a shared photo
                rpd.blocked = false;
            } else {
                rpd.blocked = mCallback.isFriendBlocked(data.mUserKey, fromId);
            }
        }
        regroupPhotos();
        mLVAdapter.notifyDataSetChanged();
    }
}