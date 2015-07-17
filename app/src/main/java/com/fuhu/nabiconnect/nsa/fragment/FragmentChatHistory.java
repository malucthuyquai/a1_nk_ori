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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.data.messageData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.chat.stickers.StickerManager;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.nsa.NSAActivity;
import com.fuhu.nabiconnect.nsa.util.ApiHelper;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.chatHistory_outObj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class FragmentChatHistory extends FragmentNSA {

	final private String TAG = FragmentChatHistory.class.getSimpleName();

	private AQuery aq;

	private ListView mLvChatHistory;
	private HistoryAdapter mHistoryAdapter;
	private ArrayList<messageData> mMessages = new ArrayList<messageData>();
	private HashSet<String> mDeleteFlag = new HashSet<String>();

	/** used to store ids of message whose delete request is already sent */
	private HashSet<String> mDeletePool = new HashSet<String>();

	private String mUserKey;
	private String mTargetId;
	private String mTargetName;
	private boolean mIsBlocked;
	private String mConversationId;
	private long mInitTimestamp;
	private long mLastTimestamp;
	private String mTargetUrl;

	private NSAActivity mActivity;
	private Bitmap mDefaultAvatar;

	private DatabaseAdapter db;

//    {
//        FragmentNSA.TRACKING_NAME = TrackingInfo.NSA_CHAT_DETAILS;
//    }


    public static String TRACK_CHAT_NAME;
    @Override
    public String getTrack() {
        return "chat_details_#" + TRACK_CHAT_NAME;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setData(savedInstanceState);
		}


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mDeleteFlag.clear();
		return inflater.inflate(R.layout.nsa_fragment_chat_history, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		aq = new AQuery(getActivity());
		db = mCallback.getDB();
		aq.id(R.id.iv_back).clicked(back_ocl);

		NSAUtil.setTypeface(getActivity(), aq.id(R.id.tv_title).getTextView(), getString(R.string.roboto_bold));

		mLvChatHistory = (ListView) getView().findViewById(R.id.lv_history);
		mHistoryAdapter = new HistoryAdapter(getActivity(), 0, mMessages);
		mLvChatHistory.setAdapter(mHistoryAdapter);
		mLvChatHistory.setOnTouchListener(otl);
		// mLvChatHistory.addFooterView(getChatFooterView());

		mActivity = (NSAActivity) getActivity();
		mDefaultAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.chat_avatar_default);
	}

	public void setData(Bundle args) {
		mUserKey = args.getString(FragmentNSA.KEY_USER_ID);
		mTargetId = args.getString(FragmentNSA.KEY_TARGET_ID);
		mTargetName = args.getString(FragmentNSA.KEY_TARGET_NAME);
		mIsBlocked = args.getBoolean(FragmentNSA.KEY_BLOCKED);
		mConversationId = args.getString(FragmentNSA.KEY_CONVERSATION_ID);
		mLastTimestamp = args.getLong(FragmentNSA.KEY_TIMESTAMP);
		mInitTimestamp = mLastTimestamp;
		mTargetUrl = args.getString(FragmentNSA.KEY_AVATAR_URL);
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity.onGetChatHistory.addEventListener(onChatHistoryReceived);
		mMessages.clear();
		loadChatHistory();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(FragmentNSA.KEY_USER_ID, mUserKey);
		outState.putString(FragmentNSA.KEY_TARGET_ID, mTargetId);
		outState.putString(FragmentNSA.KEY_TARGET_NAME, mTargetName);
		outState.putBoolean(FragmentNSA.KEY_BLOCKED, mIsBlocked);
		outState.putString(FragmentNSA.KEY_CONVERSATION_ID, mConversationId);
		outState.putLong(FragmentNSA.KEY_TIMESTAMP, mInitTimestamp);
		outState.putString(FragmentNSA.KEY_AVATAR_URL, mTargetUrl);
	}

	@Override
	public void onPause() {
		mActivity.onGetChatHistory.removeEventListener(onChatHistoryReceived);
		super.onPause();

        //tracking
        Tracking.trackBack(); //cause we pass trackBack once when transform to this fragment on FragmentChat fragment


	}

	private IApiEventListener onChatHistoryReceived = new IApiEventListener() {
		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				chatHistory_outObj data = (chatHistory_outObj) obj;
				db.updateChatHistory(mUserKey, data);
				if (data.mConversationId.equals(mConversationId)) {
					mBuffer.addAll(data.getMessages());
					Collections.reverse(mBuffer);
					if (mBuffer.size() > 0) {
						mLvChatHistory.post(mRefreshChatbox);
					}
				}
			} else if (NSAActivity.DEBUG) {
				mBuffer.addAll(NSAUtil.getFakeMessages(mCallback.getCurrentKid().getKidId(), mTargetId));
				mLvChatHistory.post(mRefreshChatbox);
			} else {
				// TODO: get chat history failed
				loadFinished = true;
			}
		}
	};

	/**
	 * private methods
	 */
	private void deleteLocalMessage(String messageId) {
		for (messageData md : mMessages) {
			if (md.m_MessageId.equals(messageId)) {
				mHistoryAdapter.remove(md);
				break;
			}
		}
		db.deleteChatMessage(mUserKey, messageId);
		mDeleteFlag.remove(messageId);
		mHistoryAdapter.notifyDataSetChanged();
	}

	private class HistoryAdapter extends ArrayAdapter<messageData> {
		private LayoutInflater inflater;
		private AQuery aq;

		private ColorMatrixColorFilter blackAndWhiteFilter;
		private ColorMatrixColorFilter originalFilter;

		public HistoryAdapter(Context context, int textViewResourceId, List<messageData> objects) {
			super(context, textViewResourceId, objects);
			inflater = LayoutInflater.from(context);
			aq = new AQuery(context);

			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0);
			blackAndWhiteFilter = new ColorMatrixColorFilter(matrix);
			matrix.setSaturation(1);
			originalFilter = new ColorMatrixColorFilter(matrix);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.nsa_item_chat_msg, null);
				holder = new ViewHolder();
				// currently not used
				holder.tv_timestamp = (TextView) convertView.findViewById(R.id.tv_timestamp);
				holder.ll_root = (LinearLayout) convertView.findViewById(R.id.ll_root);
				holder.iv_avatar_left = (ImageView) convertView.findViewById(R.id.iv_avatar_left);
				holder.iv_avatar_left.setColorFilter(mIsBlocked ? blackAndWhiteFilter : originalFilter);
				holder.iv_avatar_right = (ImageView) convertView.findViewById(R.id.iv_avatar_right);
				holder.iv_arrow_left = (ImageView) convertView.findViewById(R.id.iv_arrow_left);
				holder.iv_arrow_right = (ImageView) convertView.findViewById(R.id.iv_arrow_right);
				holder.ll_msg = (LinearLayout) convertView.findViewById(R.id.ll_msg);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				NSAUtil.setTypeface(getActivity(), holder.tv_name, getString(R.string.roboto_bold));
				holder.tv_msg = (TextView) convertView.findViewById(R.id.tv_msg);
				NSAUtil.setTypeface(getActivity(), holder.tv_msg, getString(R.string.roboto_regular));
				holder.iv_sticker = (ImageView) convertView.findViewById(R.id.iv_sticker);
				holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
				holder.iv_delete.setOnClickListener(delete_ocl);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// TODO: should further optimize following block
			final messageData data = getItem(position);
			final String url;
			final String key;
			final ImageView iv;
			if (data.m_SenderId.equals(mTargetId)) {
				// is incoming message
				iv = holder.iv_avatar_left;
				url = mTargetUrl.replace("https", "http");
				key = mTargetId;
			} else {
				// out going message
				iv = holder.iv_avatar_right;
				if (mCallback.getUserData() != null) {
					url = mCallback.getUserData().avatarURL.replace("https", "http");
				} else {
					url = "";
				}
				key = mUserKey;
			}

			// load avatar
			aq.id(iv).tag(AQuery.TAG_1, url);
			if (BitmapAjaxCallback.getMemoryCached(key, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
				iv.setImageBitmap(BitmapAjaxCallback.getMemoryCached(key, FragmentNSA.AVATAR_TARGET_WIDTH));
			} else {
				// fetch newest from network
				final Bitmap cache = db.getFriendAvatar(mUserKey, key, FragmentNSA.AVATAR_TARGET_WIDTH);
				final boolean hasCache = cache != null;
				BitmapAjaxCallback callback = new BitmapAjaxCallback() {
					@Override
					protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
						if (bmp != null) {
							// save once download is finished
							db.saveAvatarAsync(mUserKey, key, bmp);
							this.memPut(key, bmp);
							if (imageView.getTag(AQuery.TAG_1).equals(url)) {
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
				callback.url(url);
				callback.imageView(aq.getImageView());
				callback.preset(hasCache ? cache : mDefaultAvatar);
				aq.image(callback);
			}

			int id = StickerManager.getSticker(data.m_MessageContent);
			if (id != -1) {
				// is sticker
				showSticker(holder, id, data.m_SenderId.equals(mTargetId));
			} else {
				// regular text message
				showMessage(holder, data.m_SenderId.equals(mTargetId));
				holder.tv_msg.setText(data.m_MessageContent);
			}
			holder.iv_delete.setSelected(mDeleteFlag.contains(data.m_MessageId));
			holder.iv_delete.setTag(data);
			return convertView;
		}

		private void showSticker(ViewHolder holder, int resId, boolean incoming) {
			holder.ll_root.setGravity(incoming ? Gravity.LEFT : Gravity.RIGHT);
			holder.iv_avatar_left.setVisibility(incoming ? View.VISIBLE : View.GONE);
			holder.iv_avatar_right.setVisibility(!incoming ? View.VISIBLE : View.GONE);
			holder.iv_arrow_left.setVisibility(View.GONE);
			holder.iv_arrow_right.setVisibility(View.GONE);
			holder.ll_msg.setBackgroundDrawable(null);
			holder.tv_name.setVisibility(View.GONE);
			holder.tv_msg.setVisibility(View.GONE);
			holder.iv_sticker.setImageResource(resId);
			holder.iv_sticker.setVisibility(View.VISIBLE);
		}

		private void showMessage(ViewHolder holder, boolean incoming) {
			holder.ll_root.setGravity(incoming ? Gravity.LEFT : Gravity.RIGHT);
			holder.iv_avatar_left.setVisibility(incoming ? View.VISIBLE : View.GONE);
			holder.iv_avatar_right.setVisibility(!incoming ? View.VISIBLE : View.GONE);
			holder.iv_arrow_left.setVisibility(incoming ? View.VISIBLE : View.GONE);
			holder.iv_arrow_right.setVisibility(!incoming ? View.VISIBLE : View.GONE);
			holder.ll_msg.setBackgroundResource(incoming ? R.drawable.nsa_chat_bubble_left
					: R.drawable.nsa_chat_bubble_right);
			holder.ll_msg.setVisibility(View.VISIBLE);
			holder.tv_name.setVisibility(View.VISIBLE);
			holder.tv_name.setText(incoming ? mTargetName : getUserName());
			holder.tv_msg.setVisibility(View.VISIBLE);
			holder.iv_sticker.setImageDrawable(null);
			holder.iv_sticker.setVisibility(View.GONE);
		}

		private String getUserName() {
			if (NSAActivity.DEBUG) {
				return mCallback.getCurrentKid().getkidName();
			} else {
				return mCallback.getUserData().userName;
			}
		}
	}

	static class ViewHolder {
		public LinearLayout ll_root;
		public TextView tv_timestamp;
		public ImageView iv_avatar_left;
		public ImageView iv_avatar_right;
		public ImageView iv_arrow_left;
		public ImageView iv_arrow_right;
		public LinearLayout ll_msg;
		public TextView tv_name;
		public TextView tv_msg;
		public ImageView iv_sticker;
		public ImageView iv_delete;
	}

	/**
	 * click event callback
	 */
	private View.OnClickListener delete_ocl = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			messageData data = (messageData) v.getTag();
			if (v.isSelected()) {
				// already marked, send delete request
				if (mDeletePool.add(data.m_MessageId)) {
					mCallback.delChatMessage(mUserKey, data.m_MessageId, mHandler);
				}
				// local message will be removed in success onChatMessageDeleted

                //tracking
                Tracking.pushTrack(v.getContext(), "delete_message_#" + data.m_MessageId);
			} else {
				v.setSelected(true);
				mDeleteFlag.add(data.m_MessageId);

                //tracking
                Tracking.pushTrack(v.getContext(), "delete_message_select_#" + data.m_MessageId);
			}
		}
	};

	private View.OnClickListener back_ocl = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
            //tracking
            Tracking.pushTrack(v.getContext(), "back");

            mActivity.onBackPressed();
		}
	};

	private ArrayList<messageData> mBuffer = new ArrayList<messageData>();
	private Thread mHistoryLoaderThread;
	volatile boolean loadFinished = true;
	private View.OnTouchListener otl = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mLvChatHistory.getFirstVisiblePosition() == 0) {
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
			return false;
		}
	};

	private boolean mShouldLoadHistory = false;
	private Runnable mHistoryLoader = new Runnable() {
		@Override
		public void run() {
			if (!mShouldLoadHistory) {
				if (mMessages.size() > 0) {
					mLastTimestamp = mMessages.get(0).m_MessageTime;
					mShouldLoadHistory = true;
				}
				return;
			}
			mCallback.loadChatHistory(mConversationId, FragmentNSA.CHAT_POLL_LIMIT, 0, mLastTimestamp, false);
		}
	};

	private Runnable mRefreshChatbox = new Runnable() {
		@Override
		public void run() {
			if (showingLocalHistory) {
				showingLocalHistory = false;
				mMessages.clear();
			}
			mLastTimestamp = mBuffer.get(0).m_MessageTime;
			int idx = mLvChatHistory.getFirstVisiblePosition();
			mMessages.addAll(0, mBuffer);
			mHistoryAdapter.notifyDataSetChanged();
			mLvChatHistory.setSelection(mBuffer.size() + idx);
			mBuffer.clear();
			loadFinished = true;
			mShouldLoadHistory = true;
			// Calendar cal = Calendar.getInstance();
			// cal.setTimeInMillis(mMessages.get(mMessages.size() -
			// 1).m_MessageTime);
			// mTvTimestamp.setText(mDateFormat.format(cal.getTime()));
			// mLvFooter.setVisibility(View.VISIBLE);
		}
	};

	private boolean showingLocalHistory = false;

	private void loadChatHistory() {
		// use local history first
		showingLocalHistory = true;
		mBuffer.addAll(db.getChatHistory(mUserKey, mConversationId));
		Collections.reverse(mBuffer);
		if (mBuffer.size() > 0) {
			// mLastTimestamp = mBuffer.get(0).m_MessageTime;
			int idx = mLvChatHistory.getFirstVisiblePosition();
			mMessages.addAll(0, mBuffer);
			mHistoryAdapter.notifyDataSetChanged();
			mLvChatHistory.setSelection(mBuffer.size() + idx);
			mBuffer.clear();
			loadFinished = true;
			mShouldLoadHistory = true;
		}
		// load from server
		mCallback.loadChatHistory(mConversationId, mBuffer.size() == 0 ? FragmentNSA.CHAT_POLL_LIMIT : mBuffer.size(),
				0, mLastTimestamp, false);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if (m.what == ApiHelper.CHAT_DELETE) {
				mDeletePool.remove(m.obj.toString());
				if (m.arg1 == 0) {
					deleteLocalMessage(m.obj.toString());
				} else if (NSAActivity.DEBUG) {
					deleteLocalMessage(m.obj.toString());
				} else {
					LOG.E(TAG, "message delete failed");
					mCallback.showErrorDialog(false);
				}
			}
		}
	};

	// private LinearLayout mLvFooter;
	// private TextView mTvTimestamp;
	// private SimpleDateFormat mDateFormat = new
	// SimpleDateFormat("EEEE, HH:mm");

	// private View getChatFooterView() {
	// LayoutInflater inflater = LayoutInflater.from(getActivity());
	// mLvFooter = (LinearLayout) inflater.inflate(R.layout.nsa_chat_footer,
	// null);
	// mTvTimestamp = (TextView) mLvFooter.findViewById(R.id.tv_timestamp);
	// NSAUtil.setTypeface(getActivity(), mTvTimestamp,
	// getString(R.string.roboto_regular));
	// mLvFooter.setVisibility(View.GONE);
	// return mLvFooter;
	// }
}