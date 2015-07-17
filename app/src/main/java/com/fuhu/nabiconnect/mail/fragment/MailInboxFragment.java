package com.fuhu.nabiconnect.mail.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.data.InboxesData;
import com.fuhu.data.MailData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.IOnMainBarItemSelectedListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.MailActivity;
import com.fuhu.nabiconnect.mail.MailActivity.ReplyReceiverData;
import com.fuhu.nabiconnect.mail.dialog.MailContentDialog;
import com.fuhu.nabiconnect.mail.widget.MailContentWidget;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.ndnslibsoutstructs.getInboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getMail_outObj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class MailInboxFragment extends Tracking.TrackingInfoFragment {

	public static final String TAG = "MailInboxFragment";

	private static final int MSG_RELOAD_INBOX_LIST = 10001;

	private static final int REFRESH_PERIOD = 30 * 1000;

	private MailActivity m_Activity;
	private ListView m_InboxItemListView;
	private ImageView m_MailBackButton;
	private RelativeLayout m_MailContentContainer;
	private ListView m_MailsListView;

	private ImageView m_SenderAvatar;
	private TextView m_SenderName;
	private MailContentDialog m_MailContentDialog;
	private IOnMainBarItemSelectedListener m_MainBarCallback;
	private boolean m_IsRefreshing;

	private InboxesData m_CurrentInboxData;
	private Bitmap m_CurrentContactAvatar;

	private InboxListItemWidgetAdapter m_InboxListItemAdapter;
	private Hashtable<String, Bitmap> m_AvatarTable = new Hashtable<String, Bitmap>();
	private ArrayList<LoadAvatarBitmapTask> m_ContentTaskList = new ArrayList<LoadAvatarBitmapTask>();
	private ArrayList<LoadAvatarBitmapTask> m_ItemListTaskList = new ArrayList<LoadAvatarBitmapTask>();
	private ArrayList<InboxesData> m_InboxDataList;
	private Hashtable<MailData, Bitmap> m_MailContentThumbnailTable = new Hashtable<MailData, Bitmap>();
	private MailContentWidgetAdapter m_MailContentAdapter;

	private IButtonClickListener m_ButtomClickedLisener = new IButtonClickListener() {

		@Override
		public void onButtonClicked(int buttonId, String viewName, Object[] args) {
			if (MailContentWidget.TAG.equals(viewName)) {
				Bitmap contentBitmap = (Bitmap) args[0];
				String fullImageUrl = (String) args[1];
				if (contentBitmap != null) {
					m_MailContentDialog = new MailContentDialog(m_Activity, contentBitmap, fullImageUrl);
					m_MailContentDialog.addButtonListener(m_ButtomClickedLisener);
					m_MailContentDialog.setCancelable(true);
					m_MailContentDialog.setCanceledOnTouchOutside(true);
					m_MailContentDialog.show();
				}
			} else if (MailContentDialog.TAG.equals(viewName)) {
				if (m_MailContentDialog != null && m_MailContentDialog.isShowing())
					m_MailContentDialog.dismiss();

				m_MainBarCallback.OnMainBarItemSelected(MailMainBarFragment.ITEM_COMPOSE_ID, new ReplyReceiverData(
						m_CurrentInboxData, m_CurrentContactAvatar));
			}
		}
	};

	private IApiEventListener m_MailInboxesEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "m_MailInboxesEventListener - success");

				getInboxes_outObj data = (getInboxes_outObj) obj;
				updateInboxList(data);

				// send delay message for reloading
				if (m_Handler != null)
					m_Handler.sendEmptyMessageDelayed(MSG_RELOAD_INBOX_LIST, REFRESH_PERIOD);

			} else {
				LOG.V(TAG, "m_MailInboxesEventListener - failed to inbox list");
				if (!m_IsRefreshing)
					m_Activity.showGeneralWarningDialog();
				else {
					// send delay message for reloading
					if (m_Handler != null)
						m_Handler.sendEmptyMessageDelayed(MSG_RELOAD_INBOX_LIST, REFRESH_PERIOD);
				}

			}

		}
	};

	private IApiEventListener m_MailContentEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "m_MailContentEventListener - success");

				m_MailContentContainer.setVisibility(View.VISIBLE);
				m_MailBackButton.setVisibility(View.VISIBLE);
				m_InboxItemListView.setVisibility(View.INVISIBLE);

				getMail_outObj data = (getMail_outObj) obj;
				// m_SenderAvatar.setImageResource(avatarId);

				loadMailContent(data);

			} else {
				LOG.V(TAG, "m_MailContentEventListener - failed to mail content");
				m_Activity.showGeneralWarningDialog();
			}

		}
	};

	private IApiEventListener mOnUserLogin = new IApiEventListener() {
		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				m_Activity.setNeedRelogin(false);
				m_Activity.getInboxes(m_Activity.getCurrentUserData().userKey);
			} else {
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private Handler m_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			LOG.V(TAG, "Receive msg " + msg.what);

			switch (msg.what) {
			case MSG_RELOAD_INBOX_LIST:

				if (!m_Activity.getNetworkManager().checkWifiProcess())
					return;

				m_IsRefreshing = true;
				// get inboxes
				m_Activity.getInboxes(m_Activity.getCurrentUserData().userKey, false);

				break;

			}
			super.handleMessage(msg);
		}
	};

    public MailInboxFragment() {
        super(MailInboxFragment.class.getSimpleName());
    }

    @Override
    public String getTrack() {
        return "my_inbox";
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_Activity = (MailActivity) this.getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.mail_inbox_view, container, false);
	}

	@Override
	public void onPause() {
		super.onPause();

		// canael tasks
		for (LoadAvatarBitmapTask task : m_ContentTaskList) {
			if (task != null)
				task.cancel(true);
		}
		m_ContentTaskList.clear();

		for (LoadAvatarBitmapTask task : m_ItemListTaskList) {
			if (task != null)
				task.cancel(true);
		}
		m_ItemListTaskList.clear();

		// remove pending reload message
		if (m_Handler != null)
			m_Handler.removeMessages(MSG_RELOAD_INBOX_LIST);

		m_InboxListItemAdapter = null;
		m_MailContentAdapter = null;

		removeApiEventListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		m_InboxItemListView = (ListView) getView().findViewById(R.id.mail_inbox_listitem_list_view);
		m_MailBackButton = (ImageView) getView().findViewById(R.id.inbox_bar_back);
		m_MailContentContainer = (RelativeLayout) getView().findViewById(R.id.mail_inbox_content_container);
		m_SenderAvatar = (ImageView) getView().findViewById(R.id.sender_avatar);
		m_SenderName = (TextView) getView().findViewById(R.id.sender_name);
		m_MailsListView = (ListView) getView().findViewById(R.id.mail_inbox_mails_list_view);

		m_MailBackButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				m_MailContentContainer.setVisibility(View.INVISIBLE);
				m_MailBackButton.setVisibility(View.INVISIBLE);
				m_InboxItemListView.setVisibility(View.VISIBLE);

				// reset the avatar
				m_SenderAvatar.setImageResource(R.drawable.chat_avatar_default);

				// cancel tasks
				for (LoadAvatarBitmapTask task : m_ContentTaskList)
					task.cancel(true);
				m_ContentTaskList.clear();

				// reset hash table & adapter
				m_MailContentThumbnailTable.clear();
				m_MailContentAdapter = null;

				// send delay message for reloading
				if (m_Handler != null) {
					m_Handler.removeMessages(MSG_RELOAD_INBOX_LIST);
					m_Handler.sendEmptyMessage(MSG_RELOAD_INBOX_LIST);
				}

                //tracking
                Tracking.pushTrack(v.getContext(), "back_button");
                Tracking.trackBack();
			}
		});

		try {
			m_MainBarCallback = (IOnMainBarItemSelectedListener) m_Activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(m_Activity.toString() + " must implement OnMainBarItemSelectedListener");
		}

		addApiEventListener();

		m_IsRefreshing = false;

		if (!m_Activity.isFinishing()) {
			if (m_Activity.getNeedRelogin()) {
				// login is dirty
				if (m_Activity.isParentMode()) {
					m_Activity.loginAccountNoKid();
				} else {
					m_Activity.loginAccount();
				}
			} else {
				// get inboxes
				m_Activity.getInboxes(m_Activity.getCurrentUserData().userKey);
			}
		}
	}

	private void loadMailContent(getMail_outObj mailContent) {

		if (mailContent == null) {
			LOG.V(TAG, "loadMailContent() - mailContent is null");
			return;
		}

		for (final MailData fData : mailContent.getMail()) {
			LoadAvatarBitmapTask task = new LoadAvatarBitmapTask();
			m_ContentTaskList.add(task);
			final String imageUrl = (fData.thumbnailUrl == null || fData.thumbnailUrl.isEmpty()) ? fData.fileUrl
					: fData.thumbnailUrl;
			Utils.executeAsyncTask(task, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

				@Override
				public void onBitmapLoaded(Bitmap bitmap) {

					if (bitmap == null) {
						LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");

						// load full image
						LoadAvatarBitmapTask fullImageLoadingTask = new LoadAvatarBitmapTask();
						m_ContentTaskList.add(fullImageLoadingTask);
						Utils.executeAsyncTask(fullImageLoadingTask, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

							@Override
							public void onBitmapLoaded(Bitmap bitmap) {

								if (bitmap != null) {
									// update hash table
									m_MailContentThumbnailTable.put(fData, bitmap);

									// notify data change
									if (m_MailContentAdapter != null)
										m_MailContentAdapter.notifyDataSetChanged();
								}

							}
						}, fData.fileUrl);

						return;
					}

					// update hash table
					m_MailContentThumbnailTable.put(fData, bitmap);

					// notify data change
					if (m_MailContentAdapter != null)
						m_MailContentAdapter.notifyDataSetChanged();

				}
			}, imageUrl);

		}

		if (m_MailContentAdapter == null) {
			m_MailContentAdapter = new MailContentWidgetAdapter(m_Activity, 0, mailContent.getMail());
			m_MailsListView.setAdapter(m_MailContentAdapter);
		} else
			m_MailContentAdapter.notifyDataSetChanged();
	}

	private void updateInboxList(getInboxes_outObj inboxList) {

		if (inboxList == null) {
			LOG.V(TAG, "updateInboxList() - inboxList is null");
			return;
		}

		if (m_InboxDataList == null)
			m_InboxDataList = new ArrayList<InboxesData>();

		m_InboxDataList.clear();

		// sort the inboxes by unread count
		Comparator<InboxesData> mComparator = new Comparator<InboxesData>() {
			@Override
			public int compare(InboxesData id1, InboxesData id2) {
				Long unreadCount1 = id1.newReceiveCount;
				Long unreadCount2 = id2.newReceiveCount;
				return unreadCount2.compareTo(unreadCount1);
			}
		};
		Collections.sort(inboxList.getInboxes(), mComparator);
		m_InboxDataList.addAll(inboxList.getInboxes());

		for (final InboxesData fData : inboxList.getInboxes()) {
			// load avatar from db
			Bitmap bitmap = m_Activity.getDatabaseAdapter().getFriendAvatar(m_Activity.getCurrentUserData().userKey,
					fData.userId,
					m_Activity.getResources().getDimensionPixelSize(R.dimen.mail_choose_contact_widget_avatar_size));

			if (bitmap != null)
				m_AvatarTable.put(fData.userId, bitmap);
			else
				LOG.V(TAG, "loadContact() - no avatar in db");

			// load avatar from server
			LoadAvatarBitmapTask task = new LoadAvatarBitmapTask();
			m_ItemListTaskList.add(task);
			Utils.executeAsyncTask(task, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

				@Override
				public void onBitmapLoaded(Bitmap bitmap) {

					if (bitmap == null) {
						LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
						return;
					}

					if (m_Activity != null) {
						// update database
						m_Activity.getDatabaseAdapter().saveFriendAvatar(m_Activity.getCurrentUserData().userKey,
								fData.userId, bitmap);
					}

					// update hash table
					m_AvatarTable.put(fData.userId, bitmap);

					// notify data change
					if (m_InboxListItemAdapter != null)
						m_InboxListItemAdapter.notifyDataSetChanged();

				}
			}, fData.avatarURL);
		}

		if (m_InboxListItemAdapter == null) {
			m_InboxListItemAdapter = new InboxListItemWidgetAdapter(m_Activity, 0, m_InboxDataList);
			m_InboxItemListView.setAdapter(m_InboxListItemAdapter);
			m_InboxItemListView.setOnItemClickListener(oicl);
		} else
			m_InboxListItemAdapter.notifyDataSetChanged();

	}

	private void addApiEventListener() {
		LOG.V(TAG, "addApiEventListener() - start");
		m_Activity.onLoginAccount.addEventListener(mOnUserLogin);
		m_Activity.onGetMailInboxes.addEventListener(m_MailInboxesEventListener);
		m_Activity.onGetMailContent.addEventListener(m_MailContentEventListener);

		LOG.V(TAG, "addApiEventListener() - end");
	}

	private void removeApiEventListener() {
		LOG.V(TAG, "removeApiEventListener() - start");
		m_Activity.onLoginAccount.removeEventListener(mOnUserLogin);
		m_Activity.onGetMailInboxes.removeEventListener(m_MailInboxesEventListener);
		m_Activity.onGetMailContent.removeEventListener(m_MailContentEventListener);

		LOG.V(TAG, "removeApiEventListener() - end");
	}

	private class MailContentWidgetAdapter extends ArrayAdapter<MailData> {

		public MailContentWidgetAdapter(Context context, int resource, List<MailData> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(m_Activity);
				convertView = inflater.inflate(R.layout.mail_content_widget, parent, false);
			}

			RelativeLayout m_BackgroundContainer = (RelativeLayout) convertView
					.findViewById(R.id.mail_content_background);
			ImageView m_MailContentImage = (ImageView) m_BackgroundContainer.findViewById(R.id.mail_content_image);
			ProgressBar m_ProgressBar = (ProgressBar) m_BackgroundContainer.findViewById(R.id.marker_progress);

			final MailData mailData = getItem(position);
			final String fullImageUrl = mailData.fileUrl;
			final Bitmap thumbnail = m_MailContentThumbnailTable.get(mailData);
			if (thumbnail != null) {
				m_ProgressBar.setVisibility(View.INVISIBLE);
				m_MailContentImage.setVisibility(View.VISIBLE);
				m_MailContentImage.setImageBitmap(thumbnail);
			} else {
				m_ProgressBar.setVisibility(View.VISIBLE);
				m_MailContentImage.setVisibility(View.INVISIBLE);
				m_MailContentImage.setImageDrawable(null);
			}

			m_MailContentImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					if (thumbnail != null) {
						m_MailContentDialog = new MailContentDialog(m_Activity, thumbnail, fullImageUrl);
						m_MailContentDialog.addButtonListener(m_ButtomClickedLisener);
						m_MailContentDialog.setCancelable(true);
						m_MailContentDialog.setCanceledOnTouchOutside(true);
						m_MailContentDialog.show();
					}

                    //tracking
                    Tracking.pushTrack(v.getContext(), "enlarge_image_#" + mailData.mailId);
				}
			});

			return convertView;
		}

	}

	private class InboxListItemWidgetAdapter extends ArrayAdapter<InboxesData> {

		private LayoutInflater inflater;

		public InboxListItemWidgetAdapter(Context context, int resource, List<InboxesData> objects) {
			super(context, resource, objects);
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			InboxHolder holder;
			if (convertView == null) {
				holder = new InboxHolder();
				convertView = inflater.inflate(R.layout.mail_inbox_listitem_widget, parent, false);
				holder.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.iv_mail_icon = (ImageView) convertView.findViewById(R.id.iv_mail_icon);
				holder.tv_unread = (TextView) convertView.findViewById(R.id.tv_unread);
				convertView.setTag(holder);
			} else {
				holder = (InboxHolder) convertView.getTag();
			}

			final InboxesData inboxData = getItem(position);

			holder.tv_name.setText(inboxData != null ? inboxData.userName : "");
			if (inboxData.newReceiveCount > 0) {
				holder.iv_mail_icon.setImageResource(R.drawable.mail_indicator_unread);
				holder.tv_unread.setVisibility(View.VISIBLE);
				holder.tv_unread.setText(String.valueOf(inboxData.newReceiveCount));
			} else {
				holder.iv_mail_icon.setImageResource(R.drawable.mail_indicator_read);
				holder.tv_unread.setVisibility(View.INVISIBLE);
			}

			Bitmap avatar = m_AvatarTable.get(inboxData.userId);
			if (avatar != null) {
				holder.iv_avatar.setImageBitmap(avatar);
			}

			holder.avatar = avatar;
			return convertView;
		}
	}

	static private class InboxHolder {
		public ImageView iv_avatar;
		public TextView tv_name;
		public TextView tv_unread;
		public ImageView iv_mail_icon;
		public Bitmap avatar;
	}

	private AdapterView.OnItemClickListener oicl = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			InboxesData data = m_InboxDataList.get(position);
			data.newReceiveCount = 0;

			InboxHolder holder = (InboxHolder) view.getTag();
			if (!m_Activity.getNetworkManager().checkWifiProcess()) {
				return;
			}

			if (m_Handler != null) {
				m_Handler.removeMessages(MSG_RELOAD_INBOX_LIST);
			}

			for (LoadAvatarBitmapTask task : m_ItemListTaskList) {
				if (task != null) {
					task.cancel(true);
				}
			}

			m_ItemListTaskList.clear();
			m_CurrentInboxData = data;
			m_CurrentContactAvatar = holder.avatar;
			m_SenderName.setText(data.userName);
			if (holder.avatar == null) {
				LoadAvatarBitmapTask task = new LoadAvatarBitmapTask();
				m_ContentTaskList.add(task);
				Utils.executeAsyncTask(task, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

					@Override
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap == null) {
							LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
							return;
						}
						m_CurrentContactAvatar = bitmap;
						m_Activity.getDatabaseAdapter().saveAvatarAsync(m_Activity.getCurrentUserData().userKey,
								m_CurrentInboxData.userId, bitmap);
						m_SenderAvatar.setImageBitmap(bitmap);
					}
				}, data.avatarURL);
			} else {
				m_SenderAvatar.setImageBitmap(holder.avatar);
			}
			m_Activity.getMail(m_Activity.getCurrentUserData().userKey, data.inboxID);
			m_InboxListItemAdapter.notifyDataSetChanged();

            //tracking
            Tracking.pushTrack(view.getContext(), "view_message_#" + data.userName);
            Tracking.trackNextSync("message_" + data.userName);
		}
	};
}