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
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.account.data.Kid;
import com.fuhu.data.InboxesData;
import com.fuhu.data.OutboxesData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.nsa.NSAActivity;
import com.fuhu.nabiconnect.nsa.util.ImageAdapter;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.nsa.view.NSAGallery;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.getInboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getOutboxes_outObj;

import java.util.ArrayList;
import java.util.List;

/**
 * used to display list of mailboxes
 */
public class FragmentMail extends FragmentNSA {
	final private String TAG = FragmentMail.class.getSimpleName();

	private AQuery aq;
	private ScrollView sv_root;
	private RelativeLayout rl_gallery;
	private LinearLayout ll_mid;
	private TextView mTvName;
	private NSAGallery mGlKids;

	private ArrayList<Kid> mKids = new ArrayList<Kid>();
	private ImageAdapter mImageAdapter;
	private ListView mListView;
	private ArrayList<InboxesData> mInboxes = new ArrayList<InboxesData>();
	private ArrayList<OutboxesData> mOutboxes = new ArrayList<OutboxesData>();

	private InboxAdapter mInboxAdapter;
	private OutboxAdapter mOutboxAdapter;

	private ColorMatrixColorFilter mBlackAndWhiteFilter;
	private ColorMatrixColorFilter mOriginalFilter;

	private NSAActivity mActivity;
	private Bitmap mDefaultAvatar;
	private FragmentMailbox mFragmentMailbox;

	private DatabaseAdapter db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ColorMatrix matrix = new ColorMatrix();
		matrix = new ColorMatrix();
		matrix.setSaturation(0);
		mBlackAndWhiteFilter = new ColorMatrixColorFilter(matrix);
		matrix.setSaturation(1);
		mOriginalFilter = new ColorMatrixColorFilter(matrix);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nsa_fragment_mail, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aq = new AQuery(getActivity());
		db = mCallback.getDB();

		sv_root = (ScrollView) aq.id(R.id.sv_root).getView();
		rl_gallery = (RelativeLayout) aq.id(R.id.rl_gallery).getView();
		ll_mid = (LinearLayout) aq.id(R.id.ll_mid).getView();

		mTvName = aq.id(R.id.tv_name).getTextView();
		NSAUtil.setTypeface(getActivity(), mTvName, getString(R.string.roboto_light));

		mGlKids = (NSAGallery) getView().findViewById(R.id.gl_kids);
		mImageAdapter = new ImageAdapter(getActivity(), mKids);
		mGlKids.setAdapter(mImageAdapter);
		mGlKids.setOnItemSelectedListener(oisl);

		mListView = aq.id(R.id.lv_mailbox).getListView();
		mInboxAdapter = new InboxAdapter(getActivity(), 0, mInboxes);
		mListView.setAdapter(mInboxAdapter);
		mListView.setOnItemClickListener(oicl);
		mListView.setOnTouchListener(list_otl);

		mOutboxAdapter = new OutboxAdapter(getActivity(), 0, mOutboxes);

		aq.id(R.id.iv_left).clicked(kid_ocl);
		aq.id(R.id.iv_right).clicked(kid_ocl);

		aq.id(R.id.iv_inbox).getImageView().setSelected(true);

		mCurrentTab = aq.id(R.id.iv_inbox).clicked(mailbox_ocl).getImageView();
		aq.id(R.id.iv_outbox).clicked(mailbox_ocl);

		mActivity = (NSAActivity) getActivity();
		mDefaultAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.chat_avatar_default);
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.onGetMailInboxes.addEventListener(onInboxLoaded);
		mActivity.onGetMailOutboxes.addEventListener(onOutboxLoaded);
		mKids.clear();
		mKids.addAll(mCallback.getKidList());
		mImageAdapter.notifyDataSetChanged();
		mGlKids.setSelection(getKidIdx(mKids, mCallback.getCurrentKid()));
		setViewHeight();
		mRefreshHandler.sendEmptyMessageDelayed(FragmentNSA.REFRESH_WHAT, FragmentNSA.REFRESH_INTERVAL);
	}

	@Override
	public void onPause() {
		mActivity.onGetMailInboxes.removeEventListener(onInboxLoaded);
		mActivity.onGetMailOutboxes.removeEventListener(onOutboxLoaded);
		sv_root.setScrollY(0);
		mRefreshHandler.removeMessages(FragmentNSA.REFRESH_WHAT);
		super.onPause();
	}

	private IApiEventListener onInboxLoaded = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				getInboxes_outObj data = (getInboxes_outObj) obj;
				addBlockFlag(data);
				db.updateNSAInboxes(data);
				if (mCallback.checkDataOwnership(data.mUserKey)) {
					mInboxes.clear();
					mInboxes.addAll(db.getNSAInboxes(data.mUserKey));
				}
			} else if (NSAActivity.DEBUG) {
				mInboxes.addAll(NSAUtil.getFakeInboxes());
			} else {
				LOG.D(TAG, "inbox event failed");
				// TODO: load inbox failed
			}
			int y = sv_root.getScrollY();
			mInboxAdapter.notifyDataSetChanged();
			sv_root.setScrollY(y);
		}
	};

	private IApiEventListener onOutboxLoaded = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				getOutboxes_outObj data = (getOutboxes_outObj) obj;
				addBlockFlag(data);
				db.updateNSAOutboxes(data);
				if (mCallback.checkDataOwnership(data.mUserKey)) {
					mOutboxes.clear();
					mOutboxes.addAll(db.getNSAOutboxes(data.mUserKey));
				}
			} else if (NSAActivity.DEBUG) {
				mOutboxes.addAll(NSAUtil.getFakeOutboxes());
			} else {
				LOG.D(TAG, "outbox event failed");
				// TODO: load outbox failed
			}
			int y = sv_root.getScrollY();
			mOutboxAdapter.notifyDataSetChanged();
			sv_root.setScrollY(y);
		}
	};

	private class InboxAdapter extends ArrayAdapter<InboxesData> {
		private LayoutInflater inflater;
		private AQuery aq;

		public InboxAdapter(Context context, int textViewResourceId, List<InboxesData> objects) {
			super(context, textViewResourceId, objects);
			inflater = LayoutInflater.from(context);
			aq = new AQuery(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.nsa_item_mailbox, null);
				holder = new ViewHolder();
				holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
				holder.iv_mail_icon = (ImageView) convertView.findViewById(R.id.iv_mail_icon);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_unread = (TextView) convertView.findViewById(R.id.tv_unread);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final InboxesData data = getItem(position);
			// load avatar
			final String avatarUrl = data.avatarURL.replace("https", "http");
			final String userKey = getUserKey();
			aq.id(holder.iv_avatar).tag(AQuery.TAG_1, avatarUrl);
			if (BitmapAjaxCallback.getMemoryCached(data.userId, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
				holder.iv_avatar.setImageBitmap(BitmapAjaxCallback.getMemoryCached(data.userId,
						FragmentNSA.AVATAR_TARGET_WIDTH));
			} else {
				// fetch newest from network
				final Bitmap cache = db.getFriendAvatar(userKey, data.userId, FragmentNSA.AVATAR_TARGET_WIDTH);
				final boolean hasCache = cache != null;
				BitmapAjaxCallback callback = new BitmapAjaxCallback() {
					@Override
					protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
						if (bmp != null) {
							// save once download is finished
							db.saveAvatarAsync(userKey, data.userId, bmp);
							this.memPut(data.userId, bmp);
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
				holder.iv_avatar.setColorFilter(mBlackAndWhiteFilter);
			} else {
				holder.iv_avatar.setColorFilter(mOriginalFilter);
			}

			holder.tv_name.setText(data.userName);
			NSAUtil.setTypeface(getActivity(), holder.tv_name, getString(R.string.roboto_medium));
			NSAUtil.setTypeface(getActivity(), holder.tv_unread, getString(R.string.roboto_bold));
			holder.tv_unread.setText(Long.toString(data.newReceiveCount));
			holder.tv_unread.setVisibility(data.newReceiveCount > 0 ? View.VISIBLE : View.INVISIBLE);
			return convertView;
		}
	}

	private class OutboxAdapter extends ArrayAdapter<OutboxesData> {
		private LayoutInflater inflater;
		private AQuery aq;

		public OutboxAdapter(Context context, int textViewResourceId, List<OutboxesData> objects) {
			super(context, textViewResourceId, objects);
			inflater = LayoutInflater.from(context);
			aq = new AQuery(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.nsa_item_mailbox, null);
				holder = new ViewHolder();
				holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
				holder.iv_mail_icon = (ImageView) convertView.findViewById(R.id.iv_mail_icon);
				holder.iv_mail_icon.setVisibility(View.GONE);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_unread = (TextView) convertView.findViewById(R.id.tv_unread);
				holder.tv_unread.setVisibility(View.GONE);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final OutboxesData data = getItem(position);
			// load avatar
			final String avatarUrl = data.avatarURL.replace("https", "http");
			final String userKey = getUserKey();
			aq.id(holder.iv_avatar).tag(AQuery.TAG_1, avatarUrl);
			if (BitmapAjaxCallback.getMemoryCached(data.userId, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
				holder.iv_avatar.setImageBitmap(BitmapAjaxCallback.getMemoryCached(data.userId,
						FragmentNSA.AVATAR_TARGET_WIDTH));
			} else {
				// fetch newest from network
				final Bitmap cache = db.getFriendAvatar(userKey, data.userId, FragmentNSA.AVATAR_TARGET_WIDTH);
				final boolean hasCache = cache != null;
				BitmapAjaxCallback callback = new BitmapAjaxCallback() {
					@Override
					protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
						if (bmp != null) {
							// save once download is finished
							db.saveAvatarAsync(userKey, data.userId, bmp);
							this.memPut(data.userId, bmp);
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
				holder.iv_avatar.setColorFilter(mBlackAndWhiteFilter);
			} else {
				holder.iv_avatar.setColorFilter(mOriginalFilter);
			}

			holder.tv_name.setText(data.userName);
			NSAUtil.setTypeface(getActivity(), holder.tv_name, getString(R.string.roboto_medium));
			return convertView;
		}
	}

	static private class ViewHolder {
		public ImageView iv_avatar, iv_mail_icon;
		public TextView tv_name, tv_unread;
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

			ll_mid.getLocationOnScreen(loc);
			if (loc[1] <= 0) {
				sv_root.requestDisallowInterceptTouchEvent(true);
				return mListView.onTouchEvent(event);
			} else {
				if (deltaY > 0) {
					// want to scroll down list view
					sv_root.requestDisallowInterceptTouchEvent(true);
					return mListView.onTouchEvent(event);
				}
				sv_root.requestDisallowInterceptTouchEvent(false);
				return false;
			}
		}
	};

	private AdapterView.OnItemClickListener oicl = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mFragmentMailbox = new FragmentMailbox();

			Bundle bundle = new Bundle();
			bundle.putString(FragmentNSA.KEY_MY_USERKEY, getUserKey());
			if (mCurrentTab.getId() == R.id.iv_inbox) {
				// currently showing inbox
				InboxesData data = mInboxAdapter.getItem(position);
				bundle.putString(FragmentNSA.KEY_AVATAR_URL, data.avatarURL);
				bundle.putString(FragmentNSA.KEY_MAILBOX_ID, data.inboxID);
				bundle.putLong(FragmentNSA.KEY_TIMESTAMP, data.lastTimeOfNewReceive);
				bundle.putLong(FragmentNSA.KEY_UNREAD, data.newReceiveCount);
				bundle.putString(FragmentNSA.KEY_USER_ID, data.userId);
				bundle.putString(FragmentNSA.KEY_USER_NAME, data.userName);
				bundle.putBoolean(FragmentNSA.KEY_BLOCKED, data.blocked);
			} else {
				// currently showing sent mail
				OutboxesData data = mOutboxAdapter.getItem(position);
				bundle.putString(FragmentNSA.KEY_AVATAR_URL, data.avatarURL);
				bundle.putString(FragmentNSA.KEY_MAILBOX_ID, data.outboxID);
				bundle.putLong(FragmentNSA.KEY_TIMESTAMP, data.lastTimeOfNewReceive);
				bundle.putString(FragmentNSA.KEY_USER_ID, data.userId);
				bundle.putString(FragmentNSA.KEY_USER_NAME, data.userName);
				bundle.putBoolean(FragmentNSA.KEY_BLOCKED, data.blocked);
			}
			mFragmentMailbox.setData(bundle);
			mActivity.showMailbox(mFragmentMailbox, true);
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

	private View mCurrentTab;
	private View.OnClickListener mailbox_ocl = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == mCurrentTab.getId()) {
				return;
			}
			int y = sv_root.getScrollY();
			switch (v.getId()) {
			case R.id.iv_inbox:
				mListView.setAdapter(mInboxAdapter);
				break;
			case R.id.iv_outbox:
				mListView.setAdapter(mOutboxAdapter);
				break;
			}
			mCurrentTab.setSelected(false);
			v.setSelected(true);
			mCurrentTab = v;
			sv_root.setScrollY(y);
		}
	};

	private AdapterView.OnItemSelectedListener oisl = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mInboxes.clear();
			mOutboxes.clear();
			String userKey = db.getUserKey(mKids.get(position).getKidId());
			mInboxes.addAll(db.getNSAInboxes(userKey));
			mOutboxes.addAll(db.getNSAOutboxes(userKey));
			int y = sv_root.getScrollY();
			mInboxAdapter.notifyDataSetChanged();
			mOutboxAdapter.notifyDataSetChanged();
			sv_root.setScrollY(y);
			mImageAdapter.setSelectItem(position);
			mGlKids.setSelection(position);
			mTvName.setText(mKids.get(position).getkidName());
			mCallback.onKidChanged(mKids.get(position));
			mImageAdapter.notifyDataSetChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	public void setViewHeight() {
		if (rl_gallery.getHeight() == 0) {
			aq.id(rl_gallery).height(NSAActivity.TOP_VIEW_HEIGHT, false);
		}
		if (mListView.getHeight() == 0) {
			aq.id(mListView).height(NSAActivity.BOT_VIEW_HEIGHT, false);
		}
	}

	private String getUserKey() {
		return db.getUserKey(((Kid) mGlKids.getSelectedItem()).getKidId());
	}

	private Handler mRefreshHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == FragmentNSA.REFRESH_WHAT) {
				this.removeMessages(FragmentNSA.REFRESH_WHAT);
				if (mInboxes.size() > 0) {
					// currently have inbox in display
					mCallback.loadInbox();
				}
				if (mOutboxes.size() > 0) {
					// currently have outbox in display
					mCallback.loadOutbox();
				}
				this.sendEmptyMessageDelayed(FragmentNSA.REFRESH_WHAT, FragmentNSA.REFRESH_INTERVAL);
			}
		}
	};

	private void addBlockFlag(getInboxes_outObj data) {
		ArrayList<InboxesData> list = data.getInboxes();
		for (InboxesData id : list) {
			id.blocked = mCallback.isFriendBlocked(data.mUserKey, id.userId);
		}
		if (mInboxAdapter != null) {
			int y = sv_root.getScrollY();
			mInboxAdapter.notifyDataSetChanged();
			sv_root.setScrollY(y);
		}
	}

	private void addBlockFlag(getOutboxes_outObj data) {
		ArrayList<OutboxesData> list = data.getOutboxes();
		for (OutboxesData id : list) {
			id.blocked = mCallback.isFriendBlocked(data.mUserKey, id.userId);
		}
		if (mOutboxAdapter != null) {
			int y = sv_root.getScrollY();
			mOutboxAdapter.notifyDataSetChanged();
			sv_root.setScrollY(y);
		}
	}
}
