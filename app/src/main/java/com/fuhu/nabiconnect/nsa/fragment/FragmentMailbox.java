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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.data.MailData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.nsa.NSAActivity;
import com.fuhu.nabiconnect.nsa.util.ApiHelper;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.ndnslibsoutstructs.getMail_outObj;
import com.fuhu.ndnslibsoutstructs.getOutgoingMail_outObj;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * used to display mail messages of selected mailbox
 */
public class FragmentMailbox extends FragmentNSA {

	final private static String TAG = FragmentMailbox.class.getSimpleName();

	private AQuery aq;

	private String mUserKey;
	private boolean mIsBlocked = false;
	private String mAvatarUrl;
	private String mMailboxId;
	private long mUnreadCount;
	private String mContactName;
	private String mContactKey;

	private ListView mLvMail;
	private ArrayList<MailData> mMail = new ArrayList<MailData>();
	private MailAdapter mMailAdapter;

	private HashSet<Long> mDeleteFlag = new HashSet<Long>();

	/** used to store ids of mail whose delete request is already sent */
	private HashSet<Long> mDeletePool = new HashSet<Long>();

	private NSAActivity mActivity;

	private Bitmap mDefaultAvatar;

	private DatabaseAdapter db;

//    {
//        FragmentNSA.TRACKING_NAME = TrackingInfo.NSA_MAIL_DETAILS;
//    }
    @Override
    public String getTrack() {
        return "mail_details_#" + Tracking.TrackingInfoFragment.TRACK_SPECIAL;
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nsa_fragment_mailbox, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aq = new AQuery(getActivity());
		db = mCallback.getDB();
		NSAUtil.setTypeface(getActivity(), aq.id(R.id.tv_title).getTextView(), getString(R.string.roboto_bold));

		mLvMail = aq.id(R.id.lv_mail).getListView();
		mMailAdapter = new MailAdapter(getActivity(), 0, mMail);
		mLvMail.setAdapter(mMailAdapter);

		aq.id(R.id.iv_back).clicked(back_ocl);
		aq.id(R.id.tv_name).getTextView().setSelected(true);

		mActivity = (NSAActivity) getActivity();
		initHandler(mActivity);
		mDefaultAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.chat_avatar_default);
	}

	public void setData(Bundle bundle) {
		mUserKey = bundle.getString(FragmentNSA.KEY_MY_USERKEY);
		mIsBlocked = bundle.getBoolean(FragmentNSA.KEY_BLOCKED);
		mAvatarUrl = bundle.getString(FragmentNSA.KEY_AVATAR_URL);
		mMailboxId = bundle.getString(FragmentNSA.KEY_MAILBOX_ID);
		if (bundle.containsKey(FragmentNSA.KEY_UNREAD)) {
			mUnreadCount = bundle.getLong(FragmentNSA.KEY_UNREAD);
		} else {
			mUnreadCount = -1;
		}
		mContactName = bundle.getString(FragmentNSA.KEY_USER_NAME);
		mContactKey = bundle.getString(FragmentNSA.KEY_USER_ID);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mUnreadCount == -1) {
			// only listen for outbox messages
			mActivity.onGetSentMailContent.addEventListener(onSentMessageLoaded);
		} else {
			mActivity.onGetMailContent.addEventListener(onMailMessageLoaded);
		}

		// mActivity.onDeleteInMailMessage.addEventListener(onInMailDeleted);
		// mActivity.onDeleteOutMailMessage.addEventListener(onOutMailDeleted);

		NSAUtil.setTypeface(getActivity(), aq.id(R.id.tv_name).text(mContactName).getTextView(),
				getString(R.string.roboto_medium));

		aq.id(R.id.tv_title).text(
				mUnreadCount == -1 ? getString(R.string.nsa_sent_mail) : getString(R.string.nsa_inbox));

		loadUserAvatar();

		if (mUnreadCount == -1) {
			mMail.addAll(db.getSentMail(mUserKey, mMailboxId));
			mCallback.loadOutboxMessage(mMailboxId);
		} else {
			mMail.addAll(db.getReceivedMail(mUserKey, mMailboxId));
			mCallback.loadInboxMessage(mMailboxId);
		}
	}

	@Override
	public void onPause() {
		mActivity.onGetMailContent.removeEventListener(onMailMessageLoaded);
		mActivity.onGetSentMailContent.removeEventListener(onSentMessageLoaded);
		// mActivity.onDeleteInMailMessage.removeEventListener(onInMailDeleted);
		// mActivity.onDeleteOutMailMessage.removeEventListener(onOutMailDeleted);
		mMail.clear();
		super.onPause();
	}

	private IApiEventListener onMailMessageLoaded = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				getMail_outObj data = (getMail_outObj) obj;
				db.updateNSAReceivedMail(mUserKey, data);
				if (data.mBoxId.equals(mMailboxId)) {
					mMail.clear();
					mMail.addAll(db.getReceivedMail(mUserKey, data.mBoxId));
				}
			} else if (NSAActivity.DEBUG) {
				mMail.addAll(NSAUtil.getFakeMailMessage());
			} else {
				// TODO: mail message load failed
				mCallback.loadInboxMessage(mMailboxId);
			}
			mMailAdapter.notifyDataSetChanged();
		}
	};

	private IApiEventListener onSentMessageLoaded = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				getOutgoingMail_outObj data = (getOutgoingMail_outObj) obj;
				db.updateNSASentMail(mUserKey, data);
				if (data.mBoxId.equals(mMailboxId)) {
					mMail.clear();
					mMail.addAll(db.getSentMail(mUserKey, data.mBoxId));
				}
			} else if (NSAActivity.DEBUG) {
				mMail.addAll(NSAUtil.getFakeMailMessage());
			} else {
				// TODO: mail message load failed
				mCallback.loadOutboxMessage(mMailboxId);
			}
			mMailAdapter.notifyDataSetChanged();
		}
	};

	// private IApiEventListener onInMailDeleted = new IApiEventListener() {
	// @Override
	// public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
	// if (isSuccess) {
	// deleteLocalMail();
	// } else if (NSAActivity.DEBUG) {
	// deleteLocalMail();
	// } else {
	// LOG.D(TAG, "incoming mail delete failed");
	// mCallback.showErrorDialog(false);
	// // TODO: message delete failed
	// }
	// }
	// };

	// private IApiEventListener onOutMailDeleted = new IApiEventListener() {
	// @Override
	// public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
	// if (isSuccess) {
	// deleteLocalMail();
	// } else if (NSAActivity.DEBUG) {
	// deleteLocalMail();
	// } else {
	// LOG.D(TAG, "outgoing mail delete failed");
	// mCallback.showErrorDialog(false);
	// // TODO: message delete failed
	// }
	// }
	// };

	public void deleteLocalMail(long mailId, boolean success) {
		mDeletePool.remove(mailId);
		if (success) {
			for (MailData md : mMail) {
				if (md.mailId == mailId) {
					mMailAdapter.remove(md);
					break;
				}
			}
			db.deleteMail(mUserKey, Long.toString(mailId));
			mDeleteFlag.remove(mailId);
			mMailAdapter.notifyDataSetChanged();
		}
	}

	private class MailAdapter extends ArrayAdapter<MailData> {
		private LayoutInflater inflater;
		private AQuery aq;

		public MailAdapter(Context context, int textViewResourceId, List<MailData> objects) {
			super(context, textViewResourceId, objects);
			inflater = LayoutInflater.from(context);
			aq = new AQuery(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.nsa_item_mail_msg, null);
				holder = new ViewHolder();
				holder.iv_mail = (ImageView) convertView.findViewById(R.id.iv_mail);
				holder.iv_mail.setOnClickListener(mail_ocl);
				holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
				holder.iv_delete.setOnClickListener(delete_ocl);
				// holder.pb_loading = (ProgressBar)
				// convertView.findViewById(R.id.pb_loading);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final MailData md = getItem(position);
			final String mailId = Long.toString(md.mailId);
			final ImageView iv_mail = holder.iv_mail;
			// final ProgressBar pb_loading = holder.pb_loading;
			if (db.mailThumbnailExist(mUserKey, mailId)) {
				aq.id(iv_mail).image(new File(db.getMailThumbnailUrl(mUserKey, mailId)), false,
						FragmentNSA.MAIL_TARGET_WIDTH, new BitmapAjaxCallback() {
							@Override
							protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
								if (bm != null) {
									int duration = iv.getDrawable() == null ? 300 : 0;
									iv.setImageBitmap(bm);
									AlphaAnimation anim = new AlphaAnimation(0, 1);
									anim.setInterpolator(new DecelerateInterpolator());
									anim.setDuration(duration);
									iv.startAnimation(anim);
								}
							}
						});
			} else {
				// fetch from network
				aq.id(iv_mail)
				// .progress(pb_loading)
						.image(md.thumbnailUrl.replace("https", "http"), true, false, FragmentNSA.MAIL_TARGET_WIDTH, 0,
								new BitmapAjaxCallback() {
									@Override
									protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
										if (bm != null) {
											db.saveMailThumbnailAsync(mUserKey, mailId, bm);
											iv.setImageBitmap(bm);
											AlphaAnimation anim = new AlphaAnimation(0, 1);
											anim.setInterpolator(new DecelerateInterpolator());
											anim.setDuration(300);
											iv.startAnimation(anim);
										} else {
											// try loading full image
											aq.id(iv_mail)
											// .progress(pb_loading)
													.image(md.fileUrl.replace("https", "http"), true, false,
															FragmentNSA.MAIL_TARGET_WIDTH, 0, new BitmapAjaxCallback() {
																@Override
																protected void callback(String url, ImageView iv,
																		Bitmap bm, AjaxStatus status) {
																	if (bm != null) {
																		db.saveMailThumbnailAsync(mUserKey, mailId, bm);
																		iv.setImageBitmap(bm);
																		AlphaAnimation anim = new AlphaAnimation(0, 1);
																		anim.setInterpolator(new DecelerateInterpolator());
																		anim.setDuration(300);
																		iv.startAnimation(anim);
																	} else {
																		iv.setImageDrawable(null);
																	}
																}
															});
										}
									}
								});
			}
			holder.iv_mail.setTag(md);
			holder.iv_delete.setSelected(mDeleteFlag.contains(md.mailId));
			holder.iv_delete.setTag(md);
			return convertView;
		}
	}

	private static class ViewHolder {
		public ImageView iv_mail;
		public ImageView iv_delete;
		// public ProgressBar pb_loading;
	}

	private View.OnClickListener mail_ocl = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			MailData md = (MailData) v.getTag();
			mCallback.showMailContent(mUserKey, Long.toString(md.mailId), md.fileUrl);

            //tracking
            Tracking.pushTrack(v.getContext(), "view_message_#" + md.mailId);
		}
	};

	// private Long mDeleteMailId;
	private View.OnClickListener delete_ocl = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			MailData md = (MailData) v.getTag();
			Long id = md.mailId;
			// already marked send delete request
			if (mDeleteFlag.contains(id) && v.isSelected()) {
				if (mDeletePool.add(id)) {
					mCallback.deleteMailMessage(mMailboxId, Long.toString(id), mUnreadCount != -1, mHandler);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "delete_message_#" + id);
				}
			} else {
				// mark delete and return
				mDeleteFlag.add(id);
				v.setSelected(true);

                //tracking
                Tracking.pushTrack(v.getContext(), "delete_message_select_#" + id);
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

	private void loadUserAvatar() {
		final String avatarUrl = mAvatarUrl.replace("https", "http");
		final ImageView iv_avatar = aq.id(R.id.iv_avatar).tag(AQuery.TAG_1, avatarUrl).getImageView();
		if (mIsBlocked) {
			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0);
			iv_avatar.setColorFilter(new ColorMatrixColorFilter(matrix));
		}

		if (BitmapAjaxCallback.getMemoryCached(mContactKey, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
			iv_avatar.setImageBitmap(BitmapAjaxCallback.getMemoryCached(mContactKey, FragmentNSA.AVATAR_TARGET_WIDTH));
		} else {
			final String userKey = mUserKey;
			// fetch newest from network
			final Bitmap cache = db.getFriendAvatar(userKey, mContactKey, FragmentNSA.AVATAR_TARGET_WIDTH);
			final boolean hasCache = cache != null;
			BitmapAjaxCallback callback = new BitmapAjaxCallback() {
				@Override
				protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
					if (bmp != null) {
						// save once download is finished
						db.saveAvatarAsync(userKey, mContactKey, bmp);
						this.memPut(mContactKey, bmp);
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
	}

	private MyHandler mHandler;

	private void initHandler(NSAActivity activity) {
		mHandler = new MyHandler(activity);
	}

	static class MyHandler extends Handler {

		private WeakReference<NSAActivity> mReference;

		public MyHandler(NSAActivity activity) {
			mReference = new WeakReference<NSAActivity>(activity);
		}

		@Override
		public void handleMessage(Message m) {
			NSAActivity activity = mReference.get();
			if (activity == null || activity.isFinishing()) {
				return;
			}

			switch (m.what) {
			case ApiHelper.MAIL_IN_DELETE:
			case ApiHelper.MAIL_OUT_DELETE:
				activity.onMailDeleted(m.obj.toString(), m.arg1 == 0 || NSAActivity.DEBUG);
				break;
			}
		}
	};
}